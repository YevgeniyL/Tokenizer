package com.st.source.controller;

import com.st.source.api.AuthRequest;
import com.st.source.api.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class SourceController {
    private static final Logger logger = LoggerFactory.getLogger(SourceController.class);

    private final KafkaTemplate<String, AuthRequest> kafkaTemplate;

    @Value("${kafka.configuration.topic.input.name}")
    private String inputTopic;

    @Autowired
    public SourceController(KafkaTemplate<String, AuthRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping(path = "/api/auth", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    private ResponseEntity<AuthResponse> auth(@RequestBody AuthRequest authRequest) {
        //here we can validate 'authRequest' on what we need

        String transactionId = UUID.randomUUID().toString();
        kafkaTemplate.send(inputTopic, transactionId, authRequest);
        logger.info("To  topic sended message to topic '" + inputTopic + "' with key (transactionId): " + transactionId);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(new AuthResponse(true, transactionId));
    }
}
