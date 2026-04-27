package com.leonidasAndrei.asserta;

public class Card {

    //ATTRIBUTES
    private String suit;
    private int rank;
    private String symbol;
    private final String[] RANKS_SYMBOLS = {"0", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

    //METHODS
    public Card(String suit, int rank, String symbol) {
        this.suit = suit;
        this.rank = rank;
        this.symbol = symbol;
    }

    public Card(String suit, int rank) {
        this.suit = suit;
        this.rank = rank;
        this.symbol = RANKS_SYMBOLS[rank];
    }

    public Card(String suit, String symbol) {
        this.suit = suit;
        this.symbol = symbol;
        this.rank = symbolToRank(symbol);
    }

    public int symbolToRank(String symbol) {
        return switch (symbol) {
            case "A" -> 1;
            case "J" -> 11;
            case "Q" -> 12;
            case "K" -> 13;
            case "10" -> 10;
            default -> Integer.parseInt(symbol);
        };
    }

    public String RankToSymbol(int rank) {
        return RANKS_SYMBOLS[rank];
    }

    public String getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
