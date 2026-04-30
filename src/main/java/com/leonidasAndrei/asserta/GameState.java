package com.leonidasAndrei.asserta;

import java.util.ArrayList;
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
    private int numberOfTurns;
    private GamePhase phase;

    //CONSTRUCTORS
    public GameState(List<Player> players) {
        activePlayers = new ArrayList<>(players);
        tableCards = new ArrayList<>();
        currentPlayer = null;
        lastClaimer = null;
        declaredRank = 127;
        currentPlayerIndex = 0;
        roundOver = false;
        numberOfTurns = 0;
        phase = GamePhase.WAITING;

    }

    public GameState(
            List<Player> players,
            Player currentPlayer,
            Player lastClaimer,
            int declaredRank,
            List<Card> tableCards,
            int currentPlayerIndex,
            boolean roundOver,
            int numberOfTurns,
            GamePhase phase
    ) {
        this.activePlayers = players;
        this.currentPlayer = currentPlayer;
        this.lastClaimer = lastClaimer;
        this.declaredRank = declaredRank;
        this.tableCards = tableCards;
        this.currentPlayerIndex = currentPlayerIndex;
        this.roundOver = roundOver;
        this.numberOfTurns = numberOfTurns;
        this.phase = phase;
    }

    //METHODS
    public List<Player> getActivePlayers() {
        return activePlayers;
    }

    public void setActivePlayers(List<Player> activePlayers) {
        this.activePlayers = activePlayers;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Player getLastClaimer() {
        return lastClaimer;
    }

    public void setLastClaimer(Player lastClaimer) {
        this.lastClaimer = lastClaimer;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public int getDeclaredRank() {
        return declaredRank;
    }

    public void setDeclaredRank(int declaredRank) {
        this.declaredRank = declaredRank;
    }

    public List<Card> getTableCards() {
        return tableCards;
    }

    public void setTableCards(List<Card> tableCards) {
        this.tableCards = tableCards;
    }

    public boolean isRoundOver() {
        return roundOver;
    }

    public int getNumberOfTurns() {
        return numberOfTurns;
    }

    public void addNumberOfTurns() {
        numberOfTurns++;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }


}
