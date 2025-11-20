package com.runnity.notification.service;

import com.runnity.global.exception.GlobalException;
import com.runnity.global.status.ErrorStatus;
import com.runnity.member.domain.Member;
import com.runnity.member.repository.MemberRepository;
import com.runnity.notification.domain.MemberFcmToken;
import com.runnity.notification.dto.request.FcmTokenDeleteRequest;
import com.runnity.notification.dto.request.FcmTokenRegisterRequest;
import com.runnity.notification.repository.MemberFcmTokenRepository;
import com.runnity.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmTokenService {

    private final MemberFcmTokenRepository memberFcmTokenRepository;
    private final MemberRepository memberRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;

    @Transactional
    public void resiterToken(Long memberId, FcmTokenRegisterRequest request){
        String token = request.token();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.MEMBER_NOT_FOUND));

        if (memberFcmTokenRepository.findByMember_MemberIdAndTokenAndIsDeletedFalse(memberId, token).isPresent()) {
            log.debug("[FCM] 이미 내 활성 토큰 존재");
            return;
        }

        var anyTokenOpt = memberFcmTokenRepository.findByToken(token);
        if (anyTokenOpt.isEmpty()) {
            memberFcmTokenRepository.save(
                    MemberFcmToken.builder()
                            .member(member)
                            .token(token)
                            .build()
            );
            log.info("[FCM] 새 토큰 등록");
            return;
        }

        MemberFcmToken tokenRow = anyTokenOpt.get();
        Long ownerId = tokenRow.getMember().getMemberId();

        if (ownerId.equals(memberId)) {
            tokenRow.recover();
        } else {
            tokenRow.delete();
            MemberFcmToken memberFcmToken = MemberFcmToken.builder()
                    .member(member)
                    .token(token)
                    .build();
            memberFcmTokenRepository.save(memberFcmToken);
            log.info("[FCM] 토큰 소유자 이전");
        }
    }

    @Transactional
    public void deleteToken(Long memberId, FcmTokenDeleteRequest request) {
        String token = request.token();

        var tokenOpt = memberFcmTokenRepository.findByMember_MemberIdAndTokenAndIsDeletedFalse(memberId, token);
        if (tokenOpt.isEmpty()) {
            throw new GlobalException(ErrorStatus.FCM_TOKEN_NOT_FOUND);
        }

        MemberFcmToken tokenEntity = tokenOpt.get();
        tokenEntity.delete();
        memberFcmTokenRepository.save(tokenEntity);
        log.info("[FCM] FCM 토큰 비활성화 완료");
    }

    @Transactional
    public void createTestOutboxRow() {
        notificationOutboxRepository.insertTestRow();
    }
}
