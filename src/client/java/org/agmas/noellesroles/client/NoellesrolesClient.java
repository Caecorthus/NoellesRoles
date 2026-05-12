package org.agmas.noellesroles.client;

import com.google.common.collect.Maps;
import dev.doctor4t.wathe.api.event.*;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.ConfigWorldComponent;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.assassin.AssassinPlayerComponent;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.client.gui.JesterTimeRenderer;
import org.agmas.noellesroles.client.gui.LooseEndsRadarHudRenderer;
import org.agmas.noellesroles.client.gui.MurderMayhemIntroHudRenderer;
import org.agmas.noellesroles.client.murdermayhem.FogOfWarClientHelper;
import org.agmas.noellesroles.client.gui.SpectatorReplayToastOverlay;
import org.agmas.noellesroles.client.hallucination.ClientHallucinationState;
import org.agmas.noellesroles.client.hallucination.HallucinationClientVisibilityHelper;
import org.agmas.noellesroles.deatharena.DeathArenaPlayerComponent;
import org.agmas.noellesroles.client.configscreen.NoellesRolesConfigScreenFactory;
import org.agmas.noellesroles.client.silencer.SilencedTalkBubbleCleaner;
import org.agmas.noellesroles.client.sound.SoundPhysicsConfigLockManager;
import org.agmas.noellesroles.client.sound.TalkBubblesConfigLockManager;
import org.agmas.noellesroles.client.screen.RoleInfoScreen;
import org.agmas.noellesroles.client.screen.RoleTargetMenuScreen;
import org.agmas.noellesroles.client.screen.NoellesRolesConfigScreen;
import org.agmas.noellesroles.client.screen.SpectatorAssistPanelScreen;
import org.agmas.noellesroles.util.HiddenEquipmentHelper;
import dev.doctor4t.wathe.index.WatheItems;
import org.agmas.noellesroles.client.screen.AssassinScreen;
import org.agmas.noellesroles.client.screen.CommanderScreen;
import org.agmas.noellesroles.client.screen.CriminalReasonerScreen;
import org.agmas.noellesroles.commander.CommanderPlayerComponent;
import org.agmas.noellesroles.corruptcop.CorruptCopPlayerComponent;
import org.agmas.noellesroles.jester.JesterPlayerComponent;
import org.agmas.noellesroles.looseend.LooseEndPlayerComponent;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.client.renderer.EngineerDoorHighlightRenderer;
import org.agmas.noellesroles.client.renderer.FogOfWarRenderer;
import org.agmas.noellesroles.client.renderer.HallucinationDummyRenderer;
import org.agmas.noellesroles.client.renderer.HallucinationFakeBodyRenderer;
import org.agmas.noellesroles.packet.AbilityC2SPacket;
import org.agmas.noellesroles.packet.DeathArenaToggleC2SPacket;
import org.agmas.noellesroles.packet.EngineerDoorHighlightS2CPacket;
import org.agmas.noellesroles.packet.FerrymanBodyAgeSyncS2CPacket;
import org.agmas.noellesroles.packet.HallucinationDummyStateS2CPacket;
import org.agmas.noellesroles.packet.HallucinationSoundS2CPacket;
import org.agmas.noellesroles.packet.MorphCorpseToggleC2SPacket;
import org.agmas.noellesroles.packet.SpectatorInfoRequestC2SPacket;
import org.agmas.noellesroles.packet.VultureEatC2SPacket;
import org.agmas.noellesroles.vulture.VulturePlayerComponent;
import org.agmas.noellesroles.packet.ReporterMarkC2SPacket;
import org.agmas.noellesroles.packet.SpectatorReplayDetailSyncS2CPacket;
import org.agmas.noellesroles.packet.SpectatorInfoSyncS2CPacket;
import org.agmas.noellesroles.pathogen.InfectedPlayerComponent;
import org.agmas.noellesroles.professor.IronManPlayerComponent;
import org.agmas.noellesroles.taotie.SwallowedPlayerComponent;
import org.agmas.noellesroles.util.SpectatorStateHelper;
import org.agmas.noellesroles.util.SwallowedInteractionHelper;
import org.agmas.noellesroles.taotie.TaotiePlayerComponent;
import org.agmas.noellesroles.packet.TaotieSwallowC2SPacket;
import org.agmas.noellesroles.packet.SilencerSilenceC2SPacket;
import org.agmas.noellesroles.silencer.SilencerPlayerComponent;
import org.agmas.noellesroles.client.music.WorldMusicManager;
import org.agmas.noellesroles.reporter.ReporterPlayerComponent;
import org.agmas.noellesroles.bodyguard.BodyguardPlayerComponent;
import org.agmas.noellesroles.entity.HunterTrapEntity;
import org.agmas.noellesroles.ferryman.FerrymanPlayerComponent;
import org.agmas.noellesroles.hunter.HunterPlayerComponent;
import org.agmas.noellesroles.hallucination.HallucinationPlayerComponent;
import org.agmas.noellesroles.hallucination.HallucinationHelper;
import org.agmas.noellesroles.orthopedist.OrthopedistPlayerComponent;
import org.agmas.noellesroles.riotpatrol.RiotPatrolPlayerComponent;
import org.agmas.noellesroles.serialkiller.SerialKillerPlayerComponent;
import org.agmas.noellesroles.bomber.BomberPlayerComponent;
import org.agmas.noellesroles.NoellesRolesEntities;
import org.agmas.noellesroles.util.BodyTargetHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import org.agmas.noellesroles.client.renderer.ThrowingAxeEntityRenderer;
import org.agmas.noellesroles.client.roleinfo.RoleInfoRegistry;
import org.agmas.noellesroles.client.renderer.HunterTrapEntityRenderer;
import org.agmas.noellesroles.client.util.NoellesRolesItemTooltips;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.*;
import java.util.List;

public class NoellesrolesClient implements ClientModInitializer {
    public static final Identifier RIOT_FORK_IN_HAND_MODEL_ID = Identifier.of(Noellesroles.MOD_ID, "item/riot_fork_inhand");
    public static final Identifier HUNTER_TRAP_PLACED_MODEL_ID = Identifier.of(Noellesroles.MOD_ID, "item/hunter_trap_placed");
    private static final int COMMANDER_MARK_HIGHLIGHT_COLOR = 0x8F6BD1;
    public static int insanityTime = 0;
    public static KeyBinding abilityBind;
    public static KeyBinding ability2Bind;
    public static KeyBinding assistInterfaceBind;
    public static KeyBinding deathArenaToggleBind;
    public static KeyBinding roleInfoBind;
    public static KeyBinding configScreenBind;
    public static PlayerBodyEntity targetBody;
    public static PlayerEntity pathogenNearestTarget;
    public static double pathogenNearestTargetDistance;
    public static String pathogenTargetDirection;
    public static String pathogenTargetVertical; // 垂直位置提示
    public static PlayerEntity crosshairTarget;
    public static double crosshairTargetDistance;

    public static Map<UUID, UUID> SHUFFLED_PLAYER_ENTRIES_CACHE = Maps.newHashMap();

