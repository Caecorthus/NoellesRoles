package org.agmas.noellesroles.gangsterstar;

import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

/**
 * 保存黑帮巨星的回合状态：
 * 左轮剩余次数、是否购买黑帮精神，以及无护盾疯魔倒计时死亡流程。
 */
public class GangsterStarPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<GangsterStarPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(Noellesroles.MOD_ID, "gangster_star"),
            GangsterStarPlayerComponent.class
    );

    private static final int STARTING_REVOLVER_SHOTS = 1;
    private static final int FINAL_STAND_TICKS = 20 * 20;
    private static final String EVENT_GANGSTER_SPIRIT_TRIGGERED = "gangster_spirit_triggered";

    private final PlayerEntity player;
    private int revolverShots = STARTING_REVOLVER_SHOTS;
    private boolean gangsterSpirit = false;
    private boolean finalStandActive = false;
    private boolean resolvingFinalStandDeath = false;
    private int finalStandTicks = 0;
    private UUID pendingKiller = null;
    private Identifier pendingDeathReason = GameConstants.DeathReasons.GUN;

    public GangsterStarPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    public void reset() {
        if (this.finalStandActive) {
            PlayerPsychoComponent.KEY.get(this.player).stopPsycho(true);
        }
        this.revolverShots = STARTING_REVOLVER_SHOTS;
        this.gangsterSpirit = false;
        this.finalStandActive = false;
        this.finalStandTicks = 0;
        this.pendingKiller = null;
        this.pendingDeathReason = GameConstants.DeathReasons.GUN;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity recipient) {
        return recipient == this.player;
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeInt(this.revolverShots);
        buf.writeBoolean(this.gangsterSpirit);
        buf.writeBoolean(this.finalStandActive);
        buf.writeInt(this.finalStandTicks);
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        this.revolverShots = buf.readInt();
        this.gangsterSpirit = buf.readBoolean();
        this.finalStandActive = buf.readBoolean();
        this.finalStandTicks = buf.readInt();
    }

    public int getRevolverShots() {
        return this.revolverShots;
    }

    public boolean hasRevolverShot() {
        return this.revolverShots > 0;
    }

    public void addRevolverShot() {
        this.revolverShots++;
        this.sync();
    }

    public boolean consumeRevolverShot() {
        if (this.revolverShots <= 0) {
            return false;
        }
        this.revolverShots--;
        this.sync();
        return true;
    }

    public boolean hasGangsterSpirit() {
        return this.gangsterSpirit;
    }

    public boolean buyGangsterSpirit() {
        if (this.gangsterSpirit) {
            this.player.sendMessage(Text.translatable("shop.error.noellesroles.gangster_star.spirit_owned"), true);
            return false;
        }
        this.gangsterSpirit = true;
        this.sync();
        return true;
    }

    public boolean isFinalStandActive() {
        return this.finalStandActive;
    }

    public boolean isResolvingFinalStandDeath() {
        return this.resolvingFinalStandDeath;
    }

    public int getFinalStandTicks() {
        return this.finalStandTicks;
    }

    public boolean tryStartFinalStand(PlayerEntity killer, Identifier deathReason) {
        if (!this.gangsterSpirit || this.finalStandActive) {
            return false;
        }
        // 触发后立即消耗黑帮精神，避免再次被枪击时刷新倒计时。
        this.gangsterSpirit = false;
        this.finalStandActive = true;
        this.finalStandTicks = FINAL_STAND_TICKS;
        this.pendingKiller = killer == null ? null : killer.getUuid();
        this.pendingDeathReason = deathReason == null ? GameConstants.DeathReasons.GUN : deathReason;

        PlayerPsychoComponent psychoComponent = PlayerPsychoComponent.KEY.get(this.player);
        // 这里必须使用 Wathe 原生的世界疯魔计数，否则客户端 isPsychoActive() 不会触发原版疯魔BGM。
        if (psychoComponent.startPsycho(true)) {
            psychoComponent.setPsychoTicks(Integer.MAX_VALUE);
            // 黑帮精神只给予疯魔工具，不提供任何护盾。
            psychoComponent.setArmour(0);
        }

        if (this.player instanceof ServerPlayerEntity serverPlayer) {
            var event = GameRecordManager.event(EVENT_GANGSTER_SPIRIT_TRIGGERED)
                    .actor(serverPlayer);
            if (killer instanceof ServerPlayerEntity serverKiller) {
                event.target(serverKiller);
            }
            event.record();
        }

        this.player.sendMessage(Text.translatable("tip.noellesroles.gangster_star.final_stand")
                .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        this.sync();
        return true;
    }

    @Override
    public void serverTick() {
        if (!this.finalStandActive) {
            return;
        }

        this.finalStandTicks--;
        PlayerPsychoComponent.KEY.get(this.player).setArmour(0);

        if (this.finalStandTicks <= 0 && this.player instanceof ServerPlayerEntity serverPlayer) {
            this.finalStandActive = false;
            // 使用 Wathe 原生疯魔状态结束逻辑，让 GameWorldComponent.psychosActive 同步递减并停止原版疯魔BGM。
            PlayerPsychoComponent.KEY.get(this.player).stopPsycho(true);
            ServerPlayerEntity killer = null;
            if (this.pendingKiller != null && this.player.getWorld() instanceof ServerWorld serverWorld) {
                killer = serverWorld.getServer().getPlayerManager().getPlayer(this.pendingKiller);
            }
            Identifier deathReason = Noellesroles.DEATH_REASON_GANGSTER_SPIRIT_TIMEOUT;
            this.pendingKiller = null;
            this.sync();
            // 标记本次死亡由黑帮精神结算触发，避免 KillPlayer.BEFORE 再次进入黑帮精神逻辑。
            this.resolvingFinalStandDeath = true;
            try {
                GameFunctions.killPlayer(serverPlayer, true, killer, deathReason, true);
            } finally {
                this.resolvingFinalStandDeath = false;
            }
        } else if (this.finalStandTicks % 20 == 0) {
            this.sync();
        }
    }

    @Override
    public void clientTick() {
        if (this.finalStandActive && this.finalStandTicks > 0) {
            this.finalStandTicks--;
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("revolverShots", this.revolverShots);
        tag.putBoolean("gangsterSpirit", this.gangsterSpirit);
        tag.putBoolean("finalStandActive", this.finalStandActive);
        tag.putInt("finalStandTicks", this.finalStandTicks);
        if (this.pendingKiller != null) {
            tag.putUuid("pendingKiller", this.pendingKiller);
        }
        if (this.pendingDeathReason != null) {
            tag.putString("pendingDeathReason", this.pendingDeathReason.toString());
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.revolverShots = tag.contains("revolverShots") ? tag.getInt("revolverShots") : STARTING_REVOLVER_SHOTS;
        this.gangsterSpirit = tag.getBoolean("gangsterSpirit");
        this.finalStandActive = tag.getBoolean("finalStandActive");
        this.finalStandTicks = tag.getInt("finalStandTicks");
        this.pendingKiller = tag.containsUuid("pendingKiller") ? tag.getUuid("pendingKiller") : null;
        this.pendingDeathReason = tag.contains("pendingDeathReason")
                ? Identifier.tryParse(tag.getString("pendingDeathReason"))
                : GameConstants.DeathReasons.GUN;
    }
}
