package io.javatab.microservices.core.app.status;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    @GetMapping("/ping")
    public ResponseEntity<String> getStatus() {
        log.info("Status v1 invoked");
        return ResponseEntity.ok("pong");
    }
}
