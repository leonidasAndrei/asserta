package com.leonidasAndrei.asserta;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

public class GameStateTest {

    private List<Player> players;
    private Player p1;
    private Player p2;
    private List<Card> tableCards;

    @Before
    public void setup() {
        players = new ArrayList<>();
        p1 = new Player("Leonidas", 1, true);
        p2 = new Player("Andrei", 2, true);
        players.add(p1);
        players.add(p2);

        tableCards = new ArrayList<>();
        tableCards.add(new Card(Card.Suit.HEARTS, 5));
    }

    @Test
    public void testDefaultConstructor() {
        GameState state = new GameState();
        assertNotNull(state.getPlayers()); // Assuming getter exists
        assertTrue(state.getPlayers().isEmpty());
        assertEquals(0, state.getDeclaredRank());
        assertFalse(state.isRoundOver());
    }

    @Test
    public void testParameterizedConstructor() {
        GameState state = new GameState(players, p1, p2, 10, tableCards, 0, false);

        assertEquals(2, state.getPlayers().size());
        assertEquals(p1, state.getCurrentPlayer());
        assertEquals(p2, state.getLastClaimer());
        assertEquals(10, state.getDeclaredRank());
        assertEquals(1, state.getTableCards().size());
        assertEquals(0, state.getCurrentPlayerIndex());
        assertFalse(state.isRoundOver());
    }
}