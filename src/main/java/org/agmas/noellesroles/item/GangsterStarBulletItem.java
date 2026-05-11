package org.agmas.noellesroles.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class GangsterStarBulletItem extends Item {
    public GangsterStarBulletItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        addTooltipLines(tooltip, "item.noellesroles.gangster_star_bullet.tooltip");
        super.appendTooltip(stack, context, tooltip, type);
    }

    static void addTooltipLines(List<Text> tooltip, String key) {
        for (String line : Text.translatable(key).getString().split("\\R")) {
            tooltip.add(Text.literal(line));
        }
    }
}
