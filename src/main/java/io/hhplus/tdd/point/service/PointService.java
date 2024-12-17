package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;

    // 특정 유저의 포인트를 충전하는 기능
    public UserPoint chargeUserPoint(long userId, long amount) {

        UserPoint userPoint = userPointTable.selectById(userId);

        if (amount <= 0) {
            throw new IllegalArgumentException("충전금액은 0원 이하 일 수 없습니다.");
        }

        if (userPoint.point() + amount >= 1_000_000) {
            throw new IllegalArgumentException("충전 결과값이 1_000_000원을 넘을 수 없습니다.");
        }

        return new UserPoint(userId,userPoint.point() + amount,System.currentTimeMillis());
    }

    // 특정 유저의 포인트를 조회하는 기능
    public UserPoint getUserPoint(long userId) {

        UserPoint userPoint = userPointTable.selectById(userId);

        if (userPoint == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        return userPoint;
    }
}
