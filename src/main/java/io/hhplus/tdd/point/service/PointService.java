package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;

    public UserPoint chargeUserPoint(long userId, long amount) {

        UserPoint userPoint = userPointTable.selectById(userId);

        if (amount <= 0) {
            throw new IllegalArgumentException("충전금액은 음수 일 수 없습니다.");
        }

        if (userPoint.point() + amount >= 1_000_000) {
            throw new IllegalArgumentException("충전 결과값이 1_000_000원을 넘을 수 없습니다.");
        }

        return new UserPoint(userId,userPoint.point() + amount,System.currentTimeMillis());
    }
}
