package com.leonidasAndrei.asserta;

import com.leonidasAndrei.asserta.model.Card;
import org.junit.Test;
import static org.junit.Assert.*;
import com.leonidasAndrei.asserta.model.Card.*;

public class CardTest {
/*
    @Test
    public void testFullConstructor() {
        Card card = new Card(Suit.HEARTS, 10, "10");
        assertEquals(Card.Suit.HEARTS, card.getSuit());
        assertEquals(10, card.getRank());
        assertEquals("10", card.getSymbol());
    }

    @Test
    public void testSymbolToRankMapping() {
        Card card = new Card(Suit.SPADES, 1, "A");
        // Testing the specific conversion logic
        assertEquals(1, card.symbolToRank("A"));
        assertEquals(11, card.symbolToRank("J"));
        assertEquals(13, card.symbolToRank("K"));
    }

    @Test
    public void testRankToSymbolMapping() {
        Card card = new Card(Suit.DIAMONDS, 1, "A");
        assertEquals("A", card.rankToSymbol(1));
        assertEquals("Q", card.rankToSymbol(12));
    }*/

    @Test
    public void testConstructorWithSuitAndRank() {
        // Testing constructor that uses rank to determine symbol
        Card card = new Card(Suit.CLUBS, 12);
        assertEquals("Q", card.getSymbol());
    }

    @Test
    public void testConstructorWithSuitAndSymbol() {
        // Testing constructor that uses symbol to determine rank
        Card card = new Card(Suit.HEARTS, "K");
        assertEquals(13, card.getRank());
    }
/*
    @Test
    public void testSetters() {
        Card card = new Card(Suit.HEARTS, 1, "A");
        card.setRank(5);
        card.setSymbol("5");
        assertEquals(5, card.getRank());
        assertEquals("5", card.getSymbol());
    }

    @Test
    public void testBoundaryRanks() {
        Card lowCard = new Card(Suit.HEARTS, 1, "A");
        Card highCard = new Card(Suit.HEARTS, 13, "K");

        assertEquals(1, lowCard.getRank());
        assertEquals(13, highCard.getRank());
    }*/

    @Test
    public void testNullSuitThrowsException() {
        assertThrows(NullPointerException.class, () -> new Card(null, 5));
    }

    @Test
    public void testInvalidRankThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Card(Suit.DIAMONDS, 0));
        assertThrows(IllegalArgumentException.class, () -> new Card(Suit.DIAMONDS, 14));
    }
/*

    @Test
    public void testMismatchThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Card(Suit.SPADES, 5, "K"));
    }
*/

    @Test
    public void testInvalidSymbolThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Card(Suit.SPADES, "1"));
        assertThrows(IllegalArgumentException.class, () -> new Card(Suit.SPADES, "Z"));
    }

    @Test
    public void testSettersValidation() {
        Card card = new Card(Suit.SPADES, 5);
        assertThrows(IllegalArgumentException.class, () -> card.setRank(20));
    }
}