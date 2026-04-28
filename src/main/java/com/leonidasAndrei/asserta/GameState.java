package com.leonidasAndrei.asserta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameState {

    //ATTRIBUTES
    public enum GamePhase {DEALING, PLAYING, WAITING, BLUFF_CALLED, GAME_OVER}

    private List<Player> activePlayers;
    private Player currentPlayer;
    private Player lastClaimer;
    private int currentPlayerIndex;
    private int declaredRank;
    private List<Card> tableCards;
    private boolean roundOver;
    private GamePhase phase;

    //CONSTRUCTORS
    public GameState(List <Player> players) {
        this.activePlayers = new ArrayList<>(players);
        this.tableCards = new ArrayList<>();
        this.currentPlayer = null;
        this.lastClaimer = null;
        this.declaredRank = 127;
        this.currentPlayerIndex = 0;
        this.roundOver = false;
        phase = GamePhase.DEALING;

    }

    public GameState(
            List<Player> players,
            Player currentPlayer,
            Player lastClaimer,
            int declaredRank,
            List<Card> tableCards,
            int currentPlayerIndex,
            boolean roundOver,
            GamePhase phase
    ) {
        this.activePlayers = players;
        this.currentPlayer = currentPlayer;
        this.lastClaimer = lastClaimer;
        this.declaredRank = declaredRank;
        this.tableCards = tableCards;
        this.currentPlayerIndex = currentPlayerIndex;
        this.roundOver = roundOver;
        this.phase = phase;
    }

    //METHODS
    public List<Player> getActivePlayers() { return Collections.unmodifiableList(activePlayers); }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) { this.currentPlayer = currentPlayer; }

    public Player getLastClaimer() {
        return lastClaimer;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) { this.currentPlayerIndex = currentPlayerIndex; }

    public int getDeclaredRank() {
        return declaredRank;
    }

    public void setDeclaredRank(int declaredRank) { this.declaredRank = declaredRank; }

    public List<Card> getTableCards() {
        return Collections.unmodifiableList(tableCards);
    }

    public boolean isRoundOver() {
        return roundOver;
    }

    public GamePhase getPhase() { return phase; }

    public void setPhase(GamePhase phase) { this.phase = phase; }
}
