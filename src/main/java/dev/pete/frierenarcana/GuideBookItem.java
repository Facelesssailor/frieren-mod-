package dev.pete.frierenarcana;

import dev.pete.frierenarcana.client.GuideBookClient;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;

public class GuideBookItem extends Item {
    public GuideBookItem(Properties var1) {
        super(var1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level var1, Player var2, InteractionHand var3) {
        ItemStack var4 = var2.getItemInHand(var3);
        if (var1.isClientSide()) {
            GuideBookClient.open();
        }

        return InteractionResultHolder.sidedSuccess(var4, var1.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack var1, TooltipContext var2, List<Component> var3, TooltipFlag var4) {
        var3.add(Component.translatable("item.frieren_arcana.guide_book.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
