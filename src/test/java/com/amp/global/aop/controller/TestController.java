package com.amp.global.aop.controller;

import com.amp.global.annotation.LogExecutionTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/success")
    public String success() {
        return "ok";
    }

    @GetMapping("/error")
    public void error() {
        throw new RuntimeException("Test Exception");
    }

    @LogExecutionTime("느린 작업 테스트")
    @GetMapping("/slow")
    public String slow() throws InterruptedException {
        Thread.sleep(500); // 0.5초 대기
        return "slow ok";
    }
}