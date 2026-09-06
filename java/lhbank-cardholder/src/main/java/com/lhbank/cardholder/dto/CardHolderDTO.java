package com.lhbank.cardholder.dto;

import java.util.List;

public record CardHolderDTO(String id, String firstName, String lastName, String emailAddress, String phoneNumber, String preferredContact, String segment, Long avgTransactionAmount, List<String> usualCountries, List<AddressDTO> addresses) {
}

