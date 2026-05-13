package org.agmas.noellesroles.util;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.taotie.SwallowedPlayerComponent;

import java.util.Set;

public final class RoleUtils {
    private RoleUtils() {}

    private static final Set<Role> KILLER_ROLES = Set.of(
            WatheRoles.KILLER,
            Noellesroles.SWAPPER,
            Noellesroles.PHANTOM,
            Noellesroles.MORPHLING,
            Noellesroles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES,
            Noellesroles.BOMBER,
            Noellesroles.ASSASSIN,
            Noellesroles.SCAVENGER,
            Noellesroles.SERIAL_KILLER,
            Noellesroles.SILENCER,
            Noellesroles.POISONER,
            Noellesroles.BANDIT,
            Noellesroles.HUNTER,
            Noellesroles.COMMANDER,
            Noellesroles.BREACHER
    );

    public static boolean isActualKillerRole(Role role) {
        return role != null && KILLER_ROLES.contains(role);
    }

    public static int countAliveAndNotSwallowed(ServerWorld serverWorld) {
        int count = 0;
        for (ServerPlayerEntity p : serverWorld.getPlayers()) {
            if (!GameFunctions.isPlayerPlayingAndAlive(p) || SpectatorStateHelper.isRealSpectator(p)) continue;
            SwallowedPlayerComponent swallowed = SwallowedPlayerComponent.KEY.get(p);
            if (!swallowed.isSwallowed()) {
                count++;
            }
        }
        return count;
    }
}
