package org.agmas.noellesroles.jester;

public final class JesterHotbarRules {
    public static final int HOTBAR_SIZE = 9;

    private JesterHotbarRules() {
    }

    static int forcedPsychoBatSlot(boolean[] occupiedSlots, int selectedSlot) {
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            if (occupiedSlots == null || slot >= occupiedSlots.length || !occupiedSlots[slot]) {
                return slot;
            }
        }
        if (selectedSlot >= 0 && selectedSlot < HOTBAR_SIZE) {
            return selectedSlot;
        }
        return 0;
    }
}
