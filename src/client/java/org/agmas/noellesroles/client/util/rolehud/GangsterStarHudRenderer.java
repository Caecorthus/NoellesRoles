package org.agmas.noellesroles.client.util.rolehud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.agmas.noellesroles.client.util.HudRenderHelper;
import org.agmas.noellesroles.gangsterstar.GangsterStarPlayerComponent;

/**
 * 在右下角技能提示位置显示黑帮巨星的左轮状态。
 */
public final class GangsterStarHudRenderer implements RoleHudRenderer {
    @Override
    public int getTopY(TextRenderer renderer, ClientPlayerEntity player, int bottom) {
        GangsterStarPlayerComponent component = GangsterStarPlayerComponent.KEY.get(player);
        Text line = component.isFinalStandActive()
                ? Text.translatable("tip.noellesroles.gangster_star.final_stand_time", Math.max(0, component.getFinalStandTicks() / 20))
                : Text.translatable("tip.noellesroles.gangster_star.shots", component.getRevolverShots());
        return HudRenderHelper.stackLine(bottom, renderer, line, 0);
    }
}
