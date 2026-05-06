package com.leonidasAndrei.asserta;

import com.leonidasAndrei.asserta.GameState.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Game game = new Game();
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        // ── WELCOME ──────────────────────────────────────────────────────────
        System.out.println("\nWelcome to Asserta!");
        runSleep(1);
        System.out.println("Either perfect the art of deception, or become a master at detecting it in others.");
        runSleep(1);

        // ── MAIN MENU ─────────────────────────────────────────────────────────
        boolean inMenu = true;
        while (inMenu) {
            choice = -1; // reset every iteration
            System.out.println("\nHow should we proceed?");
            System.out.println("(1) --- Play");
            System.out.println("(2) --- Rules");
            System.out.println("(3) --- Exit");
            System.out.print(">> ");

            while (choice != 1 && choice != 2 && choice != 3) {
                choice = scanner.nextInt();
                if (choice != 1 && choice != 2 && choice != 3) {
                    System.out.print("Invalid choice. Type 1, 2 or 3: ");
                }
            }

            switch (choice) {
                case 1 -> inMenu = false;
                case 2 -> System.out.println("\nRules will appear here.");
                case 3 -> {
                    System.out.println("Leaving already? Goodbye!");
                    return;
                }
            }
        }

        choice = 0;
        // ── GAME SETUP ────────────────────────────────────────────────────────
        while (choice < 1 || choice > 4) {
            System.out.print("How many players? (1-4): ");
            choice = scanner.nextInt();
            if (choice < 1 || choice > 4) {
                System.out.print("Invalid. Pick a number from 1 to 4: ");
            }
        }
        scanner.nextLine(); // ← consume the leftover newline after nextInt()

        for (int i = 1; i <= choice; i++) {
            System.out.print("Enter name for player " + i + ": ");
            String name = scanner.nextLine(); // now works correctly
            game.addPlayer(new Player(name, i, true));
        }

        for (int i = choice + 1; i <= 4; i++) {
            game.addPlayer(new Player("Bot " + (i - choice), i, false));
        }
        game.startGame();

        choice = 0;
        // ── GAME LOOP ─────────────────────────────────────────────────────────
        while (game.getState().getPhase() != GamePhase.GAME_OVER) {

            if (game.getState().getPhase() == GamePhase.NEW_ROUND) {
                System.out.println("\n--- Starting a new round! ---");
                game.startGame();
                runSleep(2);
                continue;
            }
            // ── PICKING POISON PHASE ─────────────────────────────────────────
            if (game.getState().getPhase() == GamePhase.PICKING_POISON) {
                runSleep(1);
                Player loser = game.getState().getLoser();
                System.out.println("\n 🧪   🧪   🧪");
                System.out.println("[0]  [1]  [2]");

                if (loser.isHuman()) {
                    System.out.print("Pick a cup (0, 1 or 2): ");
                    int cup = -1;
                    while (cup < 0 || cup > 2) {
                        cup = scanner.nextInt();
                        if (cup < 0 || cup > 2) System.out.print("Invalid. Pick 0, 1 or 2: ");
                    }
                    game.pickPoison(cup);
                } else {
                    System.out.println(loser.getUsername() + " is choosing...");
                    runSleep(2);
                    int cup = new Random().nextInt(3);
                    System.out.println(loser.getUsername() + " picks cup [" + cup + "]...");
                    runSleep(1);
                    game.pickPoison(cup);
                }
                continue;
            }

            // ── TABLE STATUS ─────────────────────────────────────────────────
            System.out.println("\n--- " + game.getState().getDeclaredSymbol() + "'s TABLE ---");
            System.out.println("Turn " + game.getState().getNumberOfTurns());
            runSleep(0.5);
            System.out.println("There are " + game.getState().getTableCards().size() + " cards on table");
            runSleep(1);

            // ── CURRENT PLAYER ───────────────────────────────────────────────
            Player current = game.getState().getCurrentPlayer();
            System.out.println("\n--- " + current.getUsername() + "'s turn ---");
            runSleep(1);

            if (current.isHuman()) {

                // show hand
                List<Card> hand = current.getHand();
                System.out.println("\nYour hand:");
                for (int i = 0; i < hand.size(); i++) {
                    System.out.println("  [" + i + "] " + hand.get(i));
                }

                // believe or call bluff (only after at least one turn has been played)
                if (game.getState().getPhase() == GamePhase.WAITING
                        && game.getState().getNumberOfTurns() > 0) {

                    choice = -1;
                    System.out.println("\n(1) --- Believe and play");
                    System.out.println("(2) --- Call bluff");
                    System.out.print(">> ");
                    while (choice != 1 && choice != 2) {
                        choice = scanner.nextInt();
                        if (choice != 1 && choice != 2) {
                            System.out.print("Invalid. Type 1 or 2: ");
                        }
                    }

                    if (choice == 2) {
                        runSleep(1);
                        game.callBluff();
                        continue;
                    }
                }

                // collect cards to play
                List<Card> toPlay = new ArrayList<>();
                List<Integer> selectedIndices = new ArrayList<>();

                System.out.println(" - Type card index to add");
                System.out.println(" - Type 'DONE' when finished");
                System.out.println(" - You must play at least 1 card and at most 3:");
                while (toPlay.size() < 3) {
                    System.out.print(">> ");
                    String input = scanner.next().toUpperCase();

                    if (input.equals("DONE")) {
                        if (toPlay.isEmpty()) {
                            System.out.println("You must play at least 1 card.");
                            continue;
                        }
                        break;
                    }

                    try {
                        int idx = Integer.parseInt(input);
                        if (idx < 0 || idx >= hand.size()) {
                            System.out.println("Invalid index, try again.");
                        } else if (selectedIndices.contains(idx)) {
                            System.out.println("Already selected that index.");
                        } else {
                            selectedIndices.add(idx);
                            toPlay.add(hand.get(idx));
                            System.out.println("Added: " + hand.get(idx) + " (" + toPlay.size() + " selected)");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Type a NUMBER or 'DONE'.");
                    }
                }

                // print BEFORE playTurn so getDeclaredSymbol is still the current one
                System.out.println(current.getUsername() + " plays " + toPlay.size()
                        + " " + game.getState().getDeclaredSymbol() + "(s).");
                game.playTurn(toPlay);

            } else {
                game.playBotTurn();
            }
        }

        // ── GAME OVER ─────────────────────────────────────────────────────────
        System.out.println("\n🏆 Game over!");
    }

    public static void runSleep(double seconds) {
        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}