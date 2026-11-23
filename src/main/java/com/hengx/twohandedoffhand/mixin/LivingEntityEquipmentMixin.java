package com.hengx.twohandedoffhand.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.hengx.twohandedoffhand.TwoHanded;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 兜底:装备变更扫描(属性修正 / 装备事件 / 属性表同步)。
 *
 * <p>{@code LivingEntity#m_21319_} 每 tick 用 {@code getItemBySlot} 收集各槽位物品,
 * 并据此 {@code AttributeMap.add/removeAttributeModifiers}。万一
 * {@link PlayerItemBySlotMixin} 的优先级没有抢过 Better Combat(例如被别的模组改了顺序),
 * 这里仍然把真实副手栈喂给这条流水线,保证副手物品的属性加成不会失效。
 *
 * <p>非 OFFHAND 槽位原样转发,不影响原逻辑。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityEquipmentMixin {

    @Redirect(
            method = "m_21319_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_6844_(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false,
            require = 0
    )
    private ItemStack twohandedoffhand$feedOffhandToEquipmentScan(LivingEntity entity, EquipmentSlot slot) {
        if (slot == EquipmentSlot.OFFHAND && entity instanceof Player player && TwoHanded.isWielding(player)) {
            return player.getInventory().offhand.get(0);
        }
        return entity.getItemBySlot(slot);
    }
}
