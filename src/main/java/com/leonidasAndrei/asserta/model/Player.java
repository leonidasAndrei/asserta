package com.leonidasAndrei.asserta.model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    //ATTRIBUTES
    private int playerID;
    private String username;
    private List<Card> hand;
    private boolean isHuman;
    private boolean isAlive;

    //CONSTRUCTORS
    public Player(String username, int playerID, boolean isHuman) {
        this.playerID = playerID;

        if (username == null || username.isBlank()) {
            this.username = (isHuman ? "Player_" : "AI_") + playerID;
        } else {
            this.username = username;
        }
        this.isHuman = isHuman;
        this.isAlive = true;
        this.hand = new ArrayList<>();
    }

    //METHODS
    public boolean addCard(Card c) {
        if (c != null) {
            hand.add(c);
            return true;
        }
        return false;
    }

    public boolean removeCard(Card c) {
        return c != null && hand.remove(c);
    }

    public boolean hasCard(Card c) {
        return hand.contains(c);
    }

    public boolean isEmpty() {
        return hand.isEmpty();
    }

    public int size() {
        return hand.size();
    }

    public void loseLife() {
        isAlive=false;
    }

    public List<Card> getHand() {
        return hand;
    }

    public boolean isEliminated() {
        return !isAlive;
    }

    public int getPlayerID() {
        return playerID;
    }

    public String getUsername() {
        return username;
    }

    public boolean isHuman() {
        return isHuman;
    }

    public boolean isAlive() {
        return isAlive;
    }
}
