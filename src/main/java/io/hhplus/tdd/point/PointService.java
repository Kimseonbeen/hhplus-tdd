package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    // 특정 유저의 포인트를 충전하는 기능
    public UserPoint chargeUserPoint(long userId, long amount) {

        UserPoint userPoint = userPointTable.selectById(userId);

        UserPoint updateUserPoint = userPoint.chargePoint(amount);

        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, updateUserPoint.updateMillis());
        return updateUserPoint;
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

        UserPoint updateUserPoint = userPoint.usePoint(amount);

        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

        return updateUserPoint;
    }

    public List<PointHistory> getUserPointHistory(long userId) {
        UserPoint userPoint = userPointTable.selectById(userId);

        if (userPoint == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        return pointHistoryTable.selectAllByUserId(userId);
    }

}
