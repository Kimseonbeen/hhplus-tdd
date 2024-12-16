package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    private static final Logger log = LoggerFactory.getLogger(PointServiceTest.class);

    @Mock
    private UserPointTable userPointTable;

    @InjectMocks
    private PointService pointService;

    @Test
    void 포인트_충전_시_결과값이_1_000_000원_이상일_경우_요청은_실패한다() {
        long userId = 1L;
        long amount = 0L;
        long invalidAmount = 1_000_000;

        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // 예외가 발생하는지 확인
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.chargeUserPoint(userId, invalidAmount));

        // 예외 메시지 검증
        assertEquals("충전 결과값이 1_000_000원을 넘을 수 없습니다.", exception.getMessage());

        // 메서드가 정확한 인자로 호출되었는지 검증
       verify(pointService).chargeUserPoint(userId, invalidAmount);
    }

    @Test
    void 포인트_충전_시_충전금액은_음수_일_시_요청은_실패한다() {
        long userId = 1L;
        long invalidAmount = -1L;
        
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.chargeUserPoint(userId, invalidAmount));
        
        assertEquals("충전금액은 음수 일 수 없습니다.", exception.getMessage());
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

        // selectById 메서드가 호출되었는지 검증
        verify(userPointTable).selectById(userId);
    }
}