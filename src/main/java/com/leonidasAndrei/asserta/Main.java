package com.leonidasAndrei.asserta;

import com.leonidasAndrei.asserta.GameState.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Game game = new Game();
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        //CLI STUFF
        System.out.println("\nWelcome to Asserta!");
        runSleep(1);
        System.out.println("Either perfect the art of deception, or become a master at detecting it in others.");
        runSleep(1);

        while (game.getState().getPhase() == GamePhase.WAITING) {
            System.out.println("\nHow should we proceed?");
            System.out.println("(1) --- Play");
            System.out.println("(2) --- Rules");
            System.out.println("(3) --- Exit");
            choice = scanner.nextInt();

            if (choice == 3) {
                System.out.println("Leaving already?");
            }
            else if (choice == 2){
                System.out.println("Rules will appear here.");
            }
            else {
                game.addPlayer(new Player("leolo", 1, true));
                game.addPlayer(new Player("Bot 1", 2, false));
                game.addPlayer(new Player("Bot 2", 3, false));
                game.addPlayer(new Player("Bot 3", 4, false));
                game.startGame();
            }
        }

        while (game.getState().getPhase() != GamePhase.GAME_OVER) {

            System.out.println("\n--- " + game.getState().getDeclaredSymbol() + "'s TABLE ---");
            runSleep(0.5);
            System.out.println("There are " + game.getState().getTableCards().size() + " cards on table");

            runSleep(1);

            Player current = game.getState().getCurrentPlayer();
            System.out.println("\n--- " + current.getUsername() + "'s turn ---");
            runSleep(1);

            if (current.isHuman()) {
                // WAITING = someone just played, this player must decide
                if (game.getState().getPhase() == GamePhase.WAITING && game.getState().getNumberOfTurns() > 0) {

                    System.out.println("(1) --- Believe and play");
                    System.out.println("(2) --- Call bluff");
                    choice = scanner.nextInt();

                    if (choice == 2) {
                        runSleep(1);
                        game.callBluff();
                        continue;
                    }
                }

                runSleep(1);
                System.out.println("\nYour hand:");
                List<Card> hand = current.getHand();
                for (int i = 0; i < hand.size(); i++) {
                    System.out.println("  [" + i + "] " + hand.get(i));
                }

                // collect cards to play
                List<Card> toPlay = new ArrayList<>();
                runSleep(0.5);

                System.out.println(" - Type card index to add");
                System.out.println(" - Type 'DONE' when finished");
                System.out.println(" - You must play at least 1 card and maximum 3:");
                while (toPlay.size() < 3) {
                    String input = scanner.next();

                    if (input.equals("DONE") || input.equals("done")) {
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
                            continue;
                        }
                        Card chosen = hand.get(idx);
                        if (toPlay.contains(chosen)) {
                            System.out.println("Already selected that card.");
                            continue;
                        }
                        toPlay.add(chosen);
                        System.out.println("Added: " + chosen + " (" + toPlay.size() + " selected)");
                    } catch (NumberFormatException e) {
                        System.out.println("Type a NUMBER or 'DONE'.");
                    }
                }

                game.playTurn(toPlay);
                System.out.println(current.getUsername() + " plays " + toPlay.size() + " " + game.getState().getDeclaredSymbol() + "(s).");


            } else {
                game.playBotTurn();
            }
        }
    }

    public static void runSleep(double seconds) {
        double mills = seconds * 1000;
        try {
            Thread.sleep((long) mills); // 1 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}