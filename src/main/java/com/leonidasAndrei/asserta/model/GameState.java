package com.leonidasAndrei.asserta.model;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    //ATTRIBUTES

    private List<Player> activePlayers;
    private Player currentPlayer;
    private Player lastClaimer;
    private Player loser;
    private int currentPlayerIndex;
    private int declaredRank;
    private String declaredSymbol;
    private List<Card> tableCards;
    private int numberOfTurns;
    private int poisonedCup;
    private GamePhase phase;
    private int round;

    //CONSTRUCTORS
    public GameState(List<Player> players) {
        activePlayers = new ArrayList<>(players);
        tableCards = new ArrayList<>();
        currentPlayer = null;
        lastClaimer = null;
        loser = null;
        declaredRank = 127;
        declaredSymbol = "";
        currentPlayerIndex = 0;
        numberOfTurns = 0;
        poisonedCup = 127;
        phase = GamePhase.WAITING;
        round = 0;
    }

    public GameState(
            List<Player> players,
            Player currentPlayer,
            Player lastClaimer,
            Player loser,
            int declaredRank,
            String declaredSymbol,
            List<Card> tableCards,
            int currentPlayerIndex,
            int numberOfTurns,
            int poisonedCup,
            GamePhase phase,
            int round
    ) {
        this.activePlayers = players;
        this.currentPlayer = currentPlayer;
        this.lastClaimer = lastClaimer;
        this.loser = loser;
        this.declaredRank = declaredRank;
        this.declaredSymbol = declaredSymbol;
        this.tableCards = tableCards;
        this.currentPlayerIndex = currentPlayerIndex;
        this.numberOfTurns = numberOfTurns;
        this.poisonedCup = poisonedCup;
        this.phase = phase;
        this.round = round;
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

    public Player getLoser() { return loser; }

    public void setLoser(Player loser) { this.loser = loser; }

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
        numberOfTurns++;
    }

    public int getPoisonedCup() { return poisonedCup; }

    public void setPoisonedCup(int poisonedCup) {
        if (poisonedCup < 1 || poisonedCup > 3) throw new IllegalArgumentException("Cup must be 1, 2 or 3");
        this.poisonedCup = poisonedCup;
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

    public void addRound() {
        round++;
    }
}
