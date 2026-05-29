package com.leonidasAndrei.asserta.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameState {

    // ATTRIBUTES
    private List<Player> activePlayers;
    private Player currentPlayer;
    private Player lastClaimer;
    private Player loser;
    private int currentPlayerIndex;
    private int declaredRank;
    private String declaredSymbol;
    private List<Card> tableCards;
    private int numberOfTurns;
    private GamePhase phase;
    private int round;

    // FIXES & IMPROVEMENTS ATTRIBUTES
    private int lastCardsPlayedCount;            // Tracks card count of the most recent turn
    private boolean[] cupsAvailable;             // Persistent track of unchosen cups (true = full, false = empty)
    private int poisonedCupIndex;                // Hidden index of the poison (0 to 3)

    // CONSTRUCTORS
    public GameState(List<Player> players) {
        this.activePlayers = new ArrayList<>(players);
        this.tableCards = new ArrayList<>();
        this.numberOfTurns = 0;
        this.round = 0;
        this.phase = GamePhase.NEW_ROUND;

        // Initialize persistent 4 poison cups tracking
        this.cupsAvailable = new boolean[]{true, true, true, true};
        scramblePoisonCupIndex();
    }

    // POISON MEMORY HELPERS
    public void resetPoisonMemory() {
        for (int i = 0; i < 4; i++) {
            cupsAvailable[i] = true;
        }
        scramblePoisonCupIndex();
    }

    public void scramblePoisonCupIndex() {
        this.poisonedCupIndex = new Random().nextInt(4); // 0, 1, 2, or 3
    }

    public boolean isCupAvailable(int index) {
        if (index < 0 || index >= 4) return false;
        return cupsAvailable[index];
    }

    public void setCupChosen(int index) {
        if (index >= 0 && index < 4) {
            this.cupsAvailable[index] = false;
        }
    }

    // GETTERS AND SETTERS
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

    public Player getLoser() {
        return loser;
    }

    public void setLoser(Player loser) {
        this.loser = loser;
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

    public String getDeclaredSymbol() {
        return declaredSymbol;
    }

    public void setDeclaredSymbol(String declaredSymbol) {
        this.declaredSymbol = declaredSymbol;
    }

    public List<Card> getTableCards() {
        return tableCards;
    }

    public void setTableCards(List<Card> tableCards) {
        this.tableCards = tableCards;
    }

    public int getNumberOfTurns() {
        return numberOfTurns;
    }

    public void setNumberOfTurns(int numberOfTurns) {
        this.numberOfTurns = numberOfTurns;
    }

    public void addNumberOfTurns() {
        this.numberOfTurns++;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public void addRound() {
        this.round++;
    }

    public int getLastCardsPlayedCount() {
        return lastCardsPlayedCount;
    }

    public void setLastCardsPlayedCount(int lastCardsPlayedCount) {
        this.lastCardsPlayedCount = lastCardsPlayedCount;
    }

    public int getPoisonedCupIndex() {
        return poisonedCupIndex;
    }

    public boolean[] getCupsAvailable() {
        return cupsAvailable;
    }
}