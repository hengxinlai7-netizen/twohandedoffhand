package com.hengx.twohandedoffhand.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hengx.twohandedoffhand.TwoHanded;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 「使用」的一环:禁止双手武器状态下开始/持续使用副手物品
 * (盾牌举盾、弓弩拉弓、吃东西喝药等最终都会落到 {@code startUsingItem})。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityStartUseMixin {

    @Inject(method = "m_6672_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void twohandedoffhand$blockOffhandStartUsing(InteractionHand hand, CallbackInfo ci) {
        if (hand != InteractionHand.OFF_HAND) {
            return;
        }
        if (!((Object) this instanceof Player player)) {
            return;
        }
        if (!TwoHanded.isWielding(player)) {
            return;
        }
        ci.cancel();
    }
}
