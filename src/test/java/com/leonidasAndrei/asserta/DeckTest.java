package com.leonidasAndrei.asserta;

import com.leonidasAndrei.asserta.model.Card;
import com.leonidasAndrei.asserta.model.Deck;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class DeckTest {

    @Test
    public void testDefaultConstructorFailsDueToJokers() {
        try {
            new Deck();
        } catch (Exception e) {
            System.out.println("Caught expected crash: " + e.getMessage());
            return;
        }
        fail("Default constructor should have crashed due to Joker validation logic");
    }

    @Test
    public void testStandardDeckSize() {
        Deck deck = new Deck(1);
        assertEquals(52, deck.getDeck().size());
    }

    @Test
    public void testFullDeckSize() {
        try {
            new Deck(0);
        } catch (Exception e) {
            return;
        }
        fail("Full deck constructor should crash on Joker creation");
    }

    @Test
    public void testEncapsulationLeak() {
        Deck deck = new Deck(1);
        List<Card> cards = deck.getDeck();
        cards.clear(); // Malicious act
        assertEquals(0, deck.getDeck().size());
    }

    @Test
    public void testStandardDeckConstruction() {
        Deck deck = new Deck(1); // Type 1: Standard
        assertEquals("Standard deck should have 52 cards", 52, deck.size());
        assertFalse("Deck should not be empty initially", deck.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDeckTypeThrowsException() {
        new Deck(99); // Invalid type
    }

    @Test
    public void testDealCard() {
        Deck deck = new Deck(1);
        Card dealtCard = deck.deal();
        assertNotNull(dealtCard);
        assertEquals(51, deck.size());
    }

    @Test(expected = IllegalStateException.class)
    public void testDealFromEmptyDeck() {
        Deck deck = new Deck(1);
        for (int i = 0; i < 52; i++) {
            deck.deal();
        }
        deck.deal();
    }

    @Test
    public void testJokerCreationExpectation() {
        try {
            new Deck(0);
        } catch (Exception e) {
            System.out.println("Deck(0) failed as expected due to Joker logic: " + e.getMessage());
            return;
        }
        fail("Deck(0) should have failed due to Joker constraints in Card class");
    }
}
