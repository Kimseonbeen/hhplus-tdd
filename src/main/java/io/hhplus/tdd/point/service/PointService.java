package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
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

        return userPoint.chargePoint(amount);
    }

    // 특정 유저의 포인트를 조회하는 기능
    public UserPoint getUserPoint(long userId) {

        UserPoint userPoint = userPointTable.selectById(userId);

        if (userPoint == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        return userPoint;
    }

    public UserPoint UseUserPoint(long userId, long amount) {

        UserPoint userPoint = userPointTable.selectById(userId);

        // userPoint.point에서 포인트 사용
        return userPoint.usePoint(amount);

    }
}
