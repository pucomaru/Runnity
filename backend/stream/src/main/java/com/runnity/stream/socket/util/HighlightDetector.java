package com.runnity.stream.socket.util;

import com.runnity.stream.socket.dto.ChallengeStreamMessage;
import com.runnity.stream.socket.dto.HighlightEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 하이라이트 이벤트 감지 전담 컴포넌트
 * BroadcastService에서 호출해서 사용.
 */
@Component
@Slf4j
public class HighlightDetector {

    // 재접속 허용 사유 (참고용)
    private static final Set<String> RECONNECT_REASONS =
            Set.of("TIMEOUT", "DISCONNECTED", "ERROR");

    public HighlightEventDto detect(
            ChallengeStreamMessage msg,
            Map<String, Object> prevRunner,
            Long prevRankOwnerRunnerId,
            double totalDistance) {

        if (msg.getRanking() == null || msg.getDistance() == null) {
            return null;
        }

        // 이전 데이터 없으면(첫 프레임) 이벤트 발생 X
        if (prevRunner == null) {
            return null;
        }

        Double prevDist = castDouble(prevRunner.get("distance"));
        Double prevPace = castDouble(prevRunner.get("pace"));
        Integer prevRank = castInt(prevRunner.get("ranking"));

        double dist = msg.getDistance();
        double pace = msg.getPace() != null ? msg.getPace() : 0.0;
        int rank = msg.getRanking();

        // 1. 완주
        if (totalDistance > 0 && dist >= totalDistance) {
            return base(msg).highlightType("FINISH").build();
        }

        // 2. 완주 임박 (95% 이상 선을 처음 넘을 때)
        if (totalDistance > 0
                && dist >= totalDistance * 0.95
                && (prevDist == null || prevDist < totalDistance * 0.95)) {
            return base(msg).highlightType("ALMOST_FINISH").build();
        }

        // 3. 역전 (랭크가 좋아지고, 기존 그 랭크의 소유자가 다른 러너였을 때)
        if (prevRank != null && rank < prevRank) {
            if (prevRankOwnerRunnerId != null && !prevRankOwnerRunnerId.equals(msg.getRunnerId())) {
                return base(msg)
                        .highlightType("OVERTAKE")
                        .targetRunnerId(prevRankOwnerRunnerId)
                        .build();
            }
        }

        // 4. TOP3 진입
        if (prevRank != null && prevRank > 3 && rank <= 3) {
            return base(msg).highlightType("TOP3_ENTRY").build();
        }

        // 5. 속도 저하
        if (prevPace != null && pace > 0) {
            if (pace >= prevPace * 1.2 && pace >= 7.0) {
                return base(msg).highlightType("SLOW_DOWN").build();
            }
        }

        // leave, 재접속 관련은 필요하면 별도 타입으로 확장
        return null;
    }

    private HighlightEventDto.HighlightEventDtoBuilder base(ChallengeStreamMessage msg) {
        return HighlightEventDto.builder()
                .challengeId(msg.getChallengeId())
                .runnerId(msg.getRunnerId())
                .nickname(msg.getNickname())
                .profileImage(msg.getProfileImage())
                .distance(msg.getDistance())
                .pace(msg.getPace())
                .ranking(msg.getRanking());
    }

    private Double castDouble(Object o) {
        if (o == null) return null;
        return Double.parseDouble(o.toString());
    }

    private Integer castInt(Object o) {
        if (o == null) return null;
        return Integer.parseInt(o.toString());
    }

}
