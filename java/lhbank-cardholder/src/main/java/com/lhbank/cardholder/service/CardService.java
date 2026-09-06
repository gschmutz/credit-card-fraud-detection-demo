package com.lhbank.cardholder.service;

import com.lhbank.cardholder.dto.CardDTO;
import com.lhbank.cardholder.entity.Card;
import com.lhbank.cardholder.entity.Person;
import com.lhbank.cardholder.repository.CardHolderRepository;
import com.lhbank.cardholder.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;
    private final CardHolderRepository cardHolderRepository;

    public CardService(CardRepository cardRepository, CardHolderRepository cardHolderRepository) {
        this.cardRepository = cardRepository;
        this.cardHolderRepository = cardHolderRepository;
    }

    public Long addCard(CardDTO cardDTO) {
        LOGGER.info("addCard(cardHolderId={})", cardDTO.cardHolderId());

        Person person = cardHolderRepository.findById(cardDTO.cardHolderId())
                .orElseThrow(() -> new NoSuchElementException("CardHolder not found: " + cardDTO.cardHolderId()));

        var card = new Card();
        card.setNumber(cardDTO.number());
        card.setType(cardDTO.type());
        card.setExpiryDate(cardDTO.expiryDate());
        person.addCard(card);

        cardRepository.save(card);
        return card.getId();
    }

    public List<Card> getCardsByCardHolder(String cardHolderId) {
        LOGGER.info("getCardsByCardHolder({})", cardHolderId);
        return cardRepository.findByPersonId(cardHolderId);
    }
}
