package com.leonidasAndrei.asserta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameState {
    private List<Player> players;
    private Player currentPlayer;
    private Player lastClaimer;
    private int currentPlayerIndex;
    private int declaredRank;
    private List<Card> tableCards;
    private boolean roundOver;

    public GameState() {
        this.players = new ArrayList<>();
        this.tableCards = new ArrayList<>();
        this.currentPlayer = null;
        this.lastClaimer = null;
        this.declaredRank = 0;
        this.currentPlayerIndex = 0;
        this.roundOver = false;
    }

    public GameState(
            List<Player> players,
            Player currentPlayer,
            Player lastClaimer,
            int declaredRank,
            List<Card> tableCards,
            int currentPlayerIndex,
            boolean roundOver
    ) {
        this.players = players;
        this.currentPlayer = currentPlayer;
        this.lastClaimer = lastClaimer;
        this.declaredRank = declaredRank;
        this.tableCards = tableCards;
        this.currentPlayerIndex = currentPlayerIndex;
        this.roundOver = roundOver;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getLastClaimer() {
        return lastClaimer;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public int getDeclaredRank() {
        return declaredRank;
    }

    public List<Card> getTableCards() {
        return Collections.unmodifiableList(tableCards);
    }

    public boolean isRoundOver() {
        return roundOver;
    }
}
