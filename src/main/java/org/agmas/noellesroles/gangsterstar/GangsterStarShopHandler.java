package org.agmas.noellesroles.gangsterstar;

import dev.doctor4t.wathe.api.event.BuildShopEntries;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;

/**
 * 为黑帮巨星替换标准商店，只保留该角色专属的两个购买项。
 * 物品显示暂时复用 Wathe 的现有模型，后续有专属材质时再替换。
 */
public class GangsterStarShopHandler {
    private static final String BULLET_ID = "gangster_star_bullet";
    private static final String SPIRIT_ID = "gangster_spirit";

    public static void register() {
        BuildShopEntries.EVENT.register((player, context) -> {
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
            if (!gameWorld.isRole(player, Noellesroles.GANGSTER_STAR)) {
                return;
            }

            context.clearEntries();
            // 子弹不是实体物品，购买后只增加同步到客户端的左轮可用次数。
            context.addEntry(new ShopEntry.Builder(
                    BULLET_ID,
                    ModItems.GANGSTER_STAR_BULLET.getDefaultStack(),
                    100,
                    ShopEntry.Type.WEAPON
            ).onBuy(p -> {
                GangsterStarPlayerComponent.KEY.get(p).addRevolverShot();
                return true;
            }).build());
            // 黑帮精神是一次性状态，会在阻止枪击死亡时被消耗。
            context.addEntry(new ShopEntry.Builder(
                    SPIRIT_ID,
                    ModItems.GANGSTER_SPIRIT.getDefaultStack(),
                    200,
                    ShopEntry.Type.WEAPON
            ).stock(1).onBuy(p -> GangsterStarPlayerComponent.KEY.get(p).buyGangsterSpirit()).build());
        });
    }
}
