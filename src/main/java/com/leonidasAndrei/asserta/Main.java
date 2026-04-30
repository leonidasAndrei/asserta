package com.leonidasAndrei.asserta;

import com.leonidasAndrei.asserta.GameState.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Game game = new Game();
        game.addPlayer(new Player("leolo", 1, true));
        game.addPlayer(new Player("Bot 1", 2, false));
        game.addPlayer(new Player("Bot 2", 3, false));
        game.addPlayer(new Player("Bot 3", 4, false));
        game.startGame();

        // the loop keeps going until someone wins
        while (game.getState().getPhase() != GamePhase.GAME_OVER) {

            //HARDCODE
            String rnk = null;
            int dclrdRnk = game.getState().getDeclaredRank();
            switch (dclrdRnk) {
                case 1:
                    rnk = "ACE";
                    break;
                case 12:
                    rnk = "QUEEN";
                    break;
                case 13:
                    rnk = "KING";
                    break;
            }
            System.out.println("\n--- " + rnk + "'s TABLE ---");
            runSleep(1);

            Player current = game.getState().getCurrentPlayer();
            System.out.println("\n--- " + current.getUsername() + "'s turn ---");
            runSleep(0.5);
            System.out.println("Cards on table: " + game.getState().getTableCards().size());
            runSleep(0.5);

            if (current.isHuman()) {
                // WAITING = someone just played, this player must decide
                if (game.getState().getPhase() == GamePhase.WAITING && game.getState().getNumberOfTurns() > 0) {

                    System.out.println("1) Believe and play  2) Call bluff");
                    int choice = scanner.nextInt();

                    if (choice == 2) {
                        game.callBluff();
                        continue; // round restarted, go back to top of loop
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
                System.out.println("Type card index to add (type 'done' when finished, min 1 max 3):");

                while (toPlay.size() < 3) {
                    String input = scanner.next();

                    if (input.equals("done")) {
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
                        System.out.println("Type a number or 'done'.");
                    }
                }

                game.playTurn(toPlay);

            } else {
                game.playBotTurn();
            }
        }
    }

    public static void runSleep(double seconds) {
        double mills = seconds * 1000;
        try {
            Thread.sleep((long)mills); // 1 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}