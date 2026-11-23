package com.hengx.twohandedoffhand.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hengx.twohandedoffhand.TwoHanded;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 「使用」——客户端四个交互入口全部拦下副手:
 * 右键使用物品、右键方块(放置/交互)、右键实体、右键实体指定点。
 *
 * <p>返回 PASS 不消耗操作、不挥手臂,也不发包 { (Minecraft#startUseItem 会按
 * MAIN_HAND -> OFF_HAND 顺序遍历,主手照旧)。
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Inject(method = "m_233721_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void twohandedoffhand$blockOffhandUseItem(Player player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (hand == InteractionHand.OFF_HAND && TwoHanded.isWielding(player)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "m_233732_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void twohandedoffhand$blockOffhandUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (hand == InteractionHand.OFF_HAND && TwoHanded.isWielding(player)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "m_105226_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void twohandedoffhand$blockOffhandInteract(Player player, Entity target, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (hand == InteractionHand.OFF_HAND && TwoHanded.isWielding(player)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "m_105230_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void twohandedoffhand$blockOffhandInteractAt(Player player, Entity target, EntityHitResult hit,
            InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (hand == InteractionHand.OFF_HAND && TwoHanded.isWielding(player)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
