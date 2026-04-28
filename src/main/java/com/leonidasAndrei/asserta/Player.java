package com.leonidasAndrei.asserta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {

    //ATTRIBUTES
    private int playerID;
    private String username;
    private List<Card> hand;
    private boolean isHuman;
    private int lives;

    //CONSTRUCTORS
    public Player(String username, int playerID, boolean isHuman) {
        this.playerID = playerID;

        if (username == null || username.isBlank()) {
            this.username = (isHuman ? "Player_" : "AI_") + playerID;
        } else {
            this.username = username;
        }
        this.isHuman = isHuman;
        this.lives = 1;
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
        if (lives > 0) {
            lives--;
        }
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public boolean isEliminated() {
        return lives <= 0;
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

    public int getLives() {
        return lives;
    }
}
