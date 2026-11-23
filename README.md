# 双手武器·副手放行 (two-handed-offhand)

面向 **Minecraft 1.20.1 / Forge 47.x** 的小模组，配合 **Better Combat 1.9.0+** 使用。

## 它解决什么

Better Combat 在「主手或副手持双手武器」时，会把 `Player#getItemBySlot(OFFHAND)` 的返回值
直接替换成空栈：

```java
// Better Combat 1.9.0  PlayerEntityMixin#getEquippedStack_Pre
if (slot == EquipmentSlot.OFFHAND && (mainHandHasTwoHanded || offHandHasTwoHanded)) {
    cir.setReturnValue(ItemStack.EMPTY);
    cir.cancel();
}
```

这一个「伪空副手」同时做了三件事：

| 效果 | 是否符合预期 |
| --- | --- |
| 副手物品不渲染（第一/第三人称、快捷栏副手槽） | ✅ 想要 |
| 副手物品不能使用（右键、放置、实体交互、举盾…） | ✅ 想要 |
| 副手物品的一切被动效果失效（属性修正、装备事件、附魔、图腾……） | ❌ 不想要 |

本模组只保留前两条，把第三条放行。

## 实现

1. **解掉伪空副手**：以 `priority = 500`（比 Better Combat 默认的 1000 先应用，因此在同一个 HEAD 注入点上
   先执行——Mixin 这里是「先应用者先执行」，已用 `mixin.debug.export` 在真实服务端上验证）注入
   `Player#getItemBySlot`，抢在 Better Combat 之前返回真实副手栈并 `cancel()`，
   使它那段替换逻辑不再被执行。未安装 Better Combat 或不在双手状态时本注入不 cancel，行为与原版一致。
2. **重新拦下「显示」**：
   - `ItemInHandRenderer#tick` —— 第一人称副手物品（`itemInOffHand`）；
   - `ItemInHandLayer#render` —— 第三人称手持物图层；
   - `PlayerRenderer#getArmPose` —— 第三人称副手手臂姿势（避免举空气）；
   - `Gui#renderHotbar` —— 快捷栏副手槽。
3. **重新拦下「使用」**：
   - 客户端 `MultiPlayerGameMode` 的 `useItem` / `useItemOn` / `interact` / `interactAt`（副手一律 `PASS`）；
   - `LivingEntity#startUsingItem`（禁止在副手开始举盾/拉弓/进食…）；
   - 服务端 `ServerGamePacketListenerImpl#handleUseItem` / `handleUseItemOn`（改包也无法在副手使用）。
4. **兜底**：`LivingEntity#collectEquipmentChanges`（`m_21319_`）里把真实副手栈喂给装备扫描，
   即使第 1 步的优先级被别的模组打乱，副手物品的属性修正/装备事件仍然生效。

双手武器自身的判定（连击、双持禁用、姿态与动画、属性表重定向）完全交给 Better Combat，本模组不碰。

## 行为说明

- 副手物品**仍然在背包里**：只是 HUD 与手持不显示、不能使用；换手、放回背包、进出容器都正常。
- 图腾、不死图腾这类「被动触发」会正常生效（属于效果放行）。
- 副手武器提供的攻击力/攻速/护甲等属性修正会正常计入（原版装备流水线）。
- **不要**再和 `attribute-fix`（`attributefixplus`）那类「手动补副手属性」的模组一起用，否则属性会叠加两次。

## 依赖

- Minecraft 1.20.1、Forge 47.2+
- Better Combat 1.9.0+（软依赖：装了才有效果；没装则本模组静默不做事）

## 构建

```bat
set JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.8.9-hotspot
set GRADLE_USER_HOME=<workspace>\.gradle-home
gradlew.bat build --no-daemon --offline
```

产物：`build\libs\twohandedoffhand-1.0.0a.jar`

本项目**不使用** MixinGradle / Mixin 注解处理器：所有注入目标直接写 SRG 名并 `remap = false`，
因此不需要 refmap，只在正式环境（SRG 名）生效；注入均带 `require = 0`，目标缺失时只记日志不崩游戏。
