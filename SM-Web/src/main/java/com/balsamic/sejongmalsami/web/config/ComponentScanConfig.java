package com.balsamic.sejongmalsami.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

// 멀티모듈 컴포넌트 스캔 설정
@Configuration
@ComponentScan(basePackages = {
    "com.balsamic.sejongmalsami",
    "com.balsamic.sejongmalsami.web",
    "com.balsamic.sejongmalsami.application",
    "com.balsamic.sejongmalsami.member",
    "com.balsamic.sejongmalsami.post",
    "com.balsamic.sejongmalsami.notice",
    "com.balsamic.sejongmalsami.auth",
    "com.balsamic.sejongmalsami.academic",
    "com.balsamic.sejongmalsami.ai"
})
public class ComponentScanConfig {
} 