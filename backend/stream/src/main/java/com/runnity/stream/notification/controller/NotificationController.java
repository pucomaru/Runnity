package com.runnity.stream.notification.controller;

import com.runnity.stream.notification.service.FcmSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcm")
public class NotificationController {

    private final FcmSenderService fcmSenderService;

    @PostMapping("/test")
    public String sendTest(@RequestParam String token){
        fcmSenderService.sendNotification(token, "fcm 테스트", "테스트 성공");
        return "ok";
    }
}
