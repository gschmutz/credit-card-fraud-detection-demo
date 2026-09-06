package com.lhbank.cardholder.eventproducer;

import java.util.List;

import com.lhbank.cardholder.avro.CardHolderState;
import com.lhbank.cardholder.entity.Country;
import com.lhbank.cardholder.entity.Person;
import com.lhbank.cardholder.entity.Address;

public class CardHolderConverter {

    private static com.lhbank.cardholder.avro.Address convertAddress(Address address) {
        return com.lhbank.cardholder.avro.Address.newBuilder()
                .setStreet(address.getStreet())
                .setZipCode(address.getZipCode())
                .setCity(address.getCity())
                .setState(address.getState())
                .build();
    }

    private static com.lhbank.cardholder.avro.Card convertCard(com.lhbank.cardholder.entity.Card card) {
        return com.lhbank.cardholder.avro.Card.newBuilder()
                .setNumber(card.getNumber())
                .setType(card.getType())
                .setExpiryDate(card.getExpiryDate())
                .build();
    }

    public static CardHolderState convert(Person person) {
        List<com.lhbank.cardholder.avro.Address> addresses = person.getAddresses().stream()
                .map(CardHolderConverter::convertAddress)
                .collect(java.util.stream.Collectors.toCollection(() -> new java.util.ArrayList<>()));

        List<com.lhbank.cardholder.avro.Card> cards = person.getCards().stream()
                .map(CardHolderConverter::convertCard)
                .collect(java.util.stream.Collectors.toList());

        return CardHolderState.newBuilder()
                .setCardHolder(com.lhbank.cardholder.avro.CardHolder.newBuilder()
                        .setId(person.getId())
                        .setFirstName(person.getFirstName())
                        .setLastName(person.getLastName())
                        .setEmailAddress(person.getEmailAddress())
                        .setPhoneNumber(person.getPhoneNumber())
                        .setPreferredContact(person.getPreferredContact())
                        .setSegment(person.getSegment())
                        .setCards(cards)
                        .setAvgTransactionAmount(person.getAvgTransactionalAmount())
                        .setUsualCountries(person.getUsualCountries().stream()
                                .map(Country::getName)
                                .collect(java.util.stream.Collectors.toList()))
                        .setAddresses(addresses)
                        .setOnboardedDate(person.getOnboardedDate())
                        .build())
                .build();
    }
}
