# 동시성 제어 방식에 대한 분석 및 보고서

## 동시성 제어의 필요성
**1. 포인트 조회**
- 포인트 변경 중(충전/사용) 조회 시 정확하지 않은 잔액 노출 가능합니다.
- 동시에 여러 건의 조회 요청이 올 경우 일관된 값 응답 필요합니다.

```
// 예시 문제 상황
Thread1: 충전 중 (1000 -> 2000 처리 중)
Thread2: 조회 요청 -> 어느 시점의 값을 보여줄지 보장 필요
```

**2. 포인트 충전**
- 동시에 여러 충전 요청이 들어올 경우 정확한 증액 처리 필요합니다.
- 충전과 사용이 동시에 일어날 경우 순차적 처리 필요합니다.
```
초기값: 1_000원
Thread1: 충전 요청 1_000원
Thread2: 충전 요청 2_000원
예상 결과: 4_000원
실제 결과: ?
```

**3. 포인트 사용**
- 잔액 부족 체크와 차감이 원자적으로 이뤄져야 합니다.
- 동시에 여러 사용 요청이 들어올 경우 순차 처리 필요합니다.
```
잔액: 1000
Thread1: 1_000원 사용 요청 -> 잔액 체크 ok
Thread2: 1_000원 사용 요청 -> 잔액 체크 ok
=> 두 요청 모두 성공하면 -1_000원 발생
```
**이러한 이유들로 포인트 충전/사용 대하여 동시성 제어가 필요합니다.**

## 동시성 제어의 키워드
- ### synchronized
- ### ConcurrentHashMap
- ### ReentrantLock

## 동시성 제어 구현 방법
```
// synchronized
public class Counter {
    private int count = 0;
    
    public synchronized void increment() {
        count++;
    }
}

// ConcurrentHashMap
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.computeIfPresent("key", (k, v) -> v + 1);

// ReentrantLock
public class Counter {
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;
    
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
}
```

## **ConcurrentHashMap이나 synchronized를 사용하지 않은 이유**

- **ConcurrentHashMap**: 포인트 증감 연산과 같은 원자성 연산을 보장하기 어려움
- **synchronized**: `synchronized`는 모든 스레드가 하나의 락을 기다리게 되어 **병목 현상**이 발생할 수 있으며, 락을 기다리는 동안 무기한 대기하는 상황이 발생하여 시스템 응답성이 저하될 우려가있습니다.

## **ReentrantLock을 선택한 이유**
### **1. 세밀한 제어 가능**

>타임아웃 설정으로 동시 요청 처리 시 무한 대기 방지
공정성 보장으로 먼저 요청한 스레드가 우선 처리되도록 보장
try-finally 블록으로 명시적 락 관리가 가능하여 안전한 리소스 해제


### 2. 비즈니스 로직 처리

>메모리상의 포인트 값 변경 시 원자성 보장 (증감 연산 도중 다른 스레드 접근 차단)
포인트 부족 상태 체크와 차감을 하나의 원자적 작업으로 처리
충전과 사용 작업이 서로 간섭하지 않도록 보장


### 3.예외 상황 처리

>lock.tryLock() 으로 일정 시간 후 응답 보장
락 획득 실패 시 즉시 에러 응답 가능
InterruptedException 처리로 작업 취소 가능

## 동시성 제어 방식 비교

| **기능**             | **synchronized**                    | **ConcurrentHashMap**                             | **ReentrantLock**                                   |
|----------------------|-------------------------------------|--------------------------------------------------|-----------------------------------------------------|
| **목적**             | 임계 영역에 대한 동기화            | 멀티스레드 환경에서 안전한 맵 사용               | 명시적인 락 관리                                    |
| **자동/수동**        | 자동 (락 획득 및 해제)              | 자동 (맵의 동시 접근을 안전하게 처리)            | 수동 (명시적으로 락을 획득하고 해제해야 함)         |
| **성능**             | 성능 저하 가능                     | 높은 성능, 읽기 작업에 최적화됨                  | 성능에 더 많은 제어 가능, 세밀한 제어 가능          |
| **특징**             | 한 번에 하나의 스레드만 접근       | 락 분할 방식을 사용하여 동시성 보장              | 재진입 가능, tryLock(), Condition 지원             |


## 동시성 제어 검증  

100개의 스레드를 실행할 스레드 풀을 생성하고, 각 스레드에서 작업을 비동기적으로 실행한 후 완료될 때까지 기다린 후, 스레드 풀을 종료합니다.

**예시 코드**

```
long userId = 1L;
long amount = 10L;
int threads = 100;

// 스레드 풀을 생성
// newFixedThreadPool(threads)는 지정된 수(100개)의 스레드를 실행할 수 있는 스레드 풀을 생성
ExecutorService executor = Executors.newFixedThreadPool(threads);
// 모두 작업을 마칠 때까지 기다리기 위한 카운트다운 래치를 생성
// 각 스레드가 작업을 마칠 떄 마다 latch.countDown()을 호출하여 래치 값을 하나씩 감소
CountDownLatch latch = new CountDownLatch(threads);

// 여러 스레드 실행
for (int i = 0; i < threads; i++) {
    // submit()이 호출될 때마다 새로운 스레드가 풀에서 꺼내어져서 작업을 수행
    // submit()은 각 작업을 비동기적으로 실행
    // 즉, for문 내에서 100개의 작업이 순차적으로 submit()으로 스레드 풀에 제출되지만,
    // 각각의 작업은 독립적인 스레드에서 동시에 실행됌
    executor.submit(() -> {
        try {
            pointService.chargeUserPoint(userId, amount);  // charge 호출
            } finally {
                latch.countDown();  // 작업 완료 후 카운트다운
            }
    });
}

// 모든 스레드가 종료될 때까지 기다림
latch.await();
executor.shutdown();
```
## 동시성 검증 테스트 결과

- **여러_스레드에서_포인트_충전_시_정상적으로_처리된다**  
- **여러_스레드에서_포인트_사용_시_정상적으로_처리된다**  
- **여러_스레드에서_포인트_충전_사용_시_정상적으로_처리된다**  

### 위의 3가지 테스트를 통과하여 동시성이 정상적으로 처리됨을 검증하였습니다.


