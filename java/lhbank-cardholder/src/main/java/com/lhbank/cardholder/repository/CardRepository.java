package com.lhbank.cardholder.repository;

import com.lhbank.cardholder.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByPersonId(String personId);
}
