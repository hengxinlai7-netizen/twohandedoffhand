package com.hengx.twohandedoffhand.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.hengx.twohandedoffhand.TwoHanded;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

/**
 * 「显示」——第一人称。
 *
 * <p>{@code ItemInHandRenderer#tick} 每帧把主/副手物品读进 {@code itemInMainHand/itemInOffHand},
 * 后者驱动第一人称副手物品的渲染。这里把副手读取结果换成空栈,副手物品就完全不画了
 * (手臂淡出逻辑也随之走空手分支,与 Better Combat 原本的表现一致)。
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Redirect(
            method = "m_109311_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;m_21206_()Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false,
            require = 0
    )
    private ItemStack twohandedoffhand$hideOffhandInFirstPerson(LocalPlayer player) {
        if (TwoHanded.isWielding(player)) {
            return ItemStack.EMPTY;
        }
        return player.getOffhandItem();
    }
}
