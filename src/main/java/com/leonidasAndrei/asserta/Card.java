package com.leonidasAndrei.asserta;

public class Card {

    //ATTRIBUTES
    public enum Suit {
        SPADES, CLUBS, HEARTS, DIAMONDS
    }

    private Suit suit;
    private int rank;
    private String symbol;
    private static final String[] RANKS_SYMBOLS = {"0", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};


    //CONSTRUCTORS
    public Card(Suit suit, int rank, String symbol) {
        if (suit == null) {
            throw new NullPointerException("Suit cannot be null");
        }
        validateRank(rank);

        int derivedRank = symbolToRank(symbol);
        if (derivedRank != rank) {
            throw new IllegalArgumentException("Rank and symbol mismatch");
        }

        this.suit = suit;
        this.rank = rank;
        this.symbol = symbol;
    }

    public Card(Suit suit, int rank) {
        if (suit == null) {
            throw new NullPointerException("Suit cannot be null");
        }
        validateRank(rank);
        this.suit = suit;
        this.rank = rank;
        this.symbol = RANKS_SYMBOLS[rank];
    }

    public Card(Suit suit, String symbol) {
        if (suit == null) {
            throw new NullPointerException("Suit cannot be null");
        }
        this.suit = suit;
        this.symbol = symbol;
        this.rank = symbolToRank(symbol);
    }

    //METHODS
    public int symbolToRank(String symbol) {
        return switch (symbol) {
            case "A" -> 1;
            case "J" -> 11;
            case "Q" -> 12;
            case "K" -> 13;
            case "10" -> 10;
            default -> {
                if (symbol.length() == 1 && symbol.charAt(0) >= '2' && symbol.charAt(0) <= '9') {
                    yield symbol.charAt(0) - '0';
                }
                throw new IllegalArgumentException("Invalid symbol: " + symbol);
            }
        };
    }

    public String rankToSymbol(int rank) {
        validateRank(rank);
        return RANKS_SYMBOLS[rank];
    }

    private void validateRank(int rank) {
        if (rank < 1 || rank > 13) {
            throw new IllegalArgumentException("Invalid rank: " + rank);
        }
    }

    public Suit getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        validateRank(rank);
        this.rank = rank;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol + " of " + suit;
    }
}
