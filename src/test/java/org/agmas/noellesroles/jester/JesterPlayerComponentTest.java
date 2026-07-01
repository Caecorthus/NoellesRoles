package org.agmas.noellesroles.jester;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JesterPlayerComponentTest {
    @Test
    void forcedPsychoBatUsesFirstEmptyHotbarSlot() {
        assertEquals(2, JesterHotbarRules.forcedPsychoBatSlot(
                new boolean[]{true, true, false, true, true, true, true, true, true},
                6
        ));
    }

    @Test
    void forcedPsychoBatReplacesSelectedSlotWhenHotbarIsFull() {
        assertEquals(6, JesterHotbarRules.forcedPsychoBatSlot(
                new boolean[]{true, true, true, true, true, true, true, true, true},
                6
        ));
    }

    @Test
    void forcedPsychoBatFallsBackToFirstSlotForInvalidSelection() {
        assertEquals(0, JesterHotbarRules.forcedPsychoBatSlot(
                new boolean[]{true, true, true, true, true, true, true, true, true},
                12
        ));
    }
}
