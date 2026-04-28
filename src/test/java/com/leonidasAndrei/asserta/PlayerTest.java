package com.leonidasAndrei.asserta;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.List;

public class PlayerTest {

    private Player player;
    private Card aceOfSpades;

    @Before
    public void setup() {
        player = new Player("Leonidas", 1, true);
        aceOfSpades = new Card(Card.Suit.SPADES, 1);
    }

    @Test
    public void testUsernameAssignment() {
        Player p1 = new Player(null, 2, false);
        assertEquals("AI_2", p1.getUsername()); // Assuming a getter exists

        Player p2 = new Player("", 3, true);
        assertEquals("Player_3", p2.getUsername());
    }

    @Test
    public void testAddAndRemoveCard() {
        assertTrue(player.addCard(aceOfSpades));
        assertTrue(player.hasCard(aceOfSpades));
        assertEquals(1, player.size());

        assertTrue(player.removeCard(aceOfSpades));
        assertFalse(player.hasCard(aceOfSpades));
    }

    @Test
    public void testAddNullCardReturnsFalse() {
        assertFalse("Adding null card should return false", player.addCard(null));
    }

    @Test
    public void testEncapsulationLeak() {
        player.addCard(aceOfSpades);
        List<Card> hand = player.getHand();

        try {
            hand.add(new Card(Card.Suit.HEARTS, 2));
            fail("Hand should be immutable!");
        } catch (UnsupportedOperationException e) {
            // Success
        }
    }

    @Test
    public void testLoseLifeAndElimination() {
        assertFalse(player.isEliminated());
        player.loseLife();
        assertTrue(player.isEliminated());

        // Ensure lives don't go negative
        player.loseLife();
        assertEquals(0, player.getLives());
    }
}