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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
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
        // given
        long userId = 1L;
        long amount = 0L;
        long invalidAmount = 1_000_000L;

        given(userPointTable.selectById(userId))
                .willReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.chargeUserPoint(userId, invalidAmount));

        // then
        assertEquals("충전 결과값이 1_000_000원을 넘을 수 없습니다.", exception.getMessage());
        verify(userPointTable).selectById(userId);
    }

    @Test
    void 포인트_충전_시_충전금액은_0원_이하_일시_요청은_실패한다() {
        // given
        long userId = 1L;
        long amount = 100L;
        long invalidAmount = 0L;

        given(userPointTable.selectById(userId))
                .willReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.chargeUserPoint(userId, invalidAmount));

        // then
        assertEquals("충전금액은 0원 이하 일 수 없습니다.", exception.getMessage());
    }

    @Test
    void 포인트_충전_시_요청은_성공한다() {
        // given
        long userId = 1L;
        long amount = 100L;
        long plusAmount = 200L;
        long resultAmount = amount + plusAmount;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        given(userPointTable.selectById(userId)).willReturn(userPoint);
        given(userPointTable.insertOrUpdate(userId, resultAmount)).willReturn(
                new UserPoint(userId, resultAmount, System.currentTimeMillis())
        );

        // when
        UserPoint result = pointService.chargeUserPoint(userId, plusAmount);

        // then
        assertNotNull(result);
        assertEquals(resultAmount, result.point());

        verify(userPointTable).selectById(userId);
        verify(pointHistoryTable).insert(userId, plusAmount,
                TransactionType.CHARGE, result.updateMillis());
    }

    // 기존에 유저가 존재하지 않는 테스트를 작성하였으나, 현실적인 제약사항 분석 후 조건을 변경함
    @Test
    void 포인트_데이터가_존재하지_않으면_포인트가_0인_UserPoint를_반환한다() {
        // given
        long userId = 1L;
        given(userPointTable.selectById(userId)).willReturn(UserPoint.empty(userId));

        // when
        UserPoint userPoint = pointService.getUserPoint(userId);

        // then
        assertEquals(0 , userPoint.point());
    }

    @Test
    void 포인트_조회_요청은_성공한다() {
        // given
        long userId = 1L;
        long amount = 100L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());
        given(userPointTable.selectById(userId)).willReturn(userPoint);

        // when
        UserPoint result = pointService.getUserPoint(userId);

        // then
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals(amount, result.point());

        verify(userPointTable).selectById(userId);
    }

    @Test
    void 포인트_사용_시_0원_이하_일시_요청은_실패한다() {
        // given
        long userId = 1L;
        long amount = 500L;
        long invalidAmount = 0L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        given(userPointTable.selectById(userId)).willReturn(userPoint);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.UseUserPoint(userId, invalidAmount));

        // then
        assertEquals("사용금액이 0원 이하 일 수 없습니다.", exception.getMessage());
    }

    @Test
    void 포인트_사용_시_잔액이_사용금액_보다_작으면_요청은_실패한다() {
        // given
        long userId = 1L;
        long amount = 500L;
        long useAmount = 1000L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        given(userPointTable.selectById(userId)).willReturn(userPoint);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.UseUserPoint(userId, useAmount));

        // then
        assertEquals("잔액이 부족합니다.", exception.getMessage());
    }

    @Test
    void 포인트_사용_시_요청은_성공한다() {
        // given
        long userId = 1L;
        long amount = 1000L;
        long useAmount = 500L;
        long resultAmount = amount - useAmount;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        given(userPointTable.selectById(userId)).willReturn(userPoint);
        given(userPointTable.insertOrUpdate(userId, resultAmount)).willReturn(
                new UserPoint(userId, resultAmount, System.currentTimeMillis())
        );

        // when
        UserPoint updateUserPoint = pointService.UseUserPoint(userId, useAmount);

        // then
        assertNotNull(updateUserPoint);
        assertEquals(resultAmount, updateUserPoint.point());
    }

    @Test
        void 포인트_데이터가_존재하지_않을_시_포인트_내역_요청은_실패한다() {
        // given
        long userId = 1L;

        given(pointHistoryTable.selectAllByUserId(userId)).willReturn(Collections.emptyList());

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.getUserPointHistory(userId));

        // then
        assertEquals("", exception.getMessage());
    }

    @Test
    void 포인트_내역_요청은_성공한다() {
        // given
        long userId = 1L;

        List<PointHistory> pointHistories = List.of(
                new PointHistory(1L, userId, 300L,
                        TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 400L,
                        TransactionType.CHARGE, System.currentTimeMillis())
        );

        given(pointHistoryTable.selectAllByUserId(userId)).willReturn(pointHistories);

        // when
        List<PointHistory> resultList = pointService.getUserPointHistory(userId);

        // then
        assertEquals(2, resultList.size());
        assertEquals(resultList.get(0), pointHistories.get(0));
        assertEquals(resultList.get(1), pointHistories.get(1));
    }
}