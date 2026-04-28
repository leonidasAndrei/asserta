package com.leonidasAndrei.asserta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    //ATTRIBUTES
    private List<Card> deck = new ArrayList<>();
    private int type;

    //CONSTRUCTORS
    public Deck() {
        Card.Suit[] suits = Card.Suit.values();
        int suitIndex = 0;

        for (int i = 0; i < 6; i++) {
            deck.add(new Card(suits[suitIndex++ % suits.length], 13)); // King
        }

        for (int i = 0; i < 6; i++) {
            deck.add(new Card(suits[suitIndex++ % suits.length], 12)); // Queen
        }

        for (int i = 0; i < 6; i++) {
            deck.add(new Card(suits[suitIndex++ % suits.length], 1)); // Ace
        }

        // Add 2 Jokers
        deck.add(new Card(null, 0));
        deck.add(new Card(null, 0));

        Collections.shuffle(deck);
    }

    public Deck(int type) {
        if (type != 0 && type != 1) {
            throw new IllegalArgumentException("Type must be 0 (full deck) or 1 (standard deck)");
        } else {
            this.type = type;

            // Standard 52 cards
            for (Card.Suit suit : Card.Suit.values()) {
                for (int rank = 1; rank <= 13; rank++) {
                    deck.add(new Card(suit, rank));
                }
            }

            // Full 54 cards
            if (type == 0) {
                deck.add(new Card(null, 0));
                deck.add(new Card(null, 0));
            }
            Collections.shuffle(deck);
        }
    }

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
