package com.leonidasAndrei.asserta;

import com.leonidasAndrei.asserta.model.GameState;
import com.leonidasAndrei.asserta.model.Player;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import com.leonidasAndrei.asserta.model.GamePhase;

public class GameStateTest {

    /*@Test
    public void testInitialStateDefaults() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Leonidas", 1, true));

        GameState state = new GameState(players);

        assertEquals(GamePhase.DEALING, state.getPhase());
        assertEquals(127, state.getDeclaredRank());
        assertFalse(state.isRoundOver());
        assertEquals(1, state.getActivePlayers().size());
    }*/

    @Test
    public void testPhaseTransitions() {
        GameState state = new GameState(new ArrayList<>());

        state.setPhase(GamePhase.PLAYING);
        assertEquals(GamePhase.PLAYING, state.getPhase());

        state.setPhase(GamePhase.BLUFF_CALLED);
        assertEquals(GamePhase.BLUFF_CALLED, state.getPhase());
    }

    @Test
    public void testEncapsulation() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Leonidas", 1, true));

        GameState state = new GameState(players);

        try {
            state.getActivePlayers().add(new Player("Hacker", 2, true));
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
}
