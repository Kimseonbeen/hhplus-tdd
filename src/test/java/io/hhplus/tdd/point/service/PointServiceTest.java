package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    private static final Logger log = LoggerFactory.getLogger(PointServiceTest.class);

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointService pointService;

    @Test
    void 포인트_충전_시_결과값이_1_000_000원_이상일_경우_요청은_실패한다() {
        long userId = 1L;
        long amount = 0L;
        long invalidAmount = 1_000_000L;

        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // 예외가 발생하는지 확인
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.chargeUserPoint(userId, invalidAmount));

        // 예외 메시지 검증
        assertEquals("충전 결과값이 1_000_000원을 넘을 수 없습니다.", exception.getMessage());

        // 메서드가 정확한 인자로 호출되었는지 검증
        verify(userPointTable).selectById(userId);
    }

    @Test
    void 포인트_충전_시_충전금액은_0원_이하_일시_요청은_실패한다() {
        long userId = 1L;
        long amount = 100L;
        long invalidAmount = 0L;

        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));
        
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.chargeUserPoint(userId, invalidAmount));
        
        assertEquals("충전금액은 0원 이하 일 수 없습니다.", exception.getMessage());
    }

    @Test
    void 포인트_충전_시_요청은_성공한다() {
        long userId = 1L;
        long amount = 100L;
        long plusAmount = 200L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        UserPoint result = pointService.chargeUserPoint(userId, plusAmount);

        assertNotNull(result);
        assertEquals(amount + plusAmount, result.point());

        verify(userPointTable).selectById(userId);
        verify(pointHistoryTable).insert(userId, plusAmount,
                TransactionType.CHARGE, result.updateMillis());
    }

    @Test
    void 유저가_존재하지_않으면_포인트_조회_요청은_실패한다() {
        long userId = 1L;

        when(userPointTable.selectById(userId)).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.getUserPoint(userId));

        assertEquals("존재하지 않는 회원입니다.", exception.getMessage());
    }

    @Test
    void 유저가_존재하면_포인트_조회_요청은_성공한다() {
        long userId = 1L;
        long amount = 100L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        UserPoint result = pointService.getUserPoint(userId);

        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals(amount, result.point());

        verify(userPointTable).selectById(userId);
    }

    @Test
    void 포인트_사용_시_0원_이하_일시_요청은_실패한다() {
        long userId = 1L;
        long amount = 500L;
        long invalidAmount = 0L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.UseUserPoint(userId, invalidAmount));

        assertEquals("사용금액이 0원 이하 일 수 없습니다.", exception.getMessage());
    }

    @Test
    void 포인트_사용_시_잔액이_사용금액_보다_작으면_요청은_실패한다() {
        long userId = 1L;
        long amount = 500L;
        long useAmount = 1000L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.UseUserPoint(userId, useAmount));

        assertEquals("잔액이 사용금액 보다 작습니다.", exception.getMessage());
    }

    @Test
    void 포인트_사용_시_요청은_성공한다() {
        long userId = 1L;
        long amount = 1000L;
        long useAmount = 500L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        UserPoint updateUserPoint = pointService.UseUserPoint(userId, useAmount);

        assertNotNull(updateUserPoint);
        assertEquals(amount - useAmount, updateUserPoint.point());
    }

    @Test
    void 유저가_존재하지_않을_시_포인트_내역_요청은_실패한다() {
        long userId = 1L;

        when(userPointTable.selectById(userId)).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.getUserPointHistory(userId));

        assertEquals("존재하지 않는 회원입니다.", exception.getMessage());
    }

    @Test
    void 유저가_존재하면_포인트_내역_요청은_성공한다() {
        long userId = 1L;
        long amount = 1000L;
        long plusAmount = 500L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        List<PointHistory> pointHistories = List.of(
                new PointHistory(1L, userId, 300L,
                        TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 400L,
                        TransactionType.CHARGE, System.currentTimeMillis())
        );

        when(userPointTable.selectById(userId)).thenReturn(userPoint);
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(pointHistories);

        pointService.chargeUserPoint(userId, plusAmount);

        List<PointHistory> resultList = pointService.getUserPointHistory(userId);

        assertEquals(2, resultList.size());
        assertEquals(resultList.get(0), pointHistories.get(0));
        assertEquals(resultList.get(1), pointHistories.get(1));
    }
}