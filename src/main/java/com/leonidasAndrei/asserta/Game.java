package com.leonidasAndrei.asserta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.leonidasAndrei.asserta.GameState.*;

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
    public void addPlayer(Player p) {
        players.add(p);
    }

    public void startGame() {

        state.setActivePlayers(players);

        deck.shuffle();

        //distribute cards
        state.setPhase(GamePhase.DEALING);
        for (Player p : players) {
            p.getHand().clear();
            for (int i = 0; i < 5; i++) {
                p.addCard(deck.deal());
            }
        }

        //set first player turn randomly
        Random random = new Random();
        int rndIndex = random.nextInt(players.size());

        state.setCurrentPlayerIndex(rndIndex);
        state.setCurrentPlayer(players.get(rndIndex));

        //set the rank randomly
        int[] ranks = {1, 11, 12, 13}; //{A, J, Q, K}
        rndIndex = ranks[random.nextInt(ranks.length)];
        state.setDeclaredRank(rndIndex);
    }

    public void playTurn(List<Card> cardsPlayed) {

        if(state.getTableCards().size() == 20) {
            startNewRound();
        }

        state.setPhase(GamePhase.PLAYING);

        Player currentPlayer = state.getCurrentPlayer();

        for (Card c : cardsPlayed) {
            currentPlayer.removeCard(c);
        }

        state.getTableCards().addAll(cardsPlayed);
        state.setLastClaimer(currentPlayer);
        state.addNumberOfTurns();

        nextTurn();
        state.setPhase(GamePhase.WAITING);

    }

    public void nextTurn() {

        int next = (state.getCurrentPlayerIndex() + 1) % state.getActivePlayers().size();
        state.setCurrentPlayerIndex(next);
        state.setCurrentPlayer(state.getActivePlayers().get(next));

    }

    public void callBluff() {
        state.setPhase(GamePhase.BLUFF_CALLED);

        Player caller = state.getCurrentPlayer();
        Player claimer = state.getLastClaimer();

        boolean wasBluffing = checkBluff();

        if (wasBluffing) {
            System.out.println(claimer.getUsername() + " was bluffing!");
            System.out.println(claimer.getUsername() + " lost a life!");
            claimer.loseLife();
        } else {
            System.out.println(caller.getUsername() + " was wrong!");
            System.out.println(caller.getUsername() + " lost a life!");
            caller.loseLife();
        }

        handleAfterBluff();
    }

    private boolean checkBluff() {
        List<Card> table = state.getTableCards();

        for (Card c : table) {
            // Jokers are wild
            if (c.getRank() == 0) continue;

            if (c.getRank() != state.getDeclaredRank()) {
                return true;
            }
        }
        return false;
    }

    private void handleAfterBluff() {

        state.getActivePlayers().removeIf(Player::isEliminated);

        if(checkWinner()) return;
        startNewRound();
    }

    private void startNewRound() {
        //reset
        state.setPhase(GamePhase.WAITING);
        state.getTableCards().clear();
        state.setDeclaredRank(127);
        state.setLastClaimer(null);

       startGame();
    }

    public boolean checkWinner() {
        if (state.getActivePlayers().size() == 1) {
            state.setPhase(GamePhase.GAME_OVER);
            System.out.println("Winner: " + state.getActivePlayers().get(0).getUsername());
            return true;
        }
        return false;
    }

    public void playBotTurn() {
        Player bot = state.getCurrentPlayer();

        // if there are cards on table, randomly decide to call bluff
        if (state.getNumberOfTurns() > 0 && Math.random() < 0.25) {
            callBluff();
            return;
        }

        // otherwise play a random card from hand
        List<Card> hand = bot.getHand();
        Card toPlay = hand.get(new Random().nextInt(hand.size()));
        playTurn(List.of(toPlay));
    }
}
