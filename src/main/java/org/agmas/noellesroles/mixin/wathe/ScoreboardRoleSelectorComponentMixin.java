package org.agmas.noellesroles.mixin.wathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(ScoreboardRoleSelectorComponent.class)
public abstract class ScoreboardRoleSelectorComponentMixin {

    @Inject(method = "assignVigilantes", at = @At("HEAD"), cancellable = true)
    private void noellesroles$distributeVigilanteSlots(
            ServerWorld world,
            GameWorldComponent gameWorld,
            List<ServerPlayerEntity> players,
            int count,
            CallbackInfo ci) {

        int existingVigilante = 0;
        int existingVeteran = 0;
        int existingPatrol = 0;
        for (ServerPlayerEntity player : players) {
            Role role = gameWorld.getRole(player);
            if (role == null) continue;
            if (role.equals(WatheRoles.VIGILANTE)) existingVigilante++;
            else if (role.equals(WatheRoles.VETERAN)) existingVeteran++;
            else if (role.equals(Noellesroles.RIOT_PATROL)) existingPatrol++;
        }

        int targetVigilante;
        int targetVeteran;
        int targetPatrol;
        if (count <= 2) {
            targetVigilante = count;
            targetVeteran = 0;
            targetPatrol = 0;
        } else if (count == 3) {
            targetVigilante = 2;
            if (world.getRandom().nextBoolean()) {
                targetVeteran = 1;
                targetPatrol = 0;
            } else {
                targetVeteran = 0;
                targetPatrol = 1;
            }
        } else {
            targetVigilante = (count + 1) / 2;
            int remaining = count - targetVigilante;
            int extras = remaining - 2;
            int coinVeteran = 0;
            int coinPatrol = 0;
            for (int i = 0; i < extras; i++) {
                if (world.getRandom().nextBoolean()) coinVeteran++;
                else coinPatrol++;
            }
            targetVeteran = 1 + coinVeteran;
            targetPatrol = 1 + coinPatrol;
        }

        int addVigilante = Math.max(0, targetVigilante - existingVigilante);
        int addVeteran = Math.max(0, targetVeteran - existingVeteran);
        int addPatrol = Math.max(0, targetPatrol - existingPatrol);

        ScoreboardRoleSelectorComponentAccessor accessor =
                (ScoreboardRoleSelectorComponentAccessor) this;
        ArrayList<ServerPlayerEntity> available =
                accessor.noellesroles$invokeGetAvailablePlayers(world, gameWorld, players);

        List<Role> queue = new ArrayList<>(addVigilante + addVeteran + addPatrol);
        for (int i = 0; i < addVigilante; i++) queue.add(WatheRoles.VIGILANTE);
        for (int i = 0; i < addVeteran; i++) queue.add(WatheRoles.VETERAN);
        for (int i = 0; i < addPatrol; i++) queue.add(Noellesroles.RIOT_PATROL);
        Collections.shuffle(queue, new java.util.Random(world.getRandom().nextLong()));

        int slot = 0;
        for (ServerPlayerEntity player : available) {
            if (slot >= queue.size()) break;
            gameWorld.addRole(player, queue.get(slot));
            slot++;
        }

        ci.cancel();
    }

    @Inject(method = "assignCivilians", at = @At("TAIL"))
    private void noellesroles$guaranteeConductor(
            ServerWorld world,
            GameWorldComponent gameWorld,
            List<ServerPlayerEntity> players,
            CallbackInfoReturnable<Integer> cir) {

        if (!gameWorld.isRoleEnabled(Noellesroles.CONDUCTOR)) {
            return;
        }

        ArrayList<ServerPlayerEntity> vanillaCivilianCandidates = new ArrayList<>();
        ArrayList<ServerPlayerEntity> civilianCandidates = new ArrayList<>();
        for (ServerPlayerEntity player : players) {
            Role role = gameWorld.getRole(player);
            if (role == null) {
                continue;
            }
            if (role.equals(Noellesroles.CONDUCTOR)) {
                return;
            }
            if (role.equals(WatheRoles.CIVILIAN)) {
                vanillaCivilianCandidates.add(player);
                continue;
            }
            if (role.getFaction().equals(WatheRoles.CIVILIAN.getFaction())
                    && !Noellesroles.isVigilanteSlotRole(role)) {
                civilianCandidates.add(player);
            }
        }

        List<ServerPlayerEntity> candidates = vanillaCivilianCandidates.isEmpty()
                ? civilianCandidates
                : vanillaCivilianCandidates;
        if (candidates.isEmpty()) {
            return;
        }

        ServerPlayerEntity selected = candidates.get(world.getRandom().nextInt(candidates.size()));
        gameWorld.addRole(selected, Noellesroles.CONDUCTOR);
    }
}
