package com.lhbank.cardholder.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import com.lhbank.cardholder.entity.Country;

@Entity
public class Person extends Auditable {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private String preferredContact;
    private String segment;
    @OneToOne(cascade = CascadeType.ALL)
    private Card card;
    private Long avgTransactionalAmount;

    @Column(nullable = false, updatable = false)
    private Instant onboardedDate;

    @OneToMany(
        mappedBy = "person",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(
        mappedBy = "person",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Country> usualCountries = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPreferredContact() {
        return preferredContact;
    }

    public void setPreferredContact(String preferredContact) {
        this.preferredContact = preferredContact;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Long getAvgTransactionalAmount() {
        return avgTransactionalAmount;
    }

    public void setAvgTransactionalAmount(Long avgTransactionalAmount) {
        this.avgTransactionalAmount = avgTransactionalAmount;
    }

    public Instant getOnboardedDate() {
        return onboardedDate;
    }

    public void setOnboardedDate(Instant onboardedDate) {
        this.onboardedDate = onboardedDate;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Country> getUsualCountries() {
        return usualCountries;
    }

    public void setUsualCountries(List<Country> usualCountries) {
        this.usualCountries = usualCountries;
    }

    // helper methods (VERY good practice)
    public void addAddress(Address address) {
        addresses.add(address);
        address.setPerson(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setPerson(null);
    }

    // helpers for usualCountries - now work with Country entities
    public void addUsualCountry(Country country) {
        if (this.usualCountries == null) this.usualCountries = new ArrayList<>();
        this.usualCountries.add(country);
        country.setPerson(this);
    }

    public void removeUsualCountry(Country country) {
        if (this.usualCountries != null) {
            this.usualCountries.remove(country);
            country.setPerson(null);
        }
    }
}
