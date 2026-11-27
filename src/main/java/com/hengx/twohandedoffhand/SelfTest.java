package com.hengx.twohandedoffhand;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 功能自检（仅 {@code -Dtwohandedoffhand.selftest=true} 时运行）。
 *
 * <p>验证三件事：</p>
 * <ol>
 *   <li>双手武器状态下，{@code getItemBySlot(OFFHAND)} 仍能拿到真实副手物品（不被 Better Combat 置空）；</li>
 *   <li>双手武器状态下，副手 {@code startUsingItem} 被拦下；</li>
 *   <li>非双手状态下，副手使用恢复正常。</li>
 * </ol>
 */
public final class SelfTest {

    private static final String TAG = "[TwoHandedSelfTest]";
    private static boolean allPass = true;

    private SelfTest() {
    }

    public static void register() {
        if (Boolean.getBoolean("twohandedoffhand.selftest")) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(SelfTest.class);
        }
    }

    private static int waitTicks = 40;
    private static boolean ran;

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // 延迟执行:Better Combat 的 WeaponRegistry 在启动期才载入,太早拿不到 isTwoHanded
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (ran || event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
        if (--waitTicks > 0) {
            return;
        }
        ran = true;
        try {
            run(event.getServer().overworld());
        }
        catch (Throwable t) {
            log(" ABORTED " + t);
            allPass = false;
        }
        finally {
            log(" summary => " + (allPass ? "PASS" : "FAIL"));
            log(" ALL-RESULT " + (allPass ? "PASS" : "FAIL"));
            event.getServer().halt(false);
        }
    }

    private static void run(ServerLevel level) {
        registerStickAsTwoHanded();
        ItemStack twoHanded = findTwoHanded();
        pass("H0", "找到双手武器: " + describe(twoHanded), !twoHanded.isEmpty());
        if (twoHanded.isEmpty()) {
            return;
        }

        var profile = new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "tho_selftest");
        ServerPlayer player = new ServerPlayer(level.getServer(), level, profile);
        player.setItemInHand(InteractionHand.MAIN_HAND, twoHanded);
        player.getInventory().offhand.set(0, new ItemStack(Items.SHIELD));

        // H1: 服务端必须读到真实副手(效果可读);客户端由 Better Combat 置空(不渲染)
        ItemStack viaSlot = player.getItemBySlot(EquipmentSlot.OFFHAND);
        pass("H1", "服务端副手可读=" + describe(viaSlot), viaSlot.is(Items.SHIELD));

        // H1b: 装备扫描重定向已接线(属性效果走真实副手,不经过 getItemBySlot)
        pass("H1b", "装备扫描重定向已接线", equipmentRedirectWired());

        // H2: 双手状态下副手不能开始使用
        player.stopUsingItem();
        player.startUsingItem(InteractionHand.OFF_HAND);
        boolean blocked = !player.isUsingItem() || player.getUsedItemHand() != InteractionHand.OFF_HAND;
        pass("H2", "双手状态副手使用被拦=" + blocked, blocked);

        // H3: 换成非双手物品后，副手使用恢复
        player.stopUsingItem();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PAPER));
        player.getInventory().offhand.set(0, new ItemStack(Items.SHIELD));
        player.startUsingItem(InteractionHand.OFF_HAND);
        boolean allowed = player.isUsingItem() && player.getUsedItemHand() == InteractionHand.OFF_HAND;
        pass("H3", "非双手状态副手可用=" + allowed, allowed);

        player.discard();
    }

    /** 把木棍注册成双手武器(注册表公共 API),用于在空环境里造出"双手状态"。 */
    private static boolean registerStickAsTwoHanded() {
        try {
            Class<?> attrs = Class.forName("net.bettercombat.api.WeaponAttributes");
            Class<?> attackArr = Class.forName("[Lnet.bettercombat.api.WeaponAttributes$Attack;");
            Object emptyAttacks = java.lang.reflect.Array.newInstance(
                    Class.forName("net.bettercombat.api.WeaponAttributes$Attack"), 0);
            Object instance = attrs.getConstructor(double.class, String.class, String.class,
                            Boolean.class, String.class, attackArr)
                    .newInstance(2.5D, "one_handed", null, Boolean.TRUE, "sword", emptyAttacks);
            Class<?> registry = Class.forName("net.bettercombat.logic.WeaponRegistry");
            registry.getMethod("register", net.minecraft.resources.ResourceLocation.class, attrs)
                    .invoke(null, new net.minecraft.resources.ResourceLocation("minecraft", "stick"), instance);
            return TwoHanded.isTwoHanded(new ItemStack(Items.STICK));
        }
        catch (Throwable t) {
            log(" 注册双手示例失败: " + t);
            return false;
        }
    }

    private static boolean equipmentRedirectWired() {
        try (var in = SelfTest.class.getResourceAsStream(
                "/com/hengx/twohandedoffhand/mixin/LivingEntityEquipmentMixin.class")) {
            if (in == null) {
                return false;
            }
            String text = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.ISO_8859_1);
            return text.contains("feedOffhandToEquipmentScan") && text.contains("m_21319_");
        }
        catch (Throwable t) {
            return false;
        }
    }

    private static ItemStack findTwoHanded() {
        for (var item : ForgeRegistries.ITEMS) {
            try {
                ItemStack stack = new ItemStack(item);
                if (!stack.isEmpty() && TwoHanded.isTwoHanded(stack)) {
                    return stack;
                }
            }
            catch (Throwable ignored) {
                // 跳过会抛异常的物品
            }
        }
        return ItemStack.EMPTY;
    }

    private static String describe(ItemStack stack) {
        return stack.isEmpty() ? "EMPTY" : ForgeRegistries.ITEMS.getKey(stack.getItem()) + "x" + stack.getCount();
    }

    private static void pass(String name, String detail, boolean ok) {
        if (!ok) {
            allPass = false;
        }
        log(" " + name + " " + detail + (ok ? "  [PASS]" : "  [FAIL]"));
        log(" " + name + "-RESULT " + (ok ? "PASS" : "FAIL"));
    }

    private static void log(String message) {
        LogUtils.getLogger().info(TAG + message);
    }
}
