package com.hengx.twohandedoffhand.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.hengx.twohandedoffhand.TwoHanded;

import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 「显示」——第三人称手持物图层。
 *
 * <p>该图层同时读取主手与副手,副手为空时不会绘制副手物品,也不会加副手的平移变换。
 */
@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {

    @Redirect(
            method = "m_6494_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_21206_()Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false,
            require = 0
    )
    private ItemStack twohandedoffhand$hideOffhandInThirdPerson(LivingEntity entity) {
        if (entity instanceof Player player && TwoHanded.isWielding(player)) {
            return ItemStack.EMPTY;
        }
        return entity.getOffhandItem();
    }
}
