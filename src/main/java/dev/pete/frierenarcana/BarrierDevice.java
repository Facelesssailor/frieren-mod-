package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;

public final class BarrierDevice extends Item {
    public final int tier;

    public BarrierDevice(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int radius() {
        return Math.max(8, (int)Math.round((double)ArcanaConfig.DEVICE_RADIUS.get().intValue() * switch (this.tier) {
            case 1 -> 0.25;
            case 2 -> 0.375;
            case 3 -> 0.5;
            case 4 -> 0.75;
            default -> 1.0;
        }));
    }

    public int manaCost() {
        return 150 + 100 * (this.tier - 1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer p) {
            if (BarrierData.owns(p, false)) {
                BarrierData.releaseOwned(p, false);
            } else {
                MagicData magic = MagicData.getPlayerMagicData(p);
                if (magic.getMana() < (float)this.manaCost()) {
                    p.displayClientMessage(Component.translatable("message.frieren_arcana.error.mana"), true);
                    return InteractionResultHolder.fail(stack);
                }

                if (!BarrierData.get(p.serverLevel()).create(p.serverLevel(), p, this.radius(), false)) {
                    return InteractionResultHolder.fail(stack);
                }

                magic.setMana(magic.getMana() - (float)this.manaCost());
                ArcanaEvents.syncMana(p);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("info.frieren_arcana.device", this.radius(), this.manaCost()));
        lines.add(Component.translatable("info.frieren_arcana.device_release"));
    }
}
