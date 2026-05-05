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
            handlePunishment(claimer);
        } else {
            System.out.println(caller.getUsername() + " was wrong!");
            handlePunishment(caller);
        }
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

        state.getActivePlayers().removeIf(Player::isEliminated); //remove all eliminated players from activePlayers

        if (checkWinner()) return;
        startNewRound();
    }

    private void handlePunishment(Player loser) {
        // set phase and randomize poisoned cup into state
        state.setLoser(loser);
        state.setPoisonedCup(new Random().nextInt(3) + 1);
        state.setPhase(GamePhase.PICKING_POISON);
    }

    public void pickPoison(int chosen) {
        Player loser = state.getLoser();

        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        if (chosen == state.getPoisonedCup() - 1) {
            System.out.println("Poisoned! " + loser.getUsername() + " loses a life!");
            loser.loseLife();
        }
        else {
            System.out.println("Safe! " + loser.getUsername() + " survives!");
            System.out.println("(The poisoned cup was cup " + state.getPoisonedCup() + ")");
        }
        handleAfterBluff();
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

        botThink(3);

        Player bot = state.getCurrentPlayer();
        Random random = new Random();
        double smartRoll = Math.random();

        // ── SMART MODE (smartRoll > 0.6) ─────────────────────────────────────────
        if (smartRoll > 0.6) {

            // SMART: call bluff only if table is suspicious
            if (!state.getTableCards().isEmpty()) {
                int maxOfRank = 6; // there are 6 Aces, 6 Kings, 6 Queens in deck
                int tableCount = state.getTableCards().size();

                // if more cards on table than exist in the whole deck → definitely bluffing
                // if more than 4 already on table → getting very suspicious
                boolean verysuspicious = tableCount >= maxOfRank - 1;
                boolean slightlySuspicious = tableCount >= 3 && Math.random() < 0.5;

                if (verysuspicious || slightlySuspicious) {
                    System.out.println(bot.getUsername() + " calls bluff!");
                    botThink(1);
                    callBluff();
                    return;
                }
            }

            // SMART: prefer playing cards that actually match the declared rank
            List<Card> hand = new ArrayList<>(bot.getHand());
            List<Card> matchingCards = new ArrayList<>();
            List<Card> nonMatchingCards = new ArrayList<>();

            for (Card c : hand) {
                if (c.getRank() == state.getDeclaredRank() || c.getRank() == 0) { // 0 = Joker (wild)
                    matchingCards.add(c);
                } else {
                    nonMatchingCards.add(c);
                }
            }

            List<Card> toPlay = new ArrayList<>();

            if (!matchingCards.isEmpty()) {
                // has real matching cards → play them honestly (1 to min(3, matching))
                int numToPlay = random.nextInt(Math.min(3, matchingCards.size())) + 1;
                for (int i = 0; i < numToPlay; i++) {
                    int idx = random.nextInt(matchingCards.size());
                    toPlay.add(matchingCards.get(idx));
                    matchingCards.remove(idx);
                }
                /*System.out.println(bot.getUsername() + " plays " + toPlay.size() + " " + state.getDeclaredSymbol() + "(s). (honest)");*/
            } else {
                // no matching cards → forced to bluff, pick random non-matching
                int numToPlay = random.nextInt(Math.min(3, nonMatchingCards.size())) + 1;
                for (int i = 0; i < numToPlay; i++) {
                    int idx = random.nextInt(nonMatchingCards.size());
                    toPlay.add(nonMatchingCards.get(idx));
                    nonMatchingCards.remove(idx);
                }
                /*System.out.println(bot.getUsername() + " plays " + toPlay.size() + " " + state.getDeclaredSymbol() + "(s). (forced bluff)");*/
            }
            System.out.println(bot.getUsername() + " plays " + toPlay.size() + " " + state.getDeclaredSymbol() + "(s).");

            botThink(1);
            playTurn(toPlay);

            // ── DUMB MODE (smartRoll <= 0.6) ─────────────────────────────────────────
        } else {

            // random bluff call
            if (!state.getTableCards().isEmpty() && Math.random() < 0.25) {
                System.out.println(bot.getUsername() + " calls bluff!");
                botThink(1);
                callBluff();
                return;
            }

            // play random cards
            List<Card> hand = new ArrayList<>(bot.getHand());
            int maxCanPlay = Math.min(3, hand.size());
            int numToPlay = random.nextInt(maxCanPlay) + 1;

            List<Card> toPlay = new ArrayList<>();
            for (int i = 0; i < numToPlay; i++) {
                int idx = random.nextInt(hand.size());
                toPlay.add(hand.get(idx));
                hand.remove(idx);
            }

            /*System.out.println(bot.getUsername() + " plays " + numToPlay + " " + state.getDeclaredSymbol() + "(s). (random)");*/
            System.out.println(bot.getUsername() + " plays " + toPlay.size() + " " + state.getDeclaredSymbol() + "(s).");
            botThink(1);
            playTurn(toPlay);
        }
    }

    // ── helper to avoid repeating Thread.sleep boilerplate ───────────────────────
    private void botThink(int seconds) {
        for (int i = 0; i < seconds; i++) {
            try {
                System.out.println("...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
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
