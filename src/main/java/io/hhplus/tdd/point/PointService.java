package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private final Lock lock = new ReentrantLock();

    // 특정 유저의 포인트를 충전하는 기능
    public UserPoint chargeUserPoint(long userId, long amount) {

        lock.lock();

        try {
            UserPoint userPoint = userPointTable.selectById(userId);

            if (amount <= 0) {
                throw new IllegalArgumentException("충전금액은 0원 이하 일 수 없습니다.");
            }

            if (userPoint.point() + amount >= 1_000_000) {
                throw new IllegalArgumentException("충전 결과값이 1_000_000원을 넘을 수 없습니다.");
            }

            userPoint = userPointTable.insertOrUpdate(userId, userPoint.point() + amount);

            pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, userPoint.updateMillis());

            return userPoint;

        } finally {
            lock.unlock();
        }
    }

    // 특정 유저의 포인트를 조회하는 기능
    public UserPoint getUserPoint(long userId) {

        return userPointTable.selectById(userId);
    }

    public UserPoint UseUserPoint(long userId, long amount) {

        lock.lock();

        try {
            UserPoint userPoint = userPointTable.selectById(userId);

            if (amount <= 0) {
                throw new IllegalArgumentException("사용금액이 0원 이하 일 수 없습니다.");
            }

            if (userPoint.point() < amount) {
                throw new IllegalArgumentException("잔액이 부족합니다.");
            }

            userPoint = userPointTable.insertOrUpdate(userId, userPoint.point() - amount);

            pointHistoryTable.insert(userId, amount, TransactionType.USE, userPoint.updateMillis());

            return userPoint;
        } finally {
            lock.unlock();
        }
    }

    public List<PointHistory> getUserPointHistory(long userId) {

        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);

        if (histories.isEmpty()) {
            throw new IllegalArgumentException("포인트 내역 결과가 없습니다.");
        }

        return histories;
    }

}
