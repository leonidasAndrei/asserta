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
    }

    //METHODS
    public void addPlayer(Player p) {
        players.add(p);
    }

    public void startGame() {

        state.setActivePlayers(players);

        deck = new Deck();
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
        int[] ranks = {1, 12, 13}; //{A, Q, K}
        rndIndex = ranks[random.nextInt(ranks.length)];
        state.setDeclaredRank(rndIndex);
        state.setDeclaredSymbol(Card.rankToSymbolFull(rndIndex));
    }

    public void playTurn(List<Card> cardsPlayed) {

        if (state.getTableCards().size() == 20) {
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

        if (checkWinner()) return;
        startNewRound();
    }

    private void startNewRound() {
        //reset
        state.setPhase(GamePhase.WAITING);
        state.getTableCards().clear();
        state.setDeclaredRank(127);
        state.setDeclaredSymbol("");
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

        for (int i = 0; i < 3; i++) {
            try {
                System.out.println("...THINKING...");
                Thread.sleep(1000); // 1 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }


        Player bot = state.getCurrentPlayer();

        // decide whether to call bluff (only if someone already played this round)
        if (!state.getTableCards().isEmpty() && Math.random() < 0.25) {
            System.out.println(bot.getUsername() + " calls bluff!");
            try {
                System.out.println("...THINKING...");
                Thread.sleep(1000); // 1 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            callBluff();
            return;
        }

        List<Card> hand = new ArrayList<>(bot.getHand());
        Random random = new Random();

        int maxCanPlay = Math.min(3, hand.size());
        int numToPlay = random.nextInt(maxCanPlay) + 1;

        // pick random cards from hand
        List<Card> toPlay = new ArrayList<>();
        for (int i = 0; i < numToPlay; i++) {
            int idx = random.nextInt(hand.size());
            toPlay.add(hand.get(idx));
            hand.remove(idx); // avoid picking the same card twice
        }

        System.out.println(bot.getUsername() + " plays " + numToPlay + " " + state.getDeclaredSymbol() + "(s).");

        try {
            System.out.println("...THINKING...");
            Thread.sleep(1000); // 1 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        playTurn(toPlay);
    }

    public GameState getState() {
        return state;
    }

    public Deck getDeck() {
        return deck;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
