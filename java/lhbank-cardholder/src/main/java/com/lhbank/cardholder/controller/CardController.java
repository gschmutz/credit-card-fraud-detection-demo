package com.lhbank.cardholder.controller;

import com.lhbank.cardholder.dto.CardDTO;
import com.lhbank.cardholder.entity.Card;
import com.lhbank.cardholder.service.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardController.class);

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<?> addCard(@RequestBody CardDTO cardDTO) {
        try {
            Long cardId = cardService.addCard(cardDTO);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(cardId)
                    .toUri();
            return ResponseEntity.created(location).build();
        } catch (NoSuchElementException e) {
            LOGGER.warn("addCard failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Card>> getCardsByCardHolder(@RequestParam String cardHolderId) {
        return ResponseEntity.ok(cardService.getCardsByCardHolder(cardHolderId));
    }
}
