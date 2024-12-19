package io.hhplus.tdd.point.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.req.PointRequest;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class PointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @Nested
    @DirtiesContext
    @DisplayName("포인트 조회 통합 테스트")
    class UserPointTest {
        @Test
        void 포인트_데이터가_존재하지_않으면_포인트가_0인_UserPoint를_반환한다() throws Exception {
            //given
            long userId = 1L;

            // when
            // then
            mockMvc.perform(get("/point/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.point").value(0));
        }

        @Test
        void 포인트_조회_성공한다() throws Exception {
            // given
            long userId = 2L;
            long amount = 100L;

            // when
            UserPoint userPoint = userPointTable.insertOrUpdate(userId, amount);

            // then
            mockMvc.perform(get("/point/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userPoint.id()))
                    .andExpect(jsonPath("$.point").value(userPoint.point()));
        }
    }

    @Nested
    @DirtiesContext
    @DisplayName("포인트 충전 통합 테스트")
    class UserChargePointTest {
        @Test
        void 포인트_충전_시_충전금액은_0원_이하_일시_요청은_실패한다() throws Exception {
            //given
            long userId = 3L;
            long amount = 100L;
            long plusAmount = 0L;

            // when
            userPointTable.insertOrUpdate(userId, amount);
            PointRequest pointRequest = new PointRequest(userId, plusAmount);

            // ObjectMapper를 사용하여 DTO를 JSON으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = objectMapper.writeValueAsString(pointRequest);

            // then
            mockMvc.perform(patch("/point/{id}/charge", userId)
                    .contentType("application/json")
                    .content(jsonContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400"))  // ErrorResponse 코드가 "400"이어야 함
                    .andExpect(jsonPath("$.message").value("충전금액은 0원 이하 일 수 없습니다."));  // 예시로 지정한 오류 메시지 확인
        }

        @Test
        void 포인트_충전_시_결과값이_1_000_000원_이상일_경우_요청은_실패한다() throws Exception {
            //given
            long userId = 4L;
            long amount = 100L;
            long plusAmount = 1_000_000L;

            // when
            userPointTable.insertOrUpdate(userId, amount);
            PointRequest pointRequest = new PointRequest(userId, plusAmount);

            // ObjectMapper를 사용하여 DTO를 JSON으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = objectMapper.writeValueAsString(pointRequest);

            // then
            mockMvc.perform(patch("/point/{id}/charge", userId)
                            .contentType("application/json")
                            .content(jsonContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400"))
                    .andExpect(jsonPath("$.message").value("충전 결과값이 1_000_000원을 넘을 수 없습니다."));
        }

        @Test
        void 포인트_충전_시_요청은_성공한다() throws Exception {
            //given
            long userId = 5L;
            long amount = 100L;
            long plusAmount = 200L;
            long resultAmount = amount + plusAmount;

            // when
            userPointTable.insertOrUpdate(userId, amount);
            PointRequest pointRequest = new PointRequest(userId, plusAmount);

            // ObjectMapper를 사용하여 DTO를 JSON으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = objectMapper.writeValueAsString(pointRequest);

            // then
            mockMvc.perform(patch("/point/{id}/charge", userId)
                            .contentType("application/json")
                            .content(jsonContent))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.point").value(resultAmount));
        }
    }

    @Nested
    @DirtiesContext
    @DisplayName("포인트 사용 통합 테스트")
    class UserUsePointTest {
        @Test
        void 포인트_사용_금액이_0원_이하_이면_요청은_실패한다() throws Exception {
            //given
            long userId = 6L;
            long amount = 100L;
            long useAmount = 0L;

            // when
            userPointTable.insertOrUpdate(userId, amount);
            PointRequest pointRequest = new PointRequest(userId, useAmount);

            // ObjectMapper를 사용하여 DTO를 JSON으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = objectMapper.writeValueAsString(pointRequest);

            // then
            mockMvc.perform(patch("/point/{id}/use", userId)
                            .contentType("application/json")
                            .content(jsonContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400"))
                    .andExpect(jsonPath("$.message").value("사용금액이 0원 이하 일 수 없습니다."));
        }

        @Test
        void 포인트_사용_시_잔액이_사용금액_보다_작으면_요청은_실패한다() throws Exception {
            //given
            long userId = 7L;
            long amount = 500L;
            long useAmount = 1000L;

            // when
            userPointTable.insertOrUpdate(userId, amount);
            PointRequest pointRequest = new PointRequest(userId, useAmount);

            // ObjectMapper를 사용하여 DTO를 JSON으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = objectMapper.writeValueAsString(pointRequest);

            // then
            mockMvc.perform(patch("/point/{id}/use", userId)
                            .contentType("application/json")
                            .content(jsonContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400"))
                    .andExpect(jsonPath("$.message").value("잔액이 부족합니다."));
        }

        @Test
        void 포인트_사용_시_요청은_성공한다() throws Exception {
            //given
            long userId = 8L;
            long amount = 1000L;
            long useAmount = 500L;
            long resultAmount = amount - useAmount;

            // when
            userPointTable.insertOrUpdate(userId, amount);
            PointRequest pointRequest = new PointRequest(userId, useAmount);

            // ObjectMapper를 사용하여 DTO를 JSON으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = objectMapper.writeValueAsString(pointRequest);

            // then
            mockMvc.perform(patch("/point/{id}/use", userId)
                            .contentType("application/json")
                            .content(jsonContent))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.point").value(resultAmount));
        }
    }

    @Nested
    @DirtiesContext
    @DisplayName("포인트 충전, 사용 내역 통합 테스트")
    class PointHistoryTest {
        @Test
        void 포인트_데이터가_존재하지_않을_시_포인트_내역_요청은_실패한다() throws Exception {
            //given
            long userId = 9L;

            // when
            // then
            mockMvc.perform(get("/point/{id}/histories", userId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400"))
                    .andExpect(jsonPath("$.message").value("포인트 내역 결과가 없습니다."));
        }

        @Test
        void 충전_1회_사용_1회_시_각각_내역_조회가_성공한다() throws Exception {
            //given
            long userId = 10L;
            long amount = 100L;
            long plusAmount = 10_000L;
            long useAmount = 1_000L;

            // when
            userPointTable.insertOrUpdate(userId, amount);
            pointService.chargeUserPoint(userId, plusAmount);
            pointService.UseUserPoint(userId, useAmount);

            // then
            mockMvc.perform(get("/point/{id}/histories", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].userId").value(userId))
                    .andExpect(jsonPath("$[0].amount").value(plusAmount))
                    .andExpect(jsonPath("$[0].type").value(TransactionType.CHARGE.toString()))
                    .andExpect(jsonPath("$[1].userId").value(userId))
                    .andExpect(jsonPath("$[1].amount").value(useAmount))
                    .andExpect(jsonPath("$[1].type").value(TransactionType.USE.toString()));
        }
    }
}
