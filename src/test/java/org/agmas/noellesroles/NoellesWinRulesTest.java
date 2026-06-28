package org.agmas.noellesroles;

import dev.doctor4t.wathe.game.GameFunctions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoellesWinRulesTest {
    @Test
    void corruptCopBlocksOrdinaryPassengerWinBeforeSurvivalMasterPassengerWin() {
        assertEquals(
                NoellesWinRules.WinAction.BLOCK,
                NoellesWinRules.chooseWinAction(
                        GameFunctions.WinStatus.PASSENGERS,
                        false,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        true
                )
        );
    }

    @Test
    void taotieAndJesterMomentBlockBeforeSurvivalMasterPassengerWin() {
        assertEquals(
                NoellesWinRules.WinAction.BLOCK,
                NoellesWinRules.chooseWinAction(
                        GameFunctions.WinStatus.KILLERS,
                        false,
                        true,
                        false,
                        false,
                        false,
                        true,
                        false,
                        false,
                        false
                )
        );
        assertEquals(
                NoellesWinRules.WinAction.BLOCK,
                NoellesWinRules.chooseWinAction(
                        GameFunctions.WinStatus.PASSENGERS,
                        false,
                        true,
                        false,
                        false,
                        false,
                        false,
                        true,
                        false,
                        false
                )
        );
    }

    @Test
    void completedNeutralWinsResolveBeforeOrdinaryWinBlockers() {
        assertEquals(
                NoellesWinRules.WinAction.PATHOGEN_WIN,
                NoellesWinRules.chooseWinAction(
                        GameFunctions.WinStatus.PASSENGERS,
                        false,
                        true,
                        true,
                        false,
                        false,
                        true,
                        true,
                        false,
                        true
                )
        );
        assertEquals(
                NoellesWinRules.WinAction.CORRUPT_COP_WIN,
                NoellesWinRules.chooseWinAction(
                        GameFunctions.WinStatus.KILLERS,
                        false,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        true,
                        false
                )
        );
    }

    @Test
    void survivalMasterCanStillAllowPassengersAfterBlockersClear() {
        assertEquals(
                NoellesWinRules.WinAction.ALLOW_PASSENGERS,
                NoellesWinRules.chooseWinAction(
                        GameFunctions.WinStatus.TIME,
                        false,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false
                )
        );
    }
}
