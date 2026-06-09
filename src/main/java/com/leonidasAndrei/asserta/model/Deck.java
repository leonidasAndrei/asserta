package com.leonidasAndrei.asserta.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    //ATTRIBUTES
    private List<Card> deck;

    //CONSTRUCTORS
    //Bluff deck
    public Deck() {
        deck = new ArrayList<>();
        Card.Suit[] suits = Card.Suit.values();
        int suitIndex = 0;

        for (int i = 0; i < 6; i++) {
            deck.add(new Card(suits[suitIndex++ % suits.length], 13)); // King
            deck.add(new Card(suits[suitIndex++ % suits.length], 12)); // Queen
            deck.add(new Card(suits[suitIndex++ % suits.length], 1)); // Ace
        }

        // Add 2 Jokers
        deck.add(new Card(suits[0], 0));
        deck.add(new Card(suits[2], 0));
    }

    //Full or Standard deck
    public Deck(int type) {
        deck = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                deck.add(new Card(suit, rank));
            }
        }
    }

    //METHODS
    public void shuffle() {
        if(!deck.isEmpty()) {
            Collections.shuffle(deck);
        }
    }

    public Card deal() {
        if (deck.isEmpty()) {
            throw new IllegalStateException("Deck is empty");
        }
        Card d = deck.removeLast();
        return d;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public boolean isEmpty() {
        return deck.isEmpty();
    }

    public int size() {
        return deck.size();
    }
}
