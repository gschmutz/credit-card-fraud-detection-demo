package com.lhbank.cardholder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lhbank.cardholder.entity.Person;

public interface CardHolderRepository extends JpaRepository<Person, String> {
}
