package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint chargePoint(long addAmount) {

        if (addAmount <= 0) {
            throw new IllegalArgumentException("충전금액은 0원 이하 일 수 없습니다.");
        }

        if (this.point() + addAmount >= 1_000_000) {
            throw new IllegalArgumentException("충전 결과값이 1_000_000원을 넘을 수 없습니다.");
        }

        return new UserPoint(this.id, this.point + addAmount, this.updateMillis);
    }

    // 포인트 사용 메서드 추가
    public UserPoint usePoint(long useAmount) {

        if (useAmount <= 0) {
            throw new IllegalArgumentException("사용금액이 0원 이하 일 수 없습니다.");
        }

        if (this.point() < useAmount) {
            throw new IllegalArgumentException("잔액이 사용금액 보다 작습니다.");
        }

        if (this.point < useAmount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        return new UserPoint(this.id, this.point - useAmount, this.updateMillis);
    }
}
