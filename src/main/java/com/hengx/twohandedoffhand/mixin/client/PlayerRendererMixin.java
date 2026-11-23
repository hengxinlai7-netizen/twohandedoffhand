package com.hengx.twohandedoffhand.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.hengx.twohandedoffhand.TwoHanded;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.item.ItemStack;

/**
 * 「显示」——第三人称手臂姿势。
 *
 * <p>主手已在摆姿势时,副手手臂姿势取决于副手是否为空:非空会举成 ITEM 姿势。
 * 副手物品既然不渲染,这里也按空手处理,避免出现“举着一团空气”的手臂。
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Redirect(
            method = "m_117818_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;m_21206_()Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false,
            require = 0
    )
    private ItemStack twohandedoffhand$offhandArmPose(AbstractClientPlayer player) {
        if (TwoHanded.isWielding(player)) {
            return ItemStack.EMPTY;
        }
        return player.getOffhandItem();
    }
}
