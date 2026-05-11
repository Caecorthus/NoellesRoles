package org.agmas.noellesroles.client.mixin.gangsterstar;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.util.HudRenderHelper;
import org.agmas.noellesroles.gangsterstar.GangsterStarPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class GangsterStarHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    private void noellesroles$renderGangsterStarHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ClientPlayerEntity player = HudRenderHelper.getActivePlayer();
        if (player == null) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        if (!gameWorld.isRole(player, Noellesroles.GANGSTER_STAR)) return;

        GangsterStarPlayerComponent component = GangsterStarPlayerComponent.KEY.get(player);
        Text line = component.isFinalStandActive()
                ? Text.translatable("tip.noellesroles.gangster_star.final_stand_time", Math.max(0, component.getFinalStandTicks() / 20))
                : Text.translatable("tip.noellesroles.gangster_star.shots", component.getRevolverShots());

        // 右下角技能提示需要压在 Simple Voice Chat 头像层之上，否则会被组队头像遮住。
        HudRenderHelper.pushAboveVoiceChatHudLayer(context);
        try {
            HudRenderHelper.drawBottomRight(context, getTextRenderer(), line, context.getScaledWindowHeight(), Noellesroles.GANGSTER_STAR.color());
        } finally {
            HudRenderHelper.popAboveVoiceChatHudLayer(context);
        }
    }
}
