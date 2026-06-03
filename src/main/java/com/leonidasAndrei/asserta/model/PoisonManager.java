package com.leonidasAndrei.asserta.model;

import java.util.Random;

public class PoisonManager {
    private final boolean[] cupsAvailable; // true = unchosen, false = chosen & empty
    private int poisonedCupIndex;          // Hidden poison index (0 to 3)
    private final Random random = new Random();

    public PoisonManager() {
        this.cupsAvailable = new boolean[4];
        resetCups();
    }

    public void resetCups() {
        for (int i = 0; i < 4; i++) {
            cupsAvailable[i] = true;
        }
        poisonedCupIndex = random.nextInt(4);
    }

    public int getPoisonedCupIndex() {
        return poisonedCupIndex;
    }

    public boolean isCupAvailable(int index) {
        if (index < 0 || index >= 4) return false;
        return cupsAvailable[index];
    }

    public void setCupChosen(int index) {
        if (index >= 0 && index < 4) {
            cupsAvailable[index] = false; // Mark this cup as spent/empty
        }
    }
}