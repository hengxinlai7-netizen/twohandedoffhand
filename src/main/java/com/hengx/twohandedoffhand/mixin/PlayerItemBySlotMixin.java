package com.hengx.twohandedoffhand.mixin;

import com.hengx.twohandedoffhand.TwoHanded;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 服务端专用：让 {@code getItemBySlot(OFFHAND)} 返回真实副手物品。
 *
 * <p>这样所有<b>服务端</b>读取副手的逻辑（属性修饰符、被动效果、其他模组的
 * {@code getOffhandItem()} 调用）都能拿到物品，效果不会被 Better Combat 的"伪空副手"抹掉。</p>
 *
 * <p><b>客户端不拦</b>：客户端仍走 Better Combat 的原逻辑（返回 EMPTY），
 * 所以双手武器状态下副手<b>不渲染</b>——符合"双手武器本来就不该看到副手"的预期。</p>
 *
 * <p>优先注册（priority = 500，早于 Better Combat 的 1000），在 HEAD 先 cancel 掉，
 * 使 Better Combat 的替换回调不执行。</p>
 */
@Mixin(value = Player.class, priority = 500)
public abstract class PlayerItemBySlotMixin {

    @Inject(method = "m_6844_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void twohandedoffhand$exposeOffhandOnServer(EquipmentSlot slot,
                                                        CallbackInfoReturnable<ItemStack> cir) {
        if (slot != EquipmentSlot.OFFHAND) {
            return;
        }
        Player self = (Player) (Object) this;
        if (self.level() == null || self.level().isClientSide) {
            return;   // 客户端保持 Better Combat 的行为（不渲染副手）
        }
        if (!TwoHanded.isWielding(self)) {
            return;
        }
        cir.setReturnValue(self.getInventory().offhand.get(0));
        cir.cancel();
    }
}
