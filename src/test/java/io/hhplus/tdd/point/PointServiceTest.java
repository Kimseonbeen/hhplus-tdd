package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    private static final Logger log = LoggerFactory.getLogger(PointServiceTest.class);

    @Mock
    private UserPointTable userPointTable;

    @InjectMocks
    private PointService target;

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @Test
    void 특정_유저_포인트_조회() {
        long id = 1L;
        UserPoint userPoint = new UserPoint(1L, 1L, 1L);

        //doReturn(userPoint).when(userPointTable).insertOrUpdate(userPoint.id(), userPoint.point());
        doReturn(userPoint).when(userPointTable).selectById(userPoint.id());
        UserPoint result = target.getPoint(id);
        System.out.println("result = " + result);

        //assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    void 특정_유저_포인트_조회_시_유저_없음() {
        long id = 1L;
        UserPoint userPoint = new UserPoint(1L, 1L, 1L);

        //doReturn(userPoint).when(userPointTable).insertOrUpdate(userPoint.id(), userPoint.point());
        doReturn(null).when(userPointTable).selectById(userPoint.id());
        UserPoint result = target.getPoint(id);

        assertThat(result).isNull();
    }

}