package com.hengx.twohandedoffhand;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.common.Mod;

/**
 * 双手武器·副手放行。
 *
 * <p>Better Combat 在玩家主手或副手持「双手武器」(weapon_attributes 里 two_handed=true) 时，
 * 会把 {@code Player#getItemBySlot(OFFHAND)} 的返回值替换成空栈，靠“副手对外为空”来同时实现
 * 隐藏渲染和禁止使用。副作用是副手物品的一切被动效果也被一起干掉
 * (属性修正、装备事件、附魔、图腾……)。
 *
 * <p>本模组把那个“伪空副手”解掉，只重新拦下两件事:
 * <ol>
 *   <li>副手的显示与渲染(第一人称、第三人称手臂姿势、快捷栏副手槽)；</li>
 *   <li>副手的使用(右键使用/放置/实体交互/开始使用，客户端与服务端都拦)。</li>
 * </ol>
 * 其余所有读取副手的路径都能拿到真实物品，因此效果照常生效。
 * 双手武器自身的攻击判定、连击、双持禁用等仍由 Better Combat 负责。
 */
@Mod(TwoHandedOffhand.MOD_ID)
public final class TwoHandedOffhand {

    public static final String MOD_ID = "twohandedoffhand";

    public static final Logger LOGGER = LogUtils.getLogger();

    public TwoHandedOffhand() {
    }
}
