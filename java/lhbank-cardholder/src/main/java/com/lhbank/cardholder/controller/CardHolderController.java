package com.lhbank.cardholder.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.lhbank.cardholder.dto.CardHolderDTO;
import com.lhbank.cardholder.entity.Person;
import com.lhbank.cardholder.service.CardHolderService;

import java.net.URI;

@RestController
@RequestMapping("/api/cardHolders")
public class CardHolderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardHolderController.class);

    private final CardHolderService cardHolderService;

    public CardHolderController(CardHolderService cardHolderService) {
        this.cardHolderService = cardHolderService;
    }

    @GetMapping("{cardHolderNumber}")
    public ResponseEntity<Person> getCardHolderByNumber(@PathVariable String cardHolderNumber) {
        return ResponseEntity.of(cardHolderService.getCardHolder(cardHolderNumber));
    }

    @PostMapping
    //@Bulkhead(name = "onboardcardHolder", fallbackMethod = "onboardcardHolderError")
    private ResponseEntity<?> onboardCardHolder(@RequestBody CardHolderDTO cardHolder) {

        String cardHolderNumber = cardHolderService.onboardCardHolder(cardHolder);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{cardHolderNumber}").buildAndExpand(cardHolderNumber).toUri();
        return ResponseEntity.created(location).build();
    }

    public ResponseEntity<?> onboardCardHolderError(CardHolderDTO cardHolder, Throwable throwable) {
        LOGGER.error("onboardCardHolderError", throwable);

        return ResponseEntity.status(503).build();
    }
}
