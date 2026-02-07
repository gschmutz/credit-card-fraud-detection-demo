package com.lhbank.cardholder.service;

import com.lhbank.cardholder.entity.Card;
import com.lhbank.cardholder.entity.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lhbank.cardholder.dto.CardHolderDTO;
import com.lhbank.cardholder.dto.AddressDTO;
import com.lhbank.cardholder.entity.Address;
import com.lhbank.cardholder.entity.Person;
import com.lhbank.cardholder.eventproducer.CardHolderStateProducer;
import com.lhbank.cardholder.repository.CardHolderRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class CardHolderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardHolderService.class);

    private final CardHolderRepository cardHolderRepository;

    // GuS: added
    private CardHolderStateProducer cardHolderStateProducer;

    public CardHolderService(CardHolderRepository cardHolderRepository, CardHolderStateProducer cardHolderStateProducer) {
        this.cardHolderRepository = cardHolderRepository;
        this.cardHolderStateProducer = cardHolderStateProducer;
    }

    /**
     * Get's the cardHolder by cardHolder Number
     *
     * @param cardHolderNumber The number
     * @return The cardHolder if exists
     */
    public Optional<Person> getCardHolder(String cardHolderNumber) {
        LOGGER.info("getCardHolder({})", cardHolderNumber);

        return cardHolderRepository.findById(cardHolderNumber);
    }

    private Address convert(AddressDTO addressDTO) {
        var address = new Address();
        address.setStreet(addressDTO.street());
        address.setZipCode(addressDTO.zipCode());
        address.setCity(addressDTO.city());
        address.setState(addressDTO.state());
        return address;
    }

    /**
     * Onboard's a new cardHolder, given by the cardHolderDTO parameter.
     * @param cardHolderDTO       the cardHolder object to add
     * @return
     */
    public String onboardCardHolder (CardHolderDTO cardHolderDTO) {
        LOGGER.info("onboardCardHolder({})", cardHolderDTO);
        // build the person
        var person = new Person();
        // assign an id (was previously null which caused Hibernate to error on persist)
        person.setId(UUID.randomUUID().toString());
        person.setFirstName(cardHolderDTO.firstName());
        person.setLastName(cardHolderDTO.lastName());
        person.setEmailAddress(cardHolderDTO.emailAddress());
        person.setPhoneNumber(cardHolderDTO.phoneNumber());
        person.setPreferredContact(cardHolderDTO.preferredContact());
        person.setSegment(cardHolderDTO.segment());

        var card = new Card();
        card.setNumber(cardHolderDTO.card().number());
        card.setType(cardHolderDTO.card().type());
        card.setExpiryDate(cardHolderDTO.card().expiryDate());
        person.setCard(card);

        person.setAvgTransactionalAmount(cardHolderDTO.avgTransactionAmount());

        // attach addresses and ensure the bidirectional relationship is set
        for (var addressDTO : cardHolderDTO.addresses()) {
            var address = convert(addressDTO);
            person.addAddress(address);
        }
        person.setUsualCountries(cardHolderDTO.usualCountries().stream()
                .map(name -> {
                    var country = new Country();
                    country.setName(name);
                    country.setPerson(person);
                    return country;
                })
                .collect(java.util.stream.Collectors.toList()));

        person.setOnboardedDate(Instant.now());

        cardHolderRepository.saveAndFlush(person);

        //Publish the event
        cardHolderStateProducer.send(person);

        return person.getId();
    }
}
