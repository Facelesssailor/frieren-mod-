package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class StaffView {
    private static ItemStack demo;

    private StaffView() {
    }

    public static ItemStack held(Player var0) {
        if (var0 == null) {
            return ItemStack.EMPTY;
        } else {
            ItemStack var1 = var0.getMainHandItem();
            if (var1 != null && ArcanaModes.isStaff(var1)) {
                return var1.copy();
            } else {
                ItemStack var2 = var0.getOffhandItem();
                return var2 != null && ArcanaModes.isStaff(var2) ? var2.copy() : ItemStack.EMPTY;
            }
        }
    }

    public static ItemStack demo() {
        if (demo == null) {
            demo = ItemStack.EMPTY;

            for (Item var1 : BuiltInRegistries.ITEM) {
                if (var1 instanceof StaffItem) {
                    demo = var1.getDefaultInstance();
                    break;
                }
            }
        }

        return demo.copy();
    }
}
