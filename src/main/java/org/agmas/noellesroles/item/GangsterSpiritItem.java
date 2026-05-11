package org.agmas.noellesroles.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class GangsterSpiritItem extends Item {
    public GangsterSpiritItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        GangsterStarBulletItem.addTooltipLines(tooltip, "item.noellesroles.gangster_spirit.tooltip");
        super.appendTooltip(stack, context, tooltip, type);
    }
}
