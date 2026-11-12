package org.agmas.noellesroles.puppeteer;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 记录「刚刚这一枪打中的是傀儡」，供 GunMissRecordMixin 跳过虚假的 gun_miss。
 * 服务端主线程使用；Wathe 的 recordItemUse 紧跟在本模组的命中小结之后同步执行。
 */
public final class PuppetGunHitTracker {
    private static final int MAX_AGE_TICKS = 5;
    private static final Map<UUID, Entry> ENTRIES = new HashMap<>();

    private PuppetGunHitTracker() {}

    public static void mark(ServerPlayerEntity player, Identifier itemId) {
        ENTRIES.put(player.getUuid(), new Entry(itemId, player.getServer().getTicks()));
    }

    /** 只在同一玩家、同一枪械、5 tick 内消费一次。 */
    public static boolean consume(ServerPlayerEntity player, Identifier itemId) {
        Entry entry = ENTRIES.remove(player.getUuid());
        return entry != null
                && entry.itemId().equals(itemId)
                && player.getServer().getTicks() - entry.tick() <= MAX_AGE_TICKS;
    }

    public static void reset() {
        ENTRIES.clear();
    }

    private record Entry(Identifier itemId, int tick) {}
}
