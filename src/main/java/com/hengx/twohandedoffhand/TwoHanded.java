package com.hengx.twohandedoffhand;

import java.lang.reflect.Method;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * 判断“当前是否处于双手武器状态”，口径与 Better Combat 的
 * {@code PlayerEntityMixin#getEquippedStack_Pre} 完全一致:
 * 主手(当前选中格)或副手中的任意一件物品带 {@code two_handed=true},即视为双手状态。
 *
 * <p>Better Combat 通过反射访问,避免编译期依赖它的 jar;它不存在时本模组整体静默失效。
 */
public final class TwoHanded {

    private static final String MOD_ID = "bettercombat";
    private static final String REGISTRY_CLASS = "net.bettercombat.logic.WeaponRegistry";
    private static final String ATTRIBUTES_CLASS = "net.bettercombat.api.WeaponAttributes";

    /** 0 = 未初始化, 1 = 可用, 2 = 不可用 */
    private static volatile int state;

    private static Method getAttributes;
    private static Method isTwoHanded;

    private TwoHanded() {
    }

    public static boolean isWielding(Player player) {
        if (player == null) {
            return false;
        }
        Inventory inventory = player.getInventory();
        if (inventory == null) {
            return false;
        }
        return isTwoHanded(inventory.getSelected()) || isTwoHanded(inventory.offhand.get(0));
    }

    public static boolean isTwoHanded(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!ensureCompat()) {
            return false;
        }
        try {
            Object attributes = getAttributes.invoke(null, stack);
            if (attributes == null) {
                return false;
            }
            Object value = isTwoHanded.invoke(attributes);
            return value instanceof Boolean flag && flag;
        } catch (Throwable throwable) {
            fail(throwable);
            return false;
        }
    }

    private static boolean ensureCompat() {
        int current = state;
        if (current != 0) {
            return current == 1;
        }
        synchronized (TwoHanded.class) {
            if (state != 0) {
                return state == 1;
            }
            if (ModList.get() != null && !ModList.get().isLoaded(MOD_ID)) {
                state = 2;
                return false;
            }
            try {
                Class<?> registry = Class.forName(REGISTRY_CLASS);
                Class<?> attributes = Class.forName(ATTRIBUTES_CLASS);
                getAttributes = registry.getMethod("getAttributes", ItemStack.class);
                isTwoHanded = attributes.getMethod("isTwoHanded");
                getAttributes.setAccessible(true);
                isTwoHanded.setAccessible(true);
                state = 1;
            } catch (ClassNotFoundException absent) {
                // Better Combat 未安装,属于正常情况,不刷日志。
                state = 2;
            } catch (Throwable throwable) {
                fail(throwable);
            }
            return state == 1;
        }
    }

    private static void fail(Throwable throwable) {
        state = 2;
        TwoHandedOffhand.LOGGER.warn("[twohandedoffhand] Better Combat compatibility disabled: {}", throwable.toString());
    }
}
