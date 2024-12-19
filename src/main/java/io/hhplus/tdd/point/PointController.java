package io.hhplus.tdd.point;

import io.hhplus.tdd.dto.req.PointRequest;
import io.hhplus.tdd.dto.res.PointHistoryResponse;
import io.hhplus.tdd.dto.res.PointResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public PointResponse point(
            @PathVariable(name = "id") long id
    ) {
        UserPoint userPoint = pointService.getUserPoint(id);

        return PointResponse.from(userPoint);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistoryResponse> history(
            @PathVariable long id
    ) {
        List<PointHistory> userPointHistory = pointService.getUserPointHistory(id);

        return userPointHistory.stream()
                .map(PointHistoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public PointResponse charge(
            @PathVariable(name = "id") long id,
            @RequestBody PointRequest pointRequest
            ) {
        UserPoint userPoint = pointService.chargeUserPoint(id, pointRequest.amount());

        return PointResponse.from(userPoint);
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public PointResponse use(
            @PathVariable(name = "id") long id,
            @RequestBody PointRequest pointRequest
    ) {
        UserPoint userPoint = pointService.UseUserPoint(id, pointRequest.amount());

        return PointResponse.from(userPoint);
    }
}
