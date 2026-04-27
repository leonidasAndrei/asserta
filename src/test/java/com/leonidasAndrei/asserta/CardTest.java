package com.leonidasAndrei.asserta;

import org.junit.Test;
import static org.junit.Assert.*;

public class CardTest {

    @Test
    public void testFullConstructor() {
        Card card = new Card("Hearts", 10, "10");
        assertEquals("Hearts", card.getSuit());
        assertEquals(10, card.getRank());
        assertEquals("10", card.getSymbol());
    }

    @Test
    public void testSymbolToRankMapping() {
        Card card = new Card("Spades", 1, "A");
        // Testing the specific conversion logic
        assertEquals(1, card.symbolToRank("A"));
        assertEquals(11, card.symbolToRank("J"));
        assertEquals(13, card.symbolToRank("K"));
    }

    @Test
    public void testRankToSymbolMapping() {
        Card card = new Card("Diamonds", 1, "A");
        assertEquals("A", card.RankToSymbol(1));
        assertEquals("Q", card.RankToSymbol(12));
    }

    @Test
    public void testConstructorWithSuitAndRank() {
        // Testing constructor that uses rank to determine symbol
        Card card = new Card("Clubs", 12);
        assertEquals("Q", card.getSymbol());
    }

    @Test
    public void testConstructorWithSuitAndSymbol() {
        // Testing constructor that uses symbol to determine rank
        Card card = new Card("Hearts", "K");
        assertEquals(13, card.getRank());
    }

    @Test
    public void testSetters() {
        Card card = new Card("Hearts", 1, "A");
        card.setRank(5);
        card.setSymbol("5");
        assertEquals(5, card.getRank());
        assertEquals("5", card.getSymbol());
    }
}