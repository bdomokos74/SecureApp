package com.bds.secure.secureapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HealthController {
    @GetMapping("/api/health")
    public String check() {
        log.info("health called");
        return "OK";
    }
}
