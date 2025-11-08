    package com.runnity.history.controller;

    import com.runnity.global.response.ApiResponse;
    import com.runnity.global.status.SuccessStatus;
    import com.runnity.history.dto.response.MyChallengesResponse;
    import com.runnity.history.dto.response.RunRecordDetailResponse;
    import com.runnity.history.dto.response.RunRecordMonthlyResponse;
    import com.runnity.history.service.HistoryService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/api/v1/me")
    @RequiredArgsConstructor
    public class HistoryController {

        private final HistoryService service;

        // TODO: 추후 jwt에서 memberid 가져오게 변경 필요
        @GetMapping("/challenges")
        public ResponseEntity<ApiResponse<MyChallengesResponse>> getMyChallenges(){
            long memberId = 1;
            MyChallengesResponse response = service.getMyChallenges(memberId);

            return ApiResponse.success(SuccessStatus.OK, response);
        }

        // TODO: 추후 jwt에서 memberid 가져오게 변경 필요
        @GetMapping("/runs/{runRecordId}")
        public ResponseEntity<ApiResponse<RunRecordDetailResponse>> getRunRecordDetail(
                @PathVariable Long runRecordId
        ) {
            long memberId = 1L;

            RunRecordDetailResponse response = service.getRunRecordDetail(memberId, runRecordId);
            return ApiResponse.success(SuccessStatus.OK, response);
        }

        // TODO: 추후 jwt에서 memberid 가져오게 변경 필요
        @GetMapping("/runs")
        public ResponseEntity<ApiResponse<RunRecordMonthlyResponse>> getRunRecordsByMonth(
                @RequestParam int year,
                @RequestParam int month
        ) {
            long memberId = 1L;

            RunRecordMonthlyResponse response =
                    service.getRunRecordsByMonth(memberId, year, month);

            return ApiResponse.success(SuccessStatus.OK, response);
        }
    }
