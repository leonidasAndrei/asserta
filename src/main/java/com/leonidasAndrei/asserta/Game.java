package com.leonidasAndrei.asserta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {

    //ATTRIBUTES
    private GameState state;
    private Deck deck;
    private List<Player> players;

    //CONSTRUCTORS
    public Game() {
        this.players = new ArrayList<>();

        this.state = new GameState(players);
        this.deck = new Deck();
    }

    //METHODS
    public void startGame() {
        deck.shuffle();
        for (Player p : players) {
            for (int i = 0; i < 5; i++) {
                p.addCard(deck.deal());
            }
        }

        Random random = new Random();
        int rndIndex = random.nextInt(players.size());

        state.setCurrentPlayerIndex(rndIndex);
        state.setCurrentPlayer(players.get(rndIndex));


        int[] ranks = {1, 11, 12, 13};
        rndIndex = ranks[random.nextInt(ranks.length)];
        state.setDeclaredRank(rndIndex);

        state.setPhase(GameState.GamePhase.PLAYING);

    }

    public void playTurn() {
        //TODO:
        //current player chooses cards to play
        //animation(?)
        //next player decides if it's a bluff or not
    }

    public void callBluff() {
        //TODO:
        //reveal table cards
        //handle accusation -> skip turn(?)
        //boh start new round/change declared rank?
    }

    private boolean checkBluff() {
        //TODO:
        return true;
    }

    public boolean checkWinner(){
        return players.size() <= 1;
    }
}
