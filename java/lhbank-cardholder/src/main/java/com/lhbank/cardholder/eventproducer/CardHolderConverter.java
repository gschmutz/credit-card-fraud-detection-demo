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

    public static CardHolderState convert(Person person) {
        List<com.lhbank.cardholder.avro.Address> addresses = person.getAddresses().stream()
                .map(CardHolderConverter::convertAddress)
                .collect(java.util.stream.Collectors.toCollection(() -> new java.util.ArrayList<>()));

        com.lhbank.cardholder.avro.Card card = com.lhbank.cardholder.avro.Card.newBuilder()
                .setNumber(person.getCard().getNumber())
                .setType(person.getCard().getType())
                .setExpiryDate(person.getCard().getExpiryDate())
                .build();

        return CardHolderState.newBuilder()
                .setCardHolder(com.lhbank.cardholder.avro.CardHolder.newBuilder()
                        .setId(person.getId())
                        .setFirstName(person.getFirstName())
                        .setLastName(person.getLastName())
                        .setEmailAddress(person.getEmailAddress())
                        .setPhoneNumber(person.getPhoneNumber())
                        .setPreferredContact(person.getPreferredContact())
                        .setSegment(person.getSegment())
                        .setCard(card)
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
