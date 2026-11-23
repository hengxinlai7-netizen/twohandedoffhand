package com.hengx.twohandedoffhand.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.hengx.twohandedoffhand.TwoHanded;

import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 「显示」——快捷栏副手槽 (HUD)。
 *
 * <p>副手为空时不画副手槽的 29x24 底图,也不画槽里的物品。
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Redirect(
            method = "m_280518_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;m_21206_()Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false,
            require = 0
    )
    private ItemStack twohandedoffhand$hideHotbarOffhand(Player player) {
        if (TwoHanded.isWielding(player)) {
            return ItemStack.EMPTY;
        }
        return player.getOffhandItem();
    }
}