    // 不可见物品提示：切换到不可见物品时提示
    private static boolean wasHoldingInvisible = false;
    private static long spectatorReplayPollRequestId = 10_000L;
    private static long nextSpectatorReplayPollTick = Long.MAX_VALUE;
    private static boolean wasDeadSpectatorLastTick = false;
    private static boolean wasAssistInterfacePressed = false;
    private static boolean wasDeathArenaTogglePressed = false;
    private static boolean wasRoleInfoPressed = false;
    private static boolean wasConfigScreenPressed = false;
    private static int swallowedLockedSelectedSlot = -1;
    private static boolean wasClientPlayerSwallowedLastTick = false;
    private static float swallowedLockedYaw = 0.0F;
    private static float swallowedLockedPitch = 0.0F;


    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.addModels(RIOT_FORK_IN_HAND_MODEL_ID);
            pluginContext.addModels(HUNTER_TRAP_PLACED_MODEL_ID);
        });

        abilityBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Noellesroles.MOD_ID + ".ability", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.wathe.keybinds"));
        ability2Bind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Noellesroles.MOD_ID + ".ability2", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, "category.wathe.keybinds"));
        assistInterfaceBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Noellesroles.MOD_ID + ".assist_interface", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "category.wathe.keybinds"));
        deathArenaToggleBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Noellesroles.MOD_ID + ".death_arena_toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.wathe.keybinds"));
        roleInfoBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Noellesroles.MOD_ID + ".role_info", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "category.wathe.keybinds"));
        configScreenBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Noellesroles.MOD_ID + ".config_screen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.wathe.keybinds"));
        // 加载角色信息配置
        RoleInfoRegistry.load();

        // 注册解毒剂冷却模型谓词
        ModelPredicateProviderRegistry.register(ModItems.ANTIDOTE, Identifier.of(Noellesroles.MOD_ID, "cooldown"),
                (stack, world, entity, seed) -> {
                    if (!(entity instanceof PlayerEntity player)) return 0.0f;
                    return player.getItemCooldownManager().isCoolingDown(stack.getItem()) ? 1.0f : 0.0f;
                });

        NoellesRolesItemTooltips.register();

        // 注册世界BGM管理器
        WorldMusicManager.register();

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                client.execute(() -> {
                    SoundPhysicsConfigLockManager.deactivate();
                    TalkBubblesConfigLockManager.deactivate();
                    SHUFFLED_PLAYER_ENTRIES_CACHE.clear();
                    ClientHallucinationState.reset();
                })
        );

        // 注册工程师门高亮渲染器
        EngineerDoorHighlightRenderer.register();
        FogOfWarRenderer.register();
        HallucinationDummyRenderer.register();
        HallucinationFakeBodyRenderer.register();

        // 注册工程师门高亮 S2C 包接收
        ClientPlayNetworking.registerGlobalReceiver(EngineerDoorHighlightS2CPacket.ID,
                (payload, context) -> runOnClient(context.client(), () ->
                        EngineerDoorHighlightRenderer.onPacketReceived(payload.doorPos())
                ));

        ClientPlayNetworking.registerGlobalReceiver(FerrymanBodyAgeSyncS2CPacket.ID,
                (payload, context) -> runOnClient(context.client(), () -> {
                    var world = context.client().world;
                    if (world == null) return;
                    if (world.getEntityById(payload.entityId()) instanceof PlayerBodyEntity body) {
                        body.age = payload.age();
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(HallucinationDummyStateS2CPacket.ID,
                (payload, context) -> runOnClient(context.client(), () ->
                        ClientHallucinationState.applyDummySync(
                                context.client(),
                                payload.added(),
                                payload.removed(),
                                payload.fakeBodies(),
                                payload.fakeBodyDeathReasons()
                        )
                ));
        ClientPlayNetworking.registerGlobalReceiver(HallucinationSoundS2CPacket.ID,
                (payload, context) -> runOnClient(context.client(), () ->
                        ClientHallucinationState.playSound(context.client(), payload.soundId())
                ));

        // 注册职业广播 S2C 包接收：复用对讲机渲染器在屏幕上方显示
        ClientPlayNetworking.registerGlobalReceiver(org.agmas.noellesroles.packet.RoleBroadcastS2CPacket.ID,
                (payload, context) -> runOnClient(context.client(), () ->
                        dev.doctor4t.wathe.client.gui.WalkieTalkieBroadcastRenderer.addMessage(payload.message())
                ));

        // 注册观战信息同步 S2C 包接收
        ClientPlayNetworking.registerGlobalReceiver(SpectatorInfoSyncS2CPacket.ID,
                (payload, context) -> runOnClient(context.client(), () -> {
                    SpectatorAssistPanelScreen.applyServerSync(payload);
                    SpectatorReplayToastOverlay.onSpectatorSync(payload);
                }));
        ClientPlayNetworking.registerGlobalReceiver(SpectatorReplayDetailSyncS2CPacket.ID,
                (payload, context) -> runOnClient(context.client(), () ->
                        SpectatorAssistPanelScreen.applyReplayDetailSync(payload)
                ));

        // 注册实体渲染器
        EntityRendererRegistry.register(NoellesRolesEntities.POISON_GAS_BOMB_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(NoellesRolesEntities.POISON_GAS_CLOUD_ENTITY, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(NoellesRolesEntities.THROWING_AXE_ENTITY, ThrowingAxeEntityRenderer::new);
        EntityRendererRegistry.register(NoellesRolesEntities.HUNTER_TRAP_ENTITY, HunterTrapEntityRenderer::new);

        CanSeeMoney.EVENT.register(player -> {
            if (!GameFunctions.isPlayerPlayingAndAlive(player)) return null;
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(
                    player.getWorld()
            );
            if(gameWorldComponent.isRole(player, Noellesroles.RECALLER) && !gameWorldComponent.isPlayerDead(player.getUuid())){
                return CanSeeMoney.Result.ALLOW;
            }
            return null;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> LooseEndsRadarHudRenderer.tick());

        // 注册 CanSeeBodyRole 监听器：高理智的验尸官查看尸体时可获得完整验尸信息
        CanSeeBodyRole.EVENT.register(viewer -> {
            if (!(viewer instanceof PlayerEntity player)) {
                return false;
            }

            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
            if (!gameWorldComponent.isRole(player, Noellesroles.CORONER)) {
                return false;
            }
            if (!GameFunctions.isPlayerPlayingAndAlive(player) || SwallowedPlayerComponent.isPlayerSwallowed(player)) {
                return false;
            }

            PlayerMoodComponent moodComponent = PlayerMoodComponent.KEY.get(player);
            return !moodComponent.isLowerThanMid();
        });

        // 注册 GetInstinctHighlight 监听器：各角色的本能高亮逻辑
        GetInstinctHighlight.EVENT.register(entity -> {

            if (!(entity instanceof PlayerEntity player) || SpectatorStateHelper.isSpectatorLike(player) || player.isInvisible()) return null;

            if (MinecraftClient.getInstance().player == null) return null;

            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(
                MinecraftClient.getInstance().player.getWorld()
            );

            if (!WatheClient.isPlayerPlayingAndAlive()) return null;

            PlayerEntity localPlayer = MinecraftClient.getInstance().player;

            if (FogOfWarClientHelper.shouldClampKillerInstinct(localPlayer, player)
                    && !FogOfWarClientHelper.isWithinKillerInstinctLimit(localPlayer, player)) {
                return GetInstinctHighlight.HighlightResult.skip();
            }

            // 黑帮巨星使用自己的杀手本能：按本能键时用深紫色统一高亮所有其他玩家。
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.GANGSTER_STAR) && player != localPlayer) {
                if (gameWorldComponent.isRole(player, Noellesroles.SURVIVAL_MASTER) && !localPlayer.canSee(player)) {
                    return GetInstinctHighlight.HighlightResult.skip();
                }
                return GetInstinctHighlight.HighlightResult.withKeybind(
                        Noellesroles.GANGSTER_STAR.color(),
                        GetInstinctHighlight.HighlightResult.PRIORITY_HIGH + 1
                );
            }

            HallucinationPlayerComponent hallucinationComponent = HallucinationPlayerComponent.KEY.get(localPlayer);
            if (HallucinationHelper.hasInstinctMisjudge(localPlayer, player)) {
                int color = HallucinationHelper.isInstinctMisjudgeTreatAsAlly(localPlayer, player)
                        ? MathHelper.hsvToRgb(0F, 1.0F, 0.6F)
                        : Color.GREEN.getRGB();
                return GetInstinctHighlight.HighlightResult.withKeybind(color, GetInstinctHighlight.HighlightResult.PRIORITY_HIGH);
            }

            GetInstinctHighlight.HighlightResult commanderMarkedHighlight =
                    getCommanderMarkedHighlight(gameWorldComponent, localPlayer, player);
            if (commanderMarkedHighlight != null) {
                return commanderMarkedHighlight;
            }

            if (gameWorldComponent.isRole(localPlayer, Noellesroles.CORRUPT_COP)) {
                var comp = CorruptCopPlayerComponent.KEY.get(localPlayer);
                if (comp.canSeePlayersThroughWalls()){
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.CORRUPT_COP.color());
                }
            }

            // BOMBER: 本能透视 - 无需按键即可看到携带定时炸弹的玩家
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.BOMBER)) {
                BomberPlayerComponent comp = BomberPlayerComponent.KEY.get(player);
                if (comp.hasBomb()) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.BOMBER.color());
                }
            }

            if (gameWorldComponent.isRole(localPlayer, Noellesroles.JESTER)) {
                JesterPlayerComponent jesterComponent = JesterPlayerComponent.KEY.get(localPlayer);
                if (jesterComponent.inPsychoMode && player.getUuid().equals(jesterComponent.targetKiller))
                {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.JESTER.color());
                }
            }

            // 疯魔模式：所有人自动全局高亮小丑（判断目标是小丑角色+处于疯魔模式）
            if (gameWorldComponent.isRole(player, Noellesroles.JESTER)) {
                JesterPlayerComponent jesterComponent = JesterPlayerComponent.KEY.get(player);
                if (jesterComponent.inPsychoMode) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.JESTER.color());
                }
            }

            // BARTENDER: 看到喝酒者发绿光（需要视线）
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.BARTENDER)) {
                if (localPlayer.canSee(player)) {
                    BartenderPlayerComponent comp = BartenderPlayerComponent.KEY.get(player);
                    if (comp.glowTicks > 0) return GetInstinctHighlight.HighlightResult.always(Color.GREEN.getRGB());
                }
            }

            // PROFESSOR: 看到有铁人buff的人发蓝光（需要视线）
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.PROFESSOR)) {
                if (localPlayer.canSee(player)) {
                    IronManPlayerComponent comp = IronManPlayerComponent.KEY.get(player);
                    if (comp.hasBuff()) {
                        return GetInstinctHighlight.HighlightResult.always(Color.BLUE.getRGB());
                    }
                }
            }

            // TOXICOLOGIST: 看到中毒者发红光（需要视线）
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.TOXICOLOGIST)) {
                if (localPlayer.canSee(player)) {
                    PlayerPoisonComponent comp = PlayerPoisonComponent.KEY.get(player);
                    if (comp.poisonTicks > 0) return  GetInstinctHighlight.HighlightResult.always(Noellesroles.TOXICOLOGIST.color());
                }
            }

            // ORTHOPEDIST: 看到处于正骨状态的玩家（需要视线）
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.ORTHOPEDIST)) {
                OrthopedistPlayerComponent comp = OrthopedistPlayerComponent.KEY.get(player);
                if (localPlayer.canSee(player) && comp.hasBoneSettingActive()) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.ORTHOPEDIST.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.POISONER)) {
                PlayerPoisonComponent comp = PlayerPoisonComponent.KEY.get(player);
                if (comp.poisonTicks > 0) return  GetInstinctHighlight.HighlightResult.always(Noellesroles.POISONER.color());
            }

            // PATHOGEN: 只有已感染的玩家显示绿色高亮（不再透视未感染玩家）
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.PATHOGEN)) {
                InfectedPlayerComponent infected = InfectedPlayerComponent.KEY.get(player);
                if (infected.isInfected() && localPlayer.canSee(player)) {
                    // Already infected - green
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.PATHOGEN.color());
                }
            }

            // REPORTER: 被标记的目标始终高亮显示（透视效果）
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.REPORTER)) {
                ReporterPlayerComponent reporterComp = ReporterPlayerComponent.KEY.get(localPlayer);
                if (reporterComp.isMarkedTarget(player.getUuid())) {
                    // 被标记的目标 - 使用记者颜色透视
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.REPORTER.color());
                }
            }

            // 如果目标是生存大师，阻止被杀手本能高亮
            if (gameWorldComponent.isRole(player, Noellesroles.SURVIVAL_MASTER) && !localPlayer.canSee(player)) {
                return GetInstinctHighlight.HighlightResult.skip();
            }

            // BODYGUARD: 保护目标始终高亮显示（透视效果）
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.BODYGUARD)) {
                BodyguardPlayerComponent bodyguardComp = BodyguardPlayerComponent.KEY.get(localPlayer);
                if (bodyguardComp.isCurrentTarget(player.getUuid())) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.BODYGUARD.color());
                }
            }

            // SERIAL_KILLER: 当前目标始终高亮显示（透视效果）
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.SERIAL_KILLER)) {
                SerialKillerPlayerComponent serialKillerComp = SerialKillerPlayerComponent.KEY.get(localPlayer);
                if (serialKillerComp.isCurrentTarget(player.getUuid())) {
                    if (!WatheClient.isInstinctEnabled() && isCommanderMarkedTarget(gameWorldComponent, localPlayer, player)) {
                        return null;
                    }
                    // 当前目标 - 使用连环杀手颜色透视
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.SERIAL_KILLER.color());
                }
            }

            if (gameWorldComponent.canUseKillerFeatures(localPlayer)) {
                if (gameWorldComponent.isRole(player, Noellesroles.COMMANDER) && player != localPlayer) {
                    return GetInstinctHighlight.HighlightResult.withKeybind(0x2E006B, GetInstinctHighlight.HighlightResult.PRIORITY_HIGH);
                }
            }
            return null;
        });
        // 注册 GetInstinctHighlight 监听器：秃鹫的本能高亮逻辑（尸体高亮）
        GetInstinctHighlight.EVENT.register(entity -> {
            if (!(entity instanceof PlayerBodyEntity)) return null;
            if (MinecraftClient.getInstance().player == null) return null;
            if (!WatheClient.isPlayerPlayingAndAlive()) return null;
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(
                    MinecraftClient.getInstance().player.getWorld()
            );
            PlayerEntity localPlayer = MinecraftClient.getInstance().player;
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.VULTURE))
                return GetInstinctHighlight.HighlightResult.withKeybind(Noellesroles.VULTURE.color());
            return null;
        });
        // 注册 GetInstinctHighlight 监听器：秃鹫吃尸体后透视所有存活玩家
        GetInstinctHighlight.EVENT.register(entity -> {
            if (!(entity instanceof PlayerBodyEntity body)) return null;
            if (MinecraftClient.getInstance().player == null) return null;
            if (!WatheClient.isPlayerPlayingAndAlive()) return null;

            PlayerEntity localPlayer = MinecraftClient.getInstance().player;
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(localPlayer.getWorld());
            if (!gameWorldComponent.isRole(localPlayer, Noellesroles.FERRYMAN)) return null;

            FerrymanPlayerComponent ferrymanComponent = FerrymanPlayerComponent.KEY.get(localPlayer);
            if (ferrymanComponent.hasFerriedBody(body.getUuid())) return null;

            int decomposedAge = GameConstants.TIME_TO_DECOMPOSITION + GameConstants.DECOMPOSING_TIME;
            if (body.age >= decomposedAge) return null;

            return GetInstinctHighlight.HighlightResult.withKeybind(Noellesroles.FERRYMAN.color());
        });
        GetInstinctHighlight.EVENT.register(entity -> {
            if (!(entity instanceof PlayerEntity player) || SpectatorStateHelper.isSpectatorLike(player)) return null;
            if (MinecraftClient.getInstance().player == null) return null;
            if (!WatheClient.isPlayerPlayingAndAlive()) return null;
            PlayerEntity localPlayer = MinecraftClient.getInstance().player;
            if (entity == localPlayer) return null;
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(localPlayer.getWorld());
            if (!gameWorldComponent.isRole(localPlayer, Noellesroles.VULTURE)) return null;
            VulturePlayerComponent vultureComp = VulturePlayerComponent.KEY.get(localPlayer);
            if (!vultureComp.hasActiveHighlight()) return null;
            if (!GameFunctions.isPlayerPlayingAndAlive(player)) return null;
            if (!vultureComp.isEntityWithinHighlightRange(player)) return null;
            return GetInstinctHighlight.HighlightResult.always(Noellesroles.VULTURE.color());
        });

        // 注册 GetInstinctHighlight 监听器：卧底角色高亮逻辑
        // 让杀手误认为卧底是同伙（按本能时显示红色）
        GetInstinctHighlight.EVENT.register(entity -> {
            if (!(entity instanceof PlayerEntity player) || SpectatorStateHelper.isSpectatorLike(player)) return null;
            if (MinecraftClient.getInstance().player == null) return null;

            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(
                    MinecraftClient.getInstance().player.getWorld()
            );
            PlayerEntity localPlayer = MinecraftClient.getInstance().player;

            // 只有当查看者是杀手时才生效
            if (!gameWorldComponent.canUseKillerFeatures(localPlayer)) return null;
            if (!GameFunctions.isPlayerPlayingAndAlive(localPlayer)) return null;

            // 如果目标是卧底，让杀手误以为是同伙（显示红色）
            if (gameWorldComponent.isRole(player, Noellesroles.UNDERCOVER)) {
                return GetInstinctHighlight.HighlightResult.withKeybind(MathHelper.hsvToRgb(0F, 1.0F, 0.6F), GetInstinctHighlight.HighlightResult.PRIORITY_HIGH);
            }

            return null;
        });

        // 注册 ShouldShowCohort 监听器：卧底角色cohort提示逻辑
        // 让杀手看向卧底时显示"cohort"（同伙）提示
        ShouldShowCohort.EVENT.register((viewer, target) -> {
            if (viewer == null || target == null) return null;
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(viewer.getWorld());
            HallucinationPlayerComponent hallucinationComponent = HallucinationPlayerComponent.KEY.get(viewer);

            // 只有当查看者是杀手时才生效
            if (!gameWorldComponent.canUseKillerFeatures(viewer)) return null;
            if (!GameFunctions.isPlayerPlayingAndAlive(viewer)) return null;

            if (HallucinationHelper.hasInstinctMisjudge(viewer, target)) {
                return HallucinationHelper.isInstinctMisjudgeTreatAsAlly(viewer, target)
                        ? ShouldShowCohort.CohortResult.show()
                        : null;
            }

            // 如果目标是卧底，显示cohort提示
            if (gameWorldComponent.isRole(target, Noellesroles.UNDERCOVER)) {
                return ShouldShowCohort.CohortResult.show();
            }

            return null; // 不处理，使用默认逻辑
        });

        GetInstinctHighlight.EVENT.register(entity -> {
            if (!(entity instanceof HunterTrapEntity trap)) return null;
            if (MinecraftClient.getInstance().player == null) return null;

            PlayerEntity localPlayer = MinecraftClient.getInstance().player;
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(localPlayer.getWorld());
            boolean isInGameSpectator = isTrapSpectatorViewer(localPlayer, gameWorld);
            if (!WatheClient.isPlayerPlayingAndAlive() && !isInGameSpectator) return null;
            if (!trap.canBeSeenBy(localPlayer)) return null;
            if (trap.isPoisoned()) {
                return GetInstinctHighlight.HighlightResult.withKeybind(0x0B6623);
            }
            if (gameWorld.isRole(localPlayer, WatheRoles.LOOSE_END)) {
                return GetInstinctHighlight.HighlightResult.withKeybind(WatheRoles.LOOSE_END.color());
            }
            if (!gameWorld.canUseKillerFeatures(localPlayer) && !isInGameSpectator) {
                return null;
            }

            return GetInstinctHighlight.HighlightResult.withKeybind(Noellesroles.HUNTER.color());
        });

        GetInstinctHighlight.EVENT.register(entity -> {
            if (!(entity instanceof PlayerEntity player) || SpectatorStateHelper.isSpectatorLike(player)) {
                return null;
            }
            if (MinecraftClient.getInstance().player == null || !WatheClient.isPlayerPlayingAndAlive()) {
                return null;
            }
            PlayerEntity localPlayer = MinecraftClient.getInstance().player;
            if (ClientHallucinationState.isDummyEntity(player)) {
                return null;
            }
            if (!ClientHallucinationState.hasDummyForSkin(player.getUuid())) {
                return null;
            }
            if (ClientHallucinationState.shouldSuppressDirectPlayerHighlight(player.getUuid())) {
                return GetInstinctHighlight.HighlightResult.skip();
            }

            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(localPlayer.getWorld());

            // 黑帮巨星的本能不区分目标身份，幻觉替身存在时也保持统一深紫色。
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.GANGSTER_STAR) && player != localPlayer) {
                if (gameWorldComponent.isRole(player, Noellesroles.SURVIVAL_MASTER) && !localPlayer.canSee(player)) {
                    return GetInstinctHighlight.HighlightResult.skip();
                }
                return GetInstinctHighlight.HighlightResult.withKeybind(
                        Noellesroles.GANGSTER_STAR.color(),
                        GetInstinctHighlight.HighlightResult.PRIORITY_HIGH + 1
                );
            }

            if (HallucinationHelper.hasInstinctMisjudge(localPlayer, player)) {
                int color = HallucinationHelper.isInstinctMisjudgeTreatAsAlly(localPlayer, player)
                        ? MathHelper.hsvToRgb(0F, 1.0F, 0.6F)
                        : Color.GREEN.getRGB();
                return GetInstinctHighlight.HighlightResult.always(color, GetInstinctHighlight.HighlightResult.PRIORITY_HIGH);
            }

            GetInstinctHighlight.HighlightResult commanderMarkedHighlight =
                    getCommanderMarkedHighlight(gameWorldComponent, localPlayer, player);
            if (commanderMarkedHighlight != null) {
                return commanderMarkedHighlight;
            }

            if (gameWorldComponent.isRole(localPlayer, Noellesroles.CORRUPT_COP)) {
                var comp = CorruptCopPlayerComponent.KEY.get(localPlayer);
                if (comp.canSeePlayersThroughWalls()) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.CORRUPT_COP.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.BOMBER)) {
                BomberPlayerComponent comp = BomberPlayerComponent.KEY.get(player);
                if (comp.hasBomb()) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.BOMBER.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.JESTER)) {
                JesterPlayerComponent jesterComponent = JesterPlayerComponent.KEY.get(localPlayer);
                if (jesterComponent.inPsychoMode && player.getUuid().equals(jesterComponent.targetKiller)) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.JESTER.color());
                }
            }
            if (gameWorldComponent.isRole(player, Noellesroles.JESTER)) {
                JesterPlayerComponent jesterComponent = JesterPlayerComponent.KEY.get(player);
                if (jesterComponent.inPsychoMode) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.JESTER.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.BARTENDER)) {
                BartenderPlayerComponent comp = BartenderPlayerComponent.KEY.get(player);
                if (comp.glowTicks > 0) {
                    return GetInstinctHighlight.HighlightResult.always(Color.GREEN.getRGB());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.PROFESSOR)) {
                IronManPlayerComponent comp = IronManPlayerComponent.KEY.get(player);
                if (comp.hasBuff()) {
                    return GetInstinctHighlight.HighlightResult.always(Color.BLUE.getRGB());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.TOXICOLOGIST)) {
                PlayerPoisonComponent comp = PlayerPoisonComponent.KEY.get(player);
                if (comp.poisonTicks > 0) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.TOXICOLOGIST.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.ORTHOPEDIST)) {
                OrthopedistPlayerComponent comp = OrthopedistPlayerComponent.KEY.get(player);
                if (comp.hasBoneSettingActive()) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.ORTHOPEDIST.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.POISONER)) {
                PlayerPoisonComponent comp = PlayerPoisonComponent.KEY.get(player);
                if (comp.poisonTicks > 0) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.POISONER.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.PATHOGEN)) {
                InfectedPlayerComponent infected = InfectedPlayerComponent.KEY.get(player);
                if (infected.isInfected()) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.PATHOGEN.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.REPORTER)) {
                ReporterPlayerComponent reporterComp = ReporterPlayerComponent.KEY.get(localPlayer);
                if (reporterComp.isMarkedTarget(player.getUuid())) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.REPORTER.color());
                }
            }
            if (gameWorldComponent.isRole(player, Noellesroles.SURVIVAL_MASTER)) {
                return GetInstinctHighlight.HighlightResult.skip();
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.BODYGUARD)) {
                BodyguardPlayerComponent bodyguardComp = BodyguardPlayerComponent.KEY.get(localPlayer);
                if (bodyguardComp.isCurrentTarget(player.getUuid())) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.BODYGUARD.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.SERIAL_KILLER)) {
                SerialKillerPlayerComponent serialKillerComp = SerialKillerPlayerComponent.KEY.get(localPlayer);
                if (serialKillerComp.isCurrentTarget(player.getUuid())) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.SERIAL_KILLER.color());
                }
            }
            if (gameWorldComponent.canUseKillerFeatures(localPlayer)
                    && gameWorldComponent.isRole(player, Noellesroles.COMMANDER)
                    && player != localPlayer) {
                return GetInstinctHighlight.HighlightResult.withKeybind(0x2E006B, GetInstinctHighlight.HighlightResult.PRIORITY_HIGH);
            }
            return null;
        });

        GetInstinctHighlight.EVENT.register(entity -> {
            if (!(entity instanceof OtherClientPlayerEntity dummyPlayer) || !ClientHallucinationState.isDummyEntity(dummyPlayer)) {
                return null;
            }
            if (MinecraftClient.getInstance().player == null || !WatheClient.isPlayerPlayingAndAlive()) {
                return null;
            }

            PlayerEntity localPlayer = MinecraftClient.getInstance().player;
            PlayerEntity referencePlayer = localPlayer.getWorld().getPlayerByUuid(dummyPlayer.getGameProfile().getId());
            if (referencePlayer == null || SpectatorStateHelper.isSpectatorLike(referencePlayer)) {
                return null;
            }

            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(localPlayer.getWorld());
            boolean instinctEnabled = WatheClient.isInstinctEnabled();

            // 黑帮巨星看幻觉假人时也使用与角色颜色一致的深紫色本能轮廓。
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.GANGSTER_STAR)) {
                if (gameWorldComponent.isRole(referencePlayer, Noellesroles.SURVIVAL_MASTER)
                        && !localPlayer.canSee(referencePlayer)) {
                    return GetInstinctHighlight.HighlightResult.skip();
                }
                return GetInstinctHighlight.HighlightResult.withKeybind(
                        Noellesroles.GANGSTER_STAR.color(),
                        GetInstinctHighlight.HighlightResult.PRIORITY_HIGH + 1
                );
            }

            if (HallucinationHelper.hasInstinctMisjudge(localPlayer, referencePlayer)) {
                int color = HallucinationHelper.isInstinctMisjudgeTreatAsAlly(localPlayer, referencePlayer)
                        ? MathHelper.hsvToRgb(0F, 1.0F, 0.6F)
                        : Color.GREEN.getRGB();
                return GetInstinctHighlight.HighlightResult.always(color, GetInstinctHighlight.HighlightResult.PRIORITY_HIGH);
            }

            GetInstinctHighlight.HighlightResult commanderMarkedHighlight =
                    getCommanderMarkedHighlight(gameWorldComponent, localPlayer, referencePlayer);
            if (commanderMarkedHighlight != null) {
                return commanderMarkedHighlight;
            }

            if (gameWorldComponent.isRole(localPlayer, Noellesroles.CORRUPT_COP)) {
                var comp = CorruptCopPlayerComponent.KEY.get(localPlayer);
                if (comp.canSeePlayersThroughWalls()) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.CORRUPT_COP.color());
                }
            }
            if (!instinctEnabled
                    && !gameWorldComponent.isRole(localPlayer, Noellesroles.BOMBER)
                    && !gameWorldComponent.isRole(localPlayer, Noellesroles.POISONER)
                    && !gameWorldComponent.isRole(localPlayer, Noellesroles.TOXICOLOGIST)
                    && !gameWorldComponent.isRole(localPlayer, Noellesroles.BARTENDER)
                    && !gameWorldComponent.isRole(localPlayer, Noellesroles.PROFESSOR)
                    && !gameWorldComponent.isRole(localPlayer, Noellesroles.ORTHOPEDIST)
                    && !gameWorldComponent.isRole(localPlayer, Noellesroles.PATHOGEN)
                    && !gameWorldComponent.isRole(localPlayer, Noellesroles.REPORTER)
                    && !gameWorldComponent.isRole(localPlayer, Noellesroles.BODYGUARD)
                    && !gameWorldComponent.isRole(localPlayer, Noellesroles.SERIAL_KILLER)
                    && !gameWorldComponent.isRole(localPlayer, Noellesroles.JESTER)) {
                return null;
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.BOMBER)) {
                UUID dummyId = ClientHallucinationState.getDummyId(dummyPlayer);
                BomberPlayerComponent comp = BomberPlayerComponent.KEY.get(referencePlayer);
                if (comp.hasBomb() || ClientHallucinationState.dummyHasBomb(dummyId)) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.BOMBER.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.JESTER)) {
                JesterPlayerComponent jesterComponent = JesterPlayerComponent.KEY.get(localPlayer);
                if (jesterComponent.inPsychoMode && referencePlayer.getUuid().equals(jesterComponent.targetKiller)) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.JESTER.color());
                }
            }
            if (gameWorldComponent.isRole(referencePlayer, Noellesroles.JESTER)) {
                JesterPlayerComponent jesterComponent = JesterPlayerComponent.KEY.get(referencePlayer);
                if (jesterComponent.inPsychoMode) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.JESTER.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.BARTENDER)) {
                BartenderPlayerComponent comp = BartenderPlayerComponent.KEY.get(referencePlayer);
                if (comp.glowTicks > 0) {
                    return GetInstinctHighlight.HighlightResult.always(Color.GREEN.getRGB());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.PROFESSOR)) {
                IronManPlayerComponent comp = IronManPlayerComponent.KEY.get(referencePlayer);
                if (comp.hasBuff()) {
                    return GetInstinctHighlight.HighlightResult.always(Color.BLUE.getRGB());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.TOXICOLOGIST)) {
                UUID dummyId = ClientHallucinationState.getDummyId(dummyPlayer);
                PlayerPoisonComponent comp = PlayerPoisonComponent.KEY.get(referencePlayer);
                if (comp.poisonTicks > 0 || ClientHallucinationState.dummyIsPoisoned(dummyId)) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.TOXICOLOGIST.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.ORTHOPEDIST)) {
                OrthopedistPlayerComponent comp = OrthopedistPlayerComponent.KEY.get(referencePlayer);
                if (comp.hasBoneSettingActive()) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.ORTHOPEDIST.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.POISONER)) {
                UUID dummyId = ClientHallucinationState.getDummyId(dummyPlayer);
                PlayerPoisonComponent comp = PlayerPoisonComponent.KEY.get(referencePlayer);
                if (comp.poisonTicks > 0 || ClientHallucinationState.dummyIsPoisoned(dummyId)) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.POISONER.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.PATHOGEN)) {
                InfectedPlayerComponent infected = InfectedPlayerComponent.KEY.get(referencePlayer);
                if (infected.isInfected()) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.PATHOGEN.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.REPORTER)) {
                ReporterPlayerComponent reporterComp = ReporterPlayerComponent.KEY.get(localPlayer);
                if (reporterComp.isMarkedTarget(referencePlayer.getUuid())) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.REPORTER.color());
                }
            }
            if (gameWorldComponent.isRole(referencePlayer, Noellesroles.SURVIVAL_MASTER)) {
                return GetInstinctHighlight.HighlightResult.skip();
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.BODYGUARD)) {
                BodyguardPlayerComponent bodyguardComp = BodyguardPlayerComponent.KEY.get(localPlayer);
                if (bodyguardComp.isCurrentTarget(referencePlayer.getUuid())) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.BODYGUARD.color());
                }
            }
            if (gameWorldComponent.isRole(localPlayer, Noellesroles.SERIAL_KILLER)) {
                SerialKillerPlayerComponent serialKillerComp = SerialKillerPlayerComponent.KEY.get(localPlayer);
                if (serialKillerComp.isCurrentTarget(referencePlayer.getUuid())) {
                    return GetInstinctHighlight.HighlightResult.always(Noellesroles.SERIAL_KILLER.color());
                }
            }
            if (gameWorldComponent.canUseKillerFeatures(localPlayer)
                    && gameWorldComponent.isRole(referencePlayer, Noellesroles.COMMANDER)
                    && referencePlayer != localPlayer) {
                return GetInstinctHighlight.HighlightResult.withKeybind(0x2E006B, GetInstinctHighlight.HighlightResult.PRIORITY_HIGH);
            }
            return null;
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null) {
                return;
            }

            RiotPatrolPlayerComponent riotPatrolComponent = RiotPatrolPlayerComponent.KEY.get(player);
            if (riotPatrolComponent.isRooted()) {
                client.options.forwardKey.setPressed(false);
                client.options.backKey.setPressed(false);
                client.options.leftKey.setPressed(false);
                client.options.rightKey.setPressed(false);
                client.options.jumpKey.setPressed(false);
                client.options.sprintKey.setPressed(false);
                client.options.sneakKey.setPressed(false);
            }

            HunterPlayerComponent hunterComponent = HunterPlayerComponent.KEY.get(player);
            if (hunterComponent.isTrapped()) {
                client.options.forwardKey.setPressed(false);
                client.options.backKey.setPressed(false);
                client.options.leftKey.setPressed(false);
                client.options.rightKey.setPressed(false);
                client.options.jumpKey.setPressed(false);
                client.options.sprintKey.setPressed(false);
                client.options.sneakKey.setPressed(false);
            }

            if (hunterComponent.getFractureLayers() > 0) {
                client.options.sprintKey.setPressed(false);
                player.setSprinting(false);
            }

            if (player.getMainHandStack().isOf(ModItems.RIOT_SHIELD)
                    && player.getItemCooldownManager().isCoolingDown(ModItems.RIOT_SHIELD)) {
                client.options.attackKey.setPressed(false);
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FogOfWarClientHelper.tickClientState(client);
            // 更新世界BGM管理器
            WorldMusicManager.tick();
            SilencedTalkBubbleCleaner.tick(client);

            if (client.world != null) {
                SoundPhysicsConfigLockManager.updateFromWorld(ConfigWorldComponent.KEY.get(client.world));
                TalkBubblesConfigLockManager.updateFromWorld(ConfigWorldComponent.KEY.get(client.world));
            } else {
                SoundPhysicsConfigLockManager.deactivate();
                TalkBubblesConfigLockManager.deactivate();
            }
            SoundPhysicsConfigLockManager.tick(client);
            TalkBubblesConfigLockManager.tick(client);

            insanityTime++;
            if (insanityTime >= 20*6) {
                insanityTime = 0;
                List<UUID> keys = new ArrayList<>(WatheClient.PLAYER_ENTRIES_CACHE.keySet());
                List<UUID> originalkeys = new ArrayList<>(WatheClient.PLAYER_ENTRIES_CACHE.keySet());
                Collections.shuffle(keys);
                SHUFFLED_PLAYER_ENTRIES_CACHE.clear();
                int i = 0;
                for (UUID o : originalkeys) {
                    SHUFFLED_PLAYER_ENTRIES_CACHE.put(o, keys.get(i));
                    i++;
                }
            }

            // 更新病原体最近目标
            if (MinecraftClient.getInstance().player != null) {
                ClientPlayerEntity bodyPlayer = MinecraftClient.getInstance().player;
                GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(bodyPlayer.getWorld());
                double bodyRange = BodyTargetHelper.getTargetRange(bodyPlayer);
                NoellesrolesClient.targetBody = BodyTargetHelper.findTargetBody(bodyPlayer, bodyRange, body -> {
                    if (gameWorldComponent.isRole(bodyPlayer, Noellesroles.FERRYMAN)) {
                        FerrymanPlayerComponent ferrymanComponent = FerrymanPlayerComponent.KEY.get(bodyPlayer);
                        int decomposedAge = GameConstants.TIME_TO_DECOMPOSITION + GameConstants.DECOMPOSING_TIME;
                        return !ferrymanComponent.hasFerriedBody(body.getUuid()) && body.age < decomposedAge;
                    }
                    return true;
                });
                if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.PATHOGEN)) {
                    pathogenNearestTarget = null;
                    pathogenNearestTargetDistance = Double.MAX_VALUE;
                    pathogenTargetDirection = "";
                    pathogenTargetVertical = "";
                    PlayerEntity localPlayer = MinecraftClient.getInstance().player;

                    // 找到最近的未感染玩家（不限距离，用于指南针指向）
                    for (PlayerEntity player : localPlayer.getWorld().getPlayers()) {
                        if (player.equals(localPlayer)) continue;
                        if (SpectatorStateHelper.isSpectatorLike(player) || player.isCreative()) continue;
                        // 检查玩家是否有角色（在游戏中）
                        if (!gameWorldComponent.hasAnyRole(player)) continue;

                        InfectedPlayerComponent infected = InfectedPlayerComponent.KEY.get(player);
                        if (infected.isInfected()) continue;

                        double distance = localPlayer.squaredDistanceTo(player);
                        if (distance < pathogenNearestTargetDistance) {
                            pathogenNearestTargetDistance = distance;
                            pathogenNearestTarget = player;
                        }
                    }

                    // 计算方向
                    if (pathogenNearestTarget != null) {
                        pathogenNearestTargetDistance = Math.sqrt(pathogenNearestTargetDistance);

                        // 计算从玩家指向目标的方向
                        double dx = pathogenNearestTarget.getX() - localPlayer.getX();
                        double dy = pathogenNearestTarget.getY() - localPlayer.getY();
                        double dz = pathogenNearestTarget.getZ() - localPlayer.getZ();

                        // 计算目标相对于玩家视角的角度
                        double targetAngle = Math.toDegrees(Math.atan2(-dx, dz));
                        double playerYaw = localPlayer.getYaw() % 360;
                        if (playerYaw < 0) playerYaw += 360;
                        if (targetAngle < 0) targetAngle += 360;

                        // 计算相对角度（目标相对于玩家面朝方向）
                        double relativeAngle = targetAngle - playerYaw;
                        if (relativeAngle < -180) relativeAngle += 360;
                        if (relativeAngle > 180) relativeAngle -= 360;

                        // 根据相对角度确定方向箭头
                        if (relativeAngle >= -22.5 && relativeAngle < 22.5) {
                            pathogenTargetDirection = "↑"; // 前方
                        } else if (relativeAngle >= 22.5 && relativeAngle < 67.5) {
                            pathogenTargetDirection = "↗"; // 右前方
                        } else if (relativeAngle >= 67.5 && relativeAngle < 112.5) {
                            pathogenTargetDirection = "→"; // 右方
                        } else if (relativeAngle >= 112.5 && relativeAngle < 157.5) {
                            pathogenTargetDirection = "↘"; // 右后方
                        } else if (relativeAngle >= 157.5 || relativeAngle < -157.5) {
                            pathogenTargetDirection = "↓"; // 后方
                        } else if (relativeAngle >= -157.5 && relativeAngle < -112.5) {
                            pathogenTargetDirection = "↙"; // 左后方
                        } else if (relativeAngle >= -112.5 && relativeAngle < -67.5) {
                            pathogenTargetDirection = "←"; // 左方
                        } else {
                            pathogenTargetDirection = "↖"; // 左前方
                        }

                        // 计算垂直位置提示（高度差超过2格才提示）
                        if (dy > 2) {
                            pathogenTargetVertical = "↑"; // 上方
                        } else if (dy < -2) {
                            pathogenTargetVertical = "↓"; // 下方
                        } else {
                            pathogenTargetVertical = ""; // 同一高度
                        }
                    }
                } else {
                    pathogenNearestTarget = null;
                    pathogenNearestTargetDistance = 0;
                    pathogenTargetDirection = "";
                    pathogenTargetVertical = "";
                }

                crosshairTarget = null;
                crosshairTargetDistance = 0;
                PlayerEntity localPlayer = MinecraftClient.getInstance().player;
                double maxDistance = 10.0; // 最大检测距离
                var eyePos = localPlayer.getEyePos();
                var hitResult = ProjectileUtil.getCollision(
                        localPlayer,
                        entity -> entity instanceof PlayerEntity player
                                && GameFunctions.isPlayerPlayingAndAlive(player)
                                && !HallucinationClientVisibilityHelper.shouldHidePlayer(localPlayer, player)
                                && !SwallowedInteractionHelper.blocksPlayerTargetForViewer(localPlayer, player),
                        maxDistance
                );

                if (hitResult instanceof EntityHitResult entityHitResult&& entityHitResult.getEntity() instanceof PlayerEntity targetPlayer) {
                    crosshairTarget = targetPlayer;
                    crosshairTargetDistance = eyePos.distanceTo(entityHitResult.getPos());
                } else if (hitResult instanceof BlockHitResult blockHitResult){
                    Optional<PlayerEntity> sleepingPlayer = findSleepingPlayerOnBed(localPlayer.getWorld(), blockHitResult);
                    if (sleepingPlayer.isPresent()
                            && sleepingPlayer.get() != localPlayer
                            && !HallucinationClientVisibilityHelper.shouldHidePlayer(localPlayer, sleepingPlayer.get())
                            && !SwallowedInteractionHelper.blocksPlayerTargetForViewer(localPlayer, sleepingPlayer.get())) {
                        crosshairTarget = sleepingPlayer.get();
                        crosshairTargetDistance = eyePos.distanceTo(blockHitResult.getPos());
                    }
                }
            }

            ClientPlayerEntity spectatorCandidate = MinecraftClient.getInstance().player;
            if (spectatorCandidate != null) {
                GameWorldComponent spectatorWorld = GameWorldComponent.KEY.get(spectatorCandidate.getWorld());
                boolean isInGameSpectator = SpectatorStateHelper.isInGameRealSpectator(spectatorCandidate, spectatorWorld);
                if (isInGameSpectator) {
                    if (!wasDeadSpectatorLastTick) {
                        SpectatorReplayToastOverlay.beginSpectatorSession();
                    }
                    long nowTick = spectatorCandidate.getWorld().getTime();
                    if (nextSpectatorReplayPollTick == Long.MAX_VALUE) {
                        nextSpectatorReplayPollTick = nowTick;
                    }
                    if (nowTick >= nextSpectatorReplayPollTick) {
                        spectatorReplayPollRequestId++;
                        ClientPlayNetworking.send(new SpectatorInfoRequestC2SPacket(
                                spectatorReplayPollRequestId,
                                SpectatorReplayToastOverlay.getLastSeenReplayTick()
                        ));
                        nextSpectatorReplayPollTick = nowTick + 20L;
                    }
                } else {
                    nextSpectatorReplayPollTick = Long.MAX_VALUE;
                }
                wasDeadSpectatorLastTick = isInGameSpectator;
            } else {
                wasDeadSpectatorLastTick = false;
            }

            if (abilityBind.wasPressed()) {
                client.execute(() -> {
                    ClientPlayerEntity localPlayer = client.player;
                    if (localPlayer == null) return;
                    GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(localPlayer.getWorld());

                    // 按 H 打开角色信息界面
                    // (此处不处理，下方单独处理)

                    // 刺客角色按G打开刺客界面
                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.ASSASSIN)) {
                        if (GameFunctions.isPlayerPlayingAndAlive(localPlayer) && !SwallowedPlayerComponent.isPlayerSwallowed(localPlayer)) {
                            AssassinPlayerComponent assassinComp = AssassinPlayerComponent.KEY.get(localPlayer);
                            // 检查是否可以使用技能（不在冷却中且有剩余次数）
                            if (assassinComp.canGuess()) {
                                client.setScreen(new AssassinScreen(localPlayer));
                            }
                            // 如果不能使用，不打开界面，HUD 会显示相应的提示信息
                        }
                        return;
                    }

                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.CRIMINAL_REASONER)) {
                        if (GameFunctions.isPlayerPlayingAndAlive(localPlayer)
                                && !SwallowedPlayerComponent.isPlayerSwallowed(localPlayer)) {
                            AbilityPlayerComponent abilityComp = AbilityPlayerComponent.KEY.get(localPlayer);
                            if (abilityComp.getCooldown() > 0) {
                                return;
                            }
                            // 犯罪推理学家按G打开推理菜单，冷却中则不打开界面
                            client.setScreen(new CriminalReasonerScreen(localPlayer));
                        }
                        return;
                    }

                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.VOODOO)) {
                        if (GameFunctions.isPlayerPlayingAndAlive(localPlayer)
                                && !SwallowedPlayerComponent.isPlayerSwallowed(localPlayer)
                                && AbilityPlayerComponent.KEY.get(localPlayer).getCooldown() <= 0) {
                            client.setScreen(new RoleTargetMenuScreen(localPlayer, RoleTargetMenuScreen.MenuType.VOODOO));
                        }
                        return;
                    }

                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.SWAPPER)) {
                        if (GameFunctions.isPlayerPlayingAndAlive(localPlayer)
                                && !SwallowedPlayerComponent.isPlayerSwallowed(localPlayer)
                                && AbilityPlayerComponent.KEY.get(localPlayer).getCooldown() <= 0) {
                            client.setScreen(new RoleTargetMenuScreen(localPlayer, RoleTargetMenuScreen.MenuType.SWAPPER));
                        }
                        return;
                    }

                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.VULTURE)) {
                        if (!GameFunctions.isPlayerPlayingAndAlive(localPlayer) || SwallowedPlayerComponent.isPlayerSwallowed(localPlayer)) return;
                        if (targetBody == null) return;
                        ClientPlayNetworking.send(new VultureEatC2SPacket(targetBody.getUuid()));
                        return;
                    }

                    // 记者角色按G发送标记数据包
                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.FERRYMAN)) {
                        if (!GameFunctions.isPlayerPlayingAndAlive(localPlayer) || SwallowedPlayerComponent.isPlayerSwallowed(localPlayer)) return;
                        ClientPlayNetworking.send(new AbilityC2SPacket());
                        return;
                    }

                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.REPORTER)) {
                        if (crosshairTarget != null && crosshairTargetDistance <= 3.0) {
                            ClientPlayNetworking.send(new ReporterMarkC2SPacket(crosshairTarget.getUuid()));
                        }
                        return;
                    }

                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.COMMANDER)) {
                        if (GameFunctions.isPlayerPlayingAndAlive(localPlayer)
                                && !SwallowedPlayerComponent.isPlayerSwallowed(localPlayer)) {
                            AbilityPlayerComponent abilityComp = AbilityPlayerComponent.KEY.get(localPlayer);
                            CommanderPlayerComponent commanderComp = CommanderPlayerComponent.KEY.get(localPlayer);
                            if (abilityComp.getCooldown() <= 0 && commanderComp.canMarkMore()) {
                                client.setScreen(new CommanderScreen(localPlayer));
                            }
                        }
                        return;
                    }

                    // 变形者角色按G：切换尸体模式（独立于换皮变形）
                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.MORPHLING)) {
                        ClientPlayNetworking.send(new MorphCorpseToggleC2SPacket());
                        return;
                    }

                    // 饕餮角色按G吞噬准星目标
                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.TAOTIE)) {
                        if (crosshairTarget != null && crosshairTargetDistance <= 3.0) {
                            TaotiePlayerComponent taotieComp = TaotiePlayerComponent.KEY.get(localPlayer);
                            if (taotieComp.getSwallowCooldown() <= 0) {
                                ClientPlayNetworking.send(new TaotieSwallowC2SPacket(crosshairTarget.getUuid()));
                            }
                        }
                        return;
                    }

                    // 静语者角色按G：第一次标记目标，第二次释放沉默
                    // 静语者角色按G：第一次标记目标，第二次释放沉默
                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.SILENCER)) {
                        AbilityPlayerComponent abilityComp = AbilityPlayerComponent.KEY.get(localPlayer);
                        if (abilityComp.getCooldown() <= 0) {
                            SilencerPlayerComponent silencerComp = SilencerPlayerComponent.KEY.get(localPlayer);
                            if (silencerComp.hasMarkedTarget()) {
                                // 已有标记 → 发送释放沉默请求（不判断瞄准）
                                ClientPlayNetworking.send(new SilencerSilenceC2SPacket(silencerComp.getMarkedTargetUuid()));
                            } else if (crosshairTarget != null && crosshairTargetDistance <= 3.0) {
                                // 没有标记 → 发送标记请求
                                ClientPlayNetworking.send(new SilencerSilenceC2SPacket(crosshairTarget.getUuid()));
                            }
                        }
                        return;
                    }

                    ClientPlayNetworking.send(new AbilityC2SPacket());
                });
            }
            if (ability2Bind != null && ability2Bind.wasPressed()) {
                client.execute(() -> {
                    ClientPlayerEntity localPlayer = client.player;
                    if (localPlayer == null) return;
                    GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(localPlayer.getWorld());

                    if (gameWorldComponent.isRole(localPlayer, Noellesroles.MORPHLING)) {
                        if (GameFunctions.isPlayerPlayingAndAlive(localPlayer)
                                && !SwallowedPlayerComponent.isPlayerSwallowed(localPlayer)
                                && MorphlingPlayerComponent.KEY.get(localPlayer).getMorphTicks() == 0) {
                            client.setScreen(new RoleTargetMenuScreen(localPlayer, RoleTargetMenuScreen.MenuType.MORPHLING));
                        }
                    }
                });
            }
            boolean isRoleInfoPressed = roleInfoBind != null && roleInfoBind.isPressed();
            if (isRoleInfoPressed && !wasRoleInfoPressed) {
                if (MinecraftClient.getInstance().player == null) {
                    wasRoleInfoPressed = true;
                    return;
                }
                GameWorldComponent gwc = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());

                if (MinecraftClient.getInstance().currentScreen instanceof RoleInfoScreen) {
                    MinecraftClient.getInstance().setScreen(null);
                } else if (MinecraftClient.getInstance().currentScreen == null) {
                    if (!gwc.isRunning()) {
                        MinecraftClient.getInstance().setScreen(new RoleInfoScreen());
                        return;
                    }
                    boolean isAlive = GameFunctions.isPlayerPlayingAndAlive(MinecraftClient.getInstance().player);
                    boolean isSwallowed = SwallowedPlayerComponent.isPlayerSwallowed(MinecraftClient.getInstance().player);
                    boolean hasRole = gwc.hasAnyRole(MinecraftClient.getInstance().player);
                    boolean isDeadSpectator = gwc.isRunning()
                            && SpectatorStateHelper.isRealSpectator(MinecraftClient.getInstance().player);
                    boolean canOpenRoleInfo = (hasRole && (isAlive || isSwallowed)) || isDeadSpectator;

                    if (canOpenRoleInfo) {
                        MinecraftClient.getInstance().setScreen(new RoleInfoScreen());
                    }
                }
            }
            wasRoleInfoPressed = isRoleInfoPressed;

            boolean isAssistPressed = assistInterfaceBind != null && assistInterfaceBind.isPressed();
            if (isAssistPressed && !wasAssistInterfacePressed) {
                if (MinecraftClient.getInstance().player == null) {
                    wasAssistInterfacePressed = true;
                    return;
                }
                GameWorldComponent gwc = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());

                if (MinecraftClient.getInstance().currentScreen instanceof SpectatorAssistPanelScreen) {
                    MinecraftClient.getInstance().setScreen(null);
                } else if (MinecraftClient.getInstance().currentScreen == null) {
                    boolean isDeadSpectator = gwc.isRunning()
                            && SpectatorStateHelper.isRealSpectator(MinecraftClient.getInstance().player);
                    if (isDeadSpectator) {
                        MinecraftClient.getInstance().setScreen(new SpectatorAssistPanelScreen());
                    }
                }
            }
            wasAssistInterfacePressed = isAssistPressed;

            boolean isDeathArenaPressed = deathArenaToggleBind != null && deathArenaToggleBind.isPressed();
            if (isDeathArenaPressed && !wasDeathArenaTogglePressed && MinecraftClient.getInstance().currentScreen == null) {
                ClientPlayNetworking.send(new DeathArenaToggleC2SPacket());
            }
            wasDeathArenaTogglePressed = isDeathArenaPressed;

            boolean isConfigScreenPressed = configScreenBind != null && configScreenBind.isPressed();
            if (isConfigScreenPressed && !wasConfigScreenPressed) {
                MinecraftClient minecraftClient = MinecraftClient.getInstance();
                if (minecraftClient.currentScreen instanceof NoellesRolesConfigScreen) {
                    markConfigScreenKeyHandled();
                    minecraftClient.currentScreen.close();
                } else if (minecraftClient.currentScreen == null) {
                    minecraftClient.setScreen(NoellesRolesConfigScreenFactory.create(null));
                    markConfigScreenKeyHandled();
                }
            }
            wasConfigScreenPressed = isConfigScreenPressed;

            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                MurderMayhemIntroHudRenderer.tick(player);
                boolean swallowed = SwallowedPlayerComponent.isPlayerSwallowed(player);
                boolean looseEndOpeningPhase = LooseEndPlayerComponent.KEY.get(player).isOpeningPhased();
                if (swallowed) {
                    if (!wasClientPlayerSwallowedLastTick) {
                        swallowedLockedYaw = player.getYaw();
                        swallowedLockedPitch = player.getPitch();
                    }
                    if (swallowedLockedSelectedSlot < 0) {
                        swallowedLockedSelectedSlot = player.getInventory().selectedSlot;
                    } else {
                        player.getInventory().selectedSlot = swallowedLockedSelectedSlot;
                    }
                    player.prevYaw = swallowedLockedYaw;
                    player.prevPitch = swallowedLockedPitch;
                    player.setYaw(swallowedLockedYaw);
                    player.setPitch(swallowedLockedPitch);
                    player.headYaw = swallowedLockedYaw;
                    player.bodyYaw = swallowedLockedYaw;
                    if (client.options.attackKey.isPressed()) {
                        while (client.options.attackKey.wasPressed()) {
                        }
                    }
                    if (client.options.useKey.isPressed()) {
                        while (client.options.useKey.wasPressed()) {
                        }
                    }
                    if (client.options.hotbarKeys != null) {
                        for (var keyBinding : client.options.hotbarKeys) {
                            while (keyBinding.wasPressed()) {
                            }
                        }
                    }
                    player.input.movementForward = 0.0F;
                    player.input.movementSideways = 0.0F;
                    player.input.jumping = false;
                    player.input.sneaking = false;
                    player.input.pressingForward = false;
                    player.input.pressingBack = false;
                    player.input.pressingLeft = false;
                    player.input.pressingRight = false;
                    player.setSprinting(false);
                    player.noClip = true;
                    player.setInvisible(true);
                    player.setVelocity(0.0, 0.0, 0.0);
                    client.gameRenderer.setRenderHand(false);
                } else if (looseEndOpeningPhase) {
                    player.noClip = false;
                    player.setInvisible(true);
                    player.setOnGround(true);
                    player.fallDistance = 0.0F;
                    client.gameRenderer.setRenderHand(false);
                } else {
                    swallowedLockedSelectedSlot = -1;
                    if (wasClientPlayerSwallowedLastTick) {
                        player.noClip = false;
                        player.setInvisible(false);
                        client.gameRenderer.setRenderHand(true);
                    } else {
                        player.noClip = false;
                        player.setInvisible(false);
                        client.gameRenderer.setRenderHand(true);
                    }
                }
                wasClientPlayerSwallowedLastTick = swallowed;

                JesterTimeRenderer.tick();

                // 切换到不可见物品时在 actionbar 提示
                boolean holdingInvisible = WatheClient.isPlayerPlayingAndAlive()
                        && (HiddenEquipmentHelper.shouldHideItem(player.getMainHandStack(), player)
                            || player.getMainHandStack().isOf(WatheItems.NOTE));
                if (holdingInvisible && !wasHoldingInvisible) {
                    player.sendMessage(Text.translatable("tip.item.invisible_in_hand").withColor(0xAAAAAA), true);
                }
                wasHoldingInvisible = holdingInvisible;
            }
        });
    }

    private static GetInstinctHighlight.HighlightResult getCommanderMarkedHighlight(GameWorldComponent gameWorldComponent, PlayerEntity localPlayer, PlayerEntity targetPlayer) {
        if (!canSeeCommanderMarkedTargets(gameWorldComponent, localPlayer)) {
            return null;
        }
        if (WatheClient.isInstinctEnabled() && !gameWorldComponent.isRole(localPlayer, Noellesroles.UNDERCOVER)) {
            return null;
        }
        if (getMarkingCommander(gameWorldComponent, localPlayer, targetPlayer.getUuid()) == null) {
            return null;
        }

        return GetInstinctHighlight.HighlightResult.always(
                COMMANDER_MARK_HIGHLIGHT_COLOR,
                GetInstinctHighlight.HighlightResult.PRIORITY_HIGH
        );
    }

    private static boolean isCommanderMarkedTarget(GameWorldComponent gameWorldComponent, PlayerEntity localPlayer, PlayerEntity targetPlayer) {
        return getMarkingCommander(gameWorldComponent, localPlayer, targetPlayer.getUuid()) != null;
    }

    // 返回将 targetUuid 标记为威胁的任一指挥官，没有则返回 null
    private static PlayerEntity getMarkingCommander(GameWorldComponent gameWorldComponent, PlayerEntity localPlayer, UUID targetUuid) {
        for (UUID commanderUuid : gameWorldComponent.getAllWithRole(Noellesroles.COMMANDER)) {
            PlayerEntity commander = localPlayer.getWorld().getPlayerByUuid(commanderUuid);
            if (commander == null) continue;

            CommanderPlayerComponent commanderComp = CommanderPlayerComponent.KEY.get(commander);
            if (commanderComp.isThreatTarget(targetUuid)) {
                return commander;
            }
        }
        return null;
    }

    private static boolean canSeeCommanderMarkedTargets(GameWorldComponent gameWorldComponent, PlayerEntity localPlayer) {
        return gameWorldComponent.canUseKillerFeatures(localPlayer)
                || gameWorldComponent.isRole(localPlayer, Noellesroles.UNDERCOVER);
    }

    private static boolean isTrapSpectatorViewer(PlayerEntity player, GameWorldComponent gameWorld) {
        return SpectatorStateHelper.isInGameRealSpectator(player, gameWorld)
                && (!gameWorld.hasAnyRole(player) || gameWorld.isPlayerDead(player.getUuid()));
    }

    public static void markAssistInterfaceKeyHandled() {
        wasAssistInterfacePressed = true;
    }

    public static boolean isDeathArenaActiveForClientPlayer() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null && DeathArenaPlayerComponent.KEY.get(player).isInArena();
    }

    public static void markRoleInfoKeyHandled() {
        wasRoleInfoPressed = true;
    }

    public static void markConfigScreenKeyHandled() {
        wasConfigScreenPressed = true;
    }

    private static void runOnClient(MinecraftClient client, Runnable action) {
        client.execute(action);
    }

    /**
     * 检测床方块上是否有睡觉的玩家
     * @return 睡觉的玩家，如果没有则返回 Optional.empty()
     */
    public static Optional<PlayerEntity> findSleepingPlayerOnBed(World world, BlockHitResult blockHitResult) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState state = world.getBlockState(blockPos);

        if (!(state.getBlock() instanceof BedBlock)) {
            return Optional.empty();
        }

        BedPart part = state.get(BedBlock.PART);
        Direction facing = state.get(BedBlock.FACING);
        BlockPos headPos = (part == BedPart.HEAD) ? blockPos : blockPos.offset(facing);

        for (PlayerEntity player : world.getPlayers()) {
            if (!player.isSleeping()) {
                continue;
            }
            Optional<BlockPos> sleepingPosOpt = player.getSleepingPosition();
            if (sleepingPosOpt.isEmpty()) {
                continue;
            }
            BlockPos sleepingPos = sleepingPosOpt.get();
            if (sleepingPos.equals(headPos) || sleepingPos.equals(blockPos)) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }
}
