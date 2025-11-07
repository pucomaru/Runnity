package com.runnity.history.service;

import com.runnity.history.domain.RunRecord;
import com.runnity.history.domain.RunRecordType;
import com.runnity.history.dto.response.PeriodStatResponse;
import com.runnity.history.dto.response.RunRecordResponse;
import com.runnity.history.dto.response.StatsSummaryResponse;
import com.runnity.history.repository.RunRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final RunRecordRepository runRecordRepository;

    public StatsSummaryResponse getSummary(Long memberId, LocalDateTime startDate, LocalDateTime endDate, String period) {

        List<RunRecord> records = runRecordRepository.findByMemberIdAndPeriod(memberId, startDate, endDate);

        List<String> labels = generatePeriodLabels(period);
        Map<String, PeriodStatResponse> statsMap = labels.stream()
                .collect(Collectors.toMap(
                        label -> label,
                        label -> PeriodStatResponse.builder()
                                .label(label)
                                .distance(0.0f)
                                .time(0)
                                .pace(0)
                                .count(0)
                                .build(),
                        (a, b) -> a, LinkedHashMap::new
                ));

        if (!records.isEmpty()) {
            statsMap = calculatePeriodStats(records, statsMap, period);
        }

        float totalDistance = records.stream()
                .map(RunRecord::getDistance)
                .reduce(0.0f, Float::sum);

        int totalTime = records.stream()
                .map(RunRecord::getDurationSec)
                .reduce(0, Integer::sum);

        int avgPace = (int) records.stream()
                .map(RunRecord::getPace)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        int totalRunDays = (int) records.stream()
                .map(r -> r.getStartAt().toLocalDate())
                .distinct()
                .count();

        List<RunRecordResponse> personals = runRecordRepository
                .findTop5ByMember_MemberIdAndRunTypeOrderByStartAtDesc(memberId, RunRecordType.PERSONAL)
                .stream()
                .map(RunRecordResponse::from)
                .toList();

        List<RunRecordResponse> challenges = runRecordRepository
                .findTop5ByMember_MemberIdAndRunTypeOrderByStartAtDesc(memberId, RunRecordType.CHALLENGE)
                .stream()
                .map(RunRecordResponse::from)
                .toList();


        return StatsSummaryResponse.builder()
                .totalDistance(totalDistance)
                .totalTime(totalTime)
                .avgPace(avgPace)
                .totalRunDays(totalRunDays)
                .periodStats(new ArrayList<>(statsMap.values()))
                .personals(personals)
                .challenges(challenges)
                .build();
    }

    private List<String> generatePeriodLabels(String period) {
        return switch (period.toLowerCase()) {
            case "week" -> Arrays.stream(DayOfWeek.values())
                    .map(d -> d.getDisplayName(TextStyle.SHORT, Locale.KOREAN))
                    .toList();

            case "month" -> {
                int days = LocalDate.now().lengthOfMonth();
                List<String> list = new ArrayList<>();
                for (int i = 1; i <= days; i++) list.add(i + "일");
                yield list;
            }

            case "year" -> {
                List<String> list = new ArrayList<>();
                for (int i = 1; i <= 12; i++) list.add(i + "월");
                yield list;
            }

            case "all" -> {
                int currentYear = LocalDate.now().getYear();
                List<String> list = new ArrayList<>();
                for (int y = currentYear - 5; y <= currentYear; y++) list.add(y + "년");
                yield list;
            }

            default -> List.of();
        };
    }

    private String getLabelForRecord(RunRecord record, String period) {
        return switch (period.toLowerCase()) {
            case "week" -> record.getStartAt().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            case "month" -> record.getStartAt().getDayOfMonth() + "일";
            case "year" -> record.getStartAt().getMonthValue() + "월";
            case "all" -> record.getStartAt().getYear() + "년";
            default -> null;
        };
    }

    private Map<String, PeriodStatResponse> calculatePeriodStats(List<RunRecord> records, Map<String, PeriodStatResponse> statsMap, String period) {

        for (RunRecord record : records) {
            String label = getLabelForRecord(record, period);
            if (!statsMap.containsKey(label)) continue;

            PeriodStatResponse current = statsMap.get(label);

            float newDistance = current.distance() + record.getDistance();
            int newTime = current.time() + record.getDurationSec();
            int newCount = current.count() + 1;
            int newPaceSum = current.pace() + record.getPace(); // ⚙️ pace 누적 합계로 사용

            statsMap.put(label, PeriodStatResponse.builder()
                    .label(label)
                    .distance(newDistance)
                    .time(newTime)
                    .pace(newPaceSum)
                    .count(newCount)
                    .build());
        }

        statsMap.replaceAll((label, cur) -> {
            int avgPace = (int) (cur.count() > 0 ? cur.pace() / cur.count() : 0);
            return PeriodStatResponse.builder()
                    .label(label)
                    .distance(cur.distance())
                    .time(cur.time())
                    .pace(avgPace)
                    .count(cur.count())
                    .build();
        });

        return statsMap;
    }
}
