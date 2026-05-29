package com.leonidasAndrei.asserta.model;

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
    }

    //METHODS
    public void addPlayer(Player p) {
        players.add(p);
    }

    public void startGame() {
        state.setActivePlayers(players);
        state.setPhase(GamePhase.NEW_ROUND);

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
        state.setPhase(GamePhase.PLAYING);

        Player currentPlayer = state.getCurrentPlayer();

        for (Card c : cardsPlayed) {
            currentPlayer.removeCard(c);
        }

        state.getTableCards().addAll(cardsPlayed);
        state.setLastCardsPlayedCount(cardsPlayed.size());
        state.setLastClaimer(currentPlayer);
        state.addNumberOfTurns();

        if (state.getTableCards().size() >= 20) {
            startNewRound();
            return;
        }

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
            handlePunishment(claimer);
        } else {
            System.out.println(caller.getUsername() + " was wrong!");
            handlePunishment(caller);
        }
    }

    private boolean checkBluff() {
        List<Card> table = state.getTableCards();
        int count = state.getLastCardsPlayedCount();
        int size = table.size();

        if (count <= 0) count = size;

        for (int i = size - count; i < size; i++) { //start from lastCardsPlayed
            if (i < 0) continue;
            Card c = table.get(i);

            // Jokers are wild
            if (c.getRank() == 0) continue;

            if (c.getRank() != state.getDeclaredRank()) {
                return true;
            }
        }
        return false;
    }

    private void handleAfterBluff() {
        state.getActivePlayers().removeIf(Player::isEliminated); //remove all eliminated players

        if (checkWinner()) return;
        startNewRound();
    }

    private void handlePunishment(Player loser) {
        state.setLoser(loser);
        state.setPhase(GamePhase.PICKING_POISON);
    }

    // FIX: Removed Thread.sleep completely. Delays are now controlled cleanly inside the GameController layer.
    public void pickPoison(int chosenIndex) {
        Player loser = state.getLoser();
        System.out.println("The cup " + (chosenIndex + 1) + " has been chosen!");

        if (chosenIndex == state.getPoisonedCupIndex()) {
            System.out.println("Poisoned! " + loser.getUsername() + " loses a life!");
            loser.loseLife();

            // Memory resets, new poison index rolled for the next incident
            state.resetPoisonMemory();
        } else {
            System.out.println("Safe! " + loser.getUsername() + " survives!");

            // Survived -> Remember this selection as empty/unavailable for subsequent penalties
            state.setCupChosen(chosenIndex);
        }
        handleAfterBluff();
    }

    private void startNewRound() {
        state.setPhase(GamePhase.NEW_ROUND);

        //reset
        state.getTableCards().clear();
        state.setDeclaredRank(127);
        state.setDeclaredSymbol("");
        state.setLastClaimer(null);
        state.setNumberOfTurns(0);
        state.setLastCardsPlayedCount(0);
        state.addRound();
    }

    public boolean checkWinner() {
        if (state.getActivePlayers().size() == 1) {
            state.setPhase(GamePhase.GAME_OVER);
            System.out.println("Winner: " + state.getActivePlayers().get(0).getUsername());
            return true;
        }
        return false;
    }

    public List<Card> playBotTurn() {
        Player bot = state.getCurrentPlayer();
        Random random = new Random();
        double smartRoll = Math.random();
        List<Card> toPlay = new ArrayList<>();

        if (smartRoll > 0.6) {
            if (!state.getTableCards().isEmpty()) {
                int maxOfRank = 6;
                int tableCount = state.getTableCards().size();
                boolean verySuspicious = tableCount >= maxOfRank - 1;
                boolean slightlySuspicious = tableCount >= 3 && Math.random() < 0.5;

                if (verySuspicious || slightlySuspicious) {
                    System.out.println(bot.getUsername() + " calls bluff!");
                    callBluff();
                    return new ArrayList<>();
                }
            }

            List<Card> hand = new ArrayList<>(bot.getHand());
            List<Card> matching = new ArrayList<>();
            List<Card> nonMatching = new ArrayList<>();

            for (Card c : hand) {
                if (c.getRank() == state.getDeclaredRank() || c.getRank() == 0)
                    matching.add(c);
                else
                    nonMatching.add(c);
            }

            List<Card> pool = matching.isEmpty() ? nonMatching : matching;
            int numToPlay = random.nextInt(Math.min(3, pool.size())) + 1;
            for (int i = 0; i < numToPlay; i++) {
                int idx = random.nextInt(pool.size());
                toPlay.add(pool.get(idx));
                pool.remove(idx);
            }

        } else {
            if (!state.getTableCards().isEmpty() && Math.random() < 0.25) {
                System.out.println(bot.getUsername() + " calls bluff!");
                callBluff();
                return new ArrayList<>();
            }

            List<Card> hand = new ArrayList<>(bot.getHand());
            int numToPlay = random.nextInt(Math.min(3, hand.size())) + 1;
            for (int i = 0; i < numToPlay; i++) {
                int idx = random.nextInt(hand.size());
                toPlay.add(hand.get(idx));
                hand.remove(idx);
            }
        }

        System.out.println(bot.getUsername() + " plays " + toPlay.size()
                + " " + state.getDeclaredSymbol() + "(s).");
        playTurn(toPlay);
        return toPlay;
    }

    public GameState getState() { return state; }
    public Deck getDeck() { return deck; }
    public List<Player> getPlayers() { return players; }
}