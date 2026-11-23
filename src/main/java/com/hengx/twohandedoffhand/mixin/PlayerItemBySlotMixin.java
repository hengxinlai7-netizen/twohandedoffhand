package com.hengx.twohandedoffhand.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hengx.twohandedoffhand.TwoHanded;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 核心:解除 Better Combat 的“伪空副手”。
 *
 * <p>Better Combat 的 {@code PlayerEntityMixin#getEquippedStack_Pre} 在 {@code Player#getItemBySlot}
 * 的 HEAD 处 {@code setReturnValue(ItemStack.EMPTY) + cancel()}。我们以更低优先级(priority = 500,
 * 比 Better Combat 默认的 1000 先应用,因此在 HEAD 处也更先执行)注册自己的 HEAD 回调,抢先 cancel 掉
 * 并把真实副手栈交回去,于是 Better Combat 的那段回调不会被执行。
 * (Mixin 在同一个 HEAD 注入点上是“先应用者先执行”,已在 prodserver 上用 mixin.debug.export 验证过。)
 *
 * <p>副作用面收敛由本模组的其它 mixin 负责:只拦渲染与使用。
 * 未安装 Better Combat(或当前不是双手武器状态)时本注入不 cancel,行为与原版完全一致。
 */
@Mixin(value = Player.class, priority = 500)
public abstract class PlayerItemBySlotMixin {

    @Inject(method = "m_6844_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void twohandedoffhand$exposeOffhand(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (slot != EquipmentSlot.OFFHAND) {
            return;
        }
        Player self = (Player) (Object) this;
        if (!TwoHanded.isWielding(self)) {
            return;
        }
        // 与 Player#getItemBySlot 原实现相同,只是绕过 Better Combat 的替换。
        cir.setReturnValue(self.getInventory().offhand.get(0));
        cir.cancel();
    }
}
