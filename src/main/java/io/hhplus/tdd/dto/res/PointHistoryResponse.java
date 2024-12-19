package io.hhplus.tdd.dto.res;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;

public record PointHistoryResponse(
        long userId,
        long amount,
        TransactionType type
) {
    public static PointHistoryResponse from(PointHistory pointHistory) {
        return new PointHistoryResponse(
                pointHistory.userId(),
                pointHistory.amount(),
                pointHistory.type()
        );
    }
}
