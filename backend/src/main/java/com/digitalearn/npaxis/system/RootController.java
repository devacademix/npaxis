package com.digitalearn.npaxis.system;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class RootController {

    @GetMapping(value = {"/", ""})
    public ResponseEntity<GenericApiResponse<Map<String, String>>> root() {
        log.debug("Root endpoint hit");

        Map<String, String> payload = Map.of(
                "service", "NPaxis Backend",
                "status", "UP",
                "auth", "DISABLED_FOR_DEV",
                "health", "/actuator/health"
        );

        return ResponseHandler.generateResponse(payload, "NPaxis backend is running.", true, HttpStatus.OK);
    }
}
