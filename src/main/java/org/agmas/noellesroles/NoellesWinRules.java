package org.agmas.noellesroles;

import dev.doctor4t.wathe.game.GameFunctions;

/**
 * Pure round-end ordering rules for NoellesRoles neutral and moment wins.
 * NoellesRoles 中立与时刻胜利的纯结算排序规则。
 */
public final class NoellesWinRules {
    private NoellesWinRules() {
    }

    public static WinAction chooseWinAction(
            GameFunctions.WinStatus currentStatus,
            boolean vultureWon,
            boolean survivalMasterCompleted,
            boolean pathogenWon,
            boolean jesterWon,
            boolean taotieWon,
            boolean taotieBlocksOrdinaryWin,
            boolean jesterPsychoBlocksOrdinaryWin,
            boolean corruptCopWon,
            boolean corruptCopBlocksOrdinaryWin
    ) {
        if (vultureWon) {
            return WinAction.VULTURE_WIN;
        }
        if (pathogenWon) {
            return WinAction.PATHOGEN_WIN;
        }
        if (jesterWon) {
            return WinAction.JESTER_WIN;
        }
        if (taotieWon) {
            return WinAction.TAOTIE_WIN;
        }
        if (corruptCopWon) {
            return WinAction.CORRUPT_COP_WIN;
        }
        if (isOrdinaryTeamWin(currentStatus)
                && (taotieBlocksOrdinaryWin
                || jesterPsychoBlocksOrdinaryWin
                || corruptCopBlocksOrdinaryWin)) {
            return WinAction.BLOCK;
        }
        if (survivalMasterCompleted) {
            return WinAction.ALLOW_PASSENGERS;
        }
        return WinAction.NONE;
    }

    public static boolean isOrdinaryTeamWin(GameFunctions.WinStatus currentStatus) {
        return currentStatus == GameFunctions.WinStatus.KILLERS
                || currentStatus == GameFunctions.WinStatus.PASSENGERS;
    }

    public enum WinAction {
        NONE,
        VULTURE_WIN,
        PATHOGEN_WIN,
        JESTER_WIN,
        TAOTIE_WIN,
        CORRUPT_COP_WIN,
        BLOCK,
        ALLOW_PASSENGERS
    }
}
