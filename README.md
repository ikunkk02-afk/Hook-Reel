# Hook & Reel

一个将原版鱼竿扩展为钓鱼、钩锁、方块拖拽与立体移动工具的 Fabric 模组。

Hook & Reel 对原版鱼竿进行强化，加入三种最高 III 级的鱼竿附魔以及可配置的移动、拖拽和钓鱼能力。玩家可以：

- 提高稀有钓鱼战利品的出现概率；
- 蓄力将鱼钩抛向更远的位置；
- 把生物和掉落物持续拖到身边；
- 将允许移动的普通方块拖回来；
- 使用挂钩快速抵达高处；
- 在峡谷等位置放绳下降；
- 吸附在墙面并寻找下一个挂钩点；
- 通过配置决定是否能够钓出生物。

| 项目 | 信息 |
|---|---|
| 当前版本 | 1.0.0 |
| Minecraft | 1.21.1 |
| 模组加载器 | Fabric |
| Java | 21 或更高版本 |
| Mod ID | `hook_and_reel` |
| 作者 | `ikunkk02-afk` |
| 许可证 | MIT |
| 源代码 | [GitHub](https://github.com/ikunkk02-afk/Hook-Reel) |

## 主要功能

### 幸运 / Lucky Catch

- 最高等级为 III。
- 每级默认为当前一次钓鱼战利品上下文增加 `1.5` Luck，提高高价值战利品的出现概率，但不保证每次获得宝藏。
- 继续使用原版钓鱼 Loot Table，不替换原版战利品表，也不永久修改玩家属性。
- 默认允许与原版“海之眷顾”共存；服务器可通过配置关闭共存。
- 幸运 III 默认会在鱼钩处于水中并保持有效钓鱼状态约 `1` 秒后制造一次咬钩。
- 默认开启自动收杆，因此咬钩后会调用原版收杆流程并完成本次钓鱼；关闭自动收杆后，玩家需要在咬钩窗口内手动收杆。

### 钩锁 / Grappling Hook

- 最高等级为 III。
- 长按右键蓄力，松开右键发射；默认最大蓄力时间为 `1.5` 秒。
- 附魔等级越高，最大射程越远；默认 I / II / III 级分别为 `24` / `36` / `48` 格，最低射程为 `8` 格。
- 命中后可以持续拖拽生物和掉落物；默认允许拖拽玩家，不允许拖拽 Boss。
- 可以拖拽满足安全条件的普通方块。方块会实体化并被拉向玩家，到达附近后重新放置。
- 使用独立的钩锁冷却，默认完整冷却为 `10` 秒，可在配置中修改。

方块拖拽存在以下限制：

- 方块实体当前始终不能拖拽，即使配置文件中出现 `allowPullBlockEntities`，运行时也会将其校正为 `false`。
- 基岩、屏障、命令方块、结构方块、传送门、流体等危险或特殊方块默认不能拖拽。
- 门、床、高型植物、活塞等不安全多方块结构默认不能拖拽。
- 还会检查方块硬度、世界边界、区块状态和玩家破坏权限。
- 具体限制以服务器配置及 `hook_and_reel` 数据标签为准。

### 挂钩 / Anchor Hook

- 最高等级为 III。
- 与钩锁一样长按右键蓄力、松开发射；默认 I / II / III 级最大射程分别为 `24` / `36` / `48` 格。
- 鱼钩命中允许的方块表面后建立锚点，不会在命中瞬间自动把玩家拉起。
- 建立锚点后按一次空格主动收绳上升；上升时可使用 A / D 左右绕开凸出方块和障碍物。
- 接近合法墙面后可以进入墙面吸附。默认最长吸附约 `10` 秒，可使用 W / S 上下移动、A / D 左右移动。
- 墙面吸附时按空格执行墙跳，向上并离开墙面。
- 锚定时按住 Shift 可放长绳索并缓慢下降。
- 使用独立的挂钩冷却，默认完整冷却为 `1.5` 秒；未建立锚点的失败发射默认只有 `0.25` 秒短延迟。
- 挂钩冷却与钩锁冷却互不影响，均可在配置中修改。

## 操作方法

| 操作 | 默认按键 | 说明 |
|---|---|---|
| 切换模式 | V | 在钩锁模式与挂钩模式之间切换 |
| 蓄力发射 | 长按右键后松开 | 蓄力越久，鱼钩射程越远 |
| 主动收绳上升 | 空格 | 挂钩命中方块后按一次开始上升 |
| 上升时左右移动 | A / D | 绕开凸出方块和障碍物 |
| 绳降 | Shift | 锚定状态下按住以缓慢放长绳索 |
| 墙面移动 | W / S / A / D | 在墙面吸附期间上下和左右移动 |
| 墙跳 | 空格 | 从墙面向上、向外跳出 |
| 收回鱼钩 | 右键 | 解除当前鱼钩、拖拽或锚点 |

V 键是标准 Minecraft 按键绑定，可以在“选项 → 控制”中重新设置。模式通过持久化 Data Component 保存在每一根鱼竿 `ItemStack` 自身，因此两根鱼竿可以分别记住不同模式。只有鱼竿拥有对应附魔时，相应模式才可使用；只有一种相关附魔的鱼竿会自动保持在唯一可用的模式。

## 钓出生物

钓出生物是可配置功能。默认总开关开启，但安全默认值只允许水生生物：

| 类别 | 默认状态 | 默认相对权重 |
|---|---:|---:|
| 水生生物 | 开启 | 70 |
| 陆地动物 | 关闭 | 20 |
| 陆地怪物 | 关闭 | 7 |
| 下界生物 | 关闭 | 2.5 |
| Boss 类生物 | 关闭 | 0.5 |
| 末影龙 | 关闭 | 隶属 Boss 类别 |

默认基础概率为 `5%`。启用幸运附魔效果时，Lucky Catch 每级额外增加 `3` 个百分点，最终概率上限为 `25%`；例如默认配置下幸运 III 的整体生物概率为 `14%`。分类权重只在对应类别已开启且本次已命中生物概率后参与相对选择。

`netherEntitiesOnlyInNether` 默认是 `false`，但下界生物类别本身默认关闭。如果管理员开启下界生物又希望它们只在下界出现，应同时开启此维度限制。

生成过程会检查实体是否允许召唤、生成空间、液体环境、世界边界、已加载区块和玩家碰撞。选择或生成失败时，本次收杆会继续使用普通物品战利品；生物结果与物品结果互斥，不会在同一次收杆中同时获得两者。

末影龙具有额外限制：当前版本即使同时开启 Boss 和末影龙开关，也会在最终生成阶段安全拒绝末影龙，并回退到普通战利品。这是因为模组无法保证原版 Boss 战状态在任意维度中安全成立。

> 开启下界生物、敌对生物、Boss 或末影龙后，可能在普通世界中生成高危险实体，造成玩家死亡、建筑破坏或存档体验异常。服务器管理员应谨慎开启。

## 配置

客户端安装 Mod Menu 后，可从模组列表打开基于 Cloth Config 的配置界面。配置也会保存在 `config/hook_and_reel.json`；专用服务器管理员可以直接编辑该文件，并在修改后重启服务器。Mod Menu 只提供入口，不是模组运行的硬依赖。

### 幸运附魔

| 配置键 | 默认值 | 作用 |
|---|---:|---|
| `luckyEnchantmentEnabled` | `true` | 启用 Lucky Catch 效果 |
| `luckyBonusPerLevel` | `1.5` | 每级加入本次钓鱼上下文的额外 Luck |
| `allowStackWithLuckOfTheSea` | `true` | 允许与海之眷顾共存并同时生效 |
| `luckyThreeInstantCatchEnabled` | `true` | 启用幸运 III 快速钓鱼 |
| `luckyThreeInstantCatchDelaySeconds` | `1.0` | 鱼钩入水后的等待时间 |
| `luckyThreeAutoRetract` | `true` | 制造咬钩后自动执行原版收杆流程 |

### 能力冷却

| 配置键 | 默认值 | 作用 |
|---|---:|---|
| `grapplingHookCooldownSeconds` | `10.0` | 成功拖拽或拖拽超时后的钩锁完整冷却 |
| `anchorHookCooldownSeconds` | `1.5` | 解除挂钩或完成挂钩移动后的完整冷却 |
| `anchorHookFailedCastDelaySeconds` | `0.25` | 挂钩发射后未建立锚点时的短延迟 |

两种能力使用独立、保存在鱼竿上的冷却数据。将对应完整冷却设置为 `0` 会关闭该能力冷却。

### 钩锁

| 配置键 | 默认值 |
|---|---:|
| `grapplingHookEnabled` | `true` |
| `maxChargeTimeSeconds` | `1.5` |
| `minimumGrappleRange` | `8.0` |
| `grappleLevel1MaxRange` / `grappleLevel2MaxRange` / `grappleLevel3MaxRange` | `24.0` / `36.0` / `48.0` |
| `pullStrength` | `0.12` |
| `maximumPullSpeed` | `1.5` |
| `itemPullSpeedMultiplier` | `1.6` |
| `pullStopDistance` | `2.0` |
| `maxPullDurationSeconds` | `8.0` |
| `allowPullPlayers` | `true` |
| `allowPullBosses` | `false` |
| `blockPullingEnabled` | `true` |
| `allowPullBlockEntities` | `false`，当前版本强制关闭 |
| `maximumBlockHardness` | `50.0` |
| `blockPullSpeedMultiplier` | `0.8` |
| `maxBlockPullDurationSeconds` | `10.0` |
| `blockPullStopDistance` | `2.5` |
| `blockPullDurabilityCost` | `3` |

### 挂钩

| 配置键 | 默认值 |
|---|---:|
| `anchorHookEnabled` | `true` |
| `anchorLevel1MaxRange` / `anchorLevel2MaxRange` / `anchorLevel3MaxRange` | `24.0` / `36.0` / `48.0` |
| `reelAcceleration` | `0.10` |
| `maximumReelSpeed` | `1.60` |
| `maximumReelUpDurationSeconds` | `5.0` |
| `reelingLateralControlEnabled` | `true` |
| `reelingLateralControlStrength` | `0.10` |
| `maximumReelingLateralSpeed` | `0.45` |
| `wallDetectionDistance` | `0.40` |
| `wallClingDurationSeconds` | `10.0` |
| `wallClingStrength` | `0.08` |
| `wallClimbSpeed` / `wallClimbDownSpeed` | `0.18` / `0.15` |
| `wallHorizontalMoveSpeed` | `0.12` |
| `wallJumpUpVelocity` / `wallJumpOutVelocity` | `0.42` / `0.55` |
| `rappelEnabled` | `true` |
| `rappelSpeed` | `2.5` 格/秒 |
| `anchorDurabilityCost` | `1` |
| `anchorHookCooldownSeconds` | `1.5` 秒 |

配置界面和文件还提供收线目标偏移、绕障检测、绳索约束、摆荡控制、落地解除以及 HUD 显示等细项。

### 钓出生物

| 配置键 | 默认值 |
|---|---:|
| `allowFishingEntities` | `true` |
| `fishingEntityBaseChance` | `0.05` |
| `fishingEntityChanceBonusPerLuckyLevel` | `0.03` |
| `maximumFishingEntityChance` | `0.25` |
| `allowAquaticEntities` | `true` |
| `allowLandAnimals` | `false` |
| `allowLandMonsters` | `false` |
| `allowNetherEntities` | `false` |
| `netherEntitiesOnlyInNether` | `false` |
| `allowBossEntities` | `false` |
| `allowEnderDragonFishing` | `false`；当前生成器仍会安全拒绝末影龙 |
| `aquaticEntityCategoryWeight` | `70.0` |
| `landAnimalCategoryWeight` | `20.0` |
| `landMonsterCategoryWeight` | `7.0` |
| `netherEntityCategoryWeight` | `2.5` |
| `bossEntityCategoryWeight` | `0.5` |

多人专用服务器中，实际钓鱼结果、碰撞、移动、拖拽和冷却时间均以服务器配置与服务器验证为准。客户端配置不能覆盖服务器规则。

## 安装

### 版本与依赖

| 组件 | 要求 |
|---|---|
| Minecraft | `1.21.1` |
| Java | `21` 或更高版本 |
| Fabric Loader | `0.19.3` 或更高版本 |
| Fabric API | 必需；项目使用 `0.116.13+1.21.1` 构建和验证，模组元数据未声明更具体的最低版本 |
| Cloth Config API | 必需，`15.0.140` 或更高版本 |
| Mod Menu | 可选但推荐；项目使用 `11.0.4`，模组元数据建议 `11.0.4` 或更高版本 |

### 安装步骤

1. 安装 Minecraft 1.21.1。
2. 安装适配 1.21.1 的 Fabric Loader，并满足上述最低版本。
3. 安装 Fabric API。
4. 安装 Cloth Config API。
5. 推荐在客户端安装 Mod Menu，以便从游戏内打开配置界面；不安装也能运行模组。
6. 将 Hook & Reel 的 JAR 放入 `.minecraft/mods`。
7. 启动游戏。

多人游戏中，客户端和服务端都需要安装 Hook & Reel、Fabric API 与 Cloth Config API，并保持模组版本一致。Mod Menu 只需按需要安装在客户端，专用服务器不需要它。

## 兼容性与注意事项

- 目标环境为 Minecraft 1.21.1 Fabric 与 Java 21。
- 普通鱼竿保持原版行为；未附魔鱼竿不会启用钩锁或挂钩能力。
- 服务端负责最终碰撞判定、实体与玩家移动、方块处理、钓鱼结果和冷却验证。
- 高速鱼钩会沿当前 tick 的运动路径执行连续碰撞检查，降低穿透方块的情况；路径涉及未加载区块时会安全终止。
- 方块实体默认且当前强制不支持拖拽。
- 方块拖拽包含中断恢复、所有权状态和复制防护：中断时会尝试恢复原位、在当前位置附近重新放置，最后才回退为恰好一个方块物品。服务器管理员仍应在启用重要存档前先备份。
- 生物、Boss、下界生物和其他危险设置需要谨慎开启；模组不保证所有 Boss 都能在任意位置安全生成。
- 与大幅修改 `FishingRodItem`、`FishingHook`、钓鱼 Loot 流程或玩家移动系统的模组可能存在兼容风险。
- 本项目不宣称与所有模组完全兼容；安装到大型整合包前建议先在测试存档验证。

## 数据包扩展

服务器管理员和整合包作者可以通过数据包扩展以下 `hook_and_reel` 标签。内置文件位于 `src/main/resources/data/hook_and_reel/tags/`。

### 生物标签

| 标签 | 作用 |
|---|---|
| `hook_and_reel:fishable_aquatic_entities` | 可钓出的水生生物 |
| `hook_and_reel:fishable_land_animals` | 可钓出的陆地动物 |
| `hook_and_reel:fishable_land_monsters` | 可钓出的陆地怪物 |
| `hook_and_reel:fishable_nether_entities` | 可钓出的下界生物 |
| `hook_and_reel:fishable_boss_entities` | 可钓出的 Boss 类生物 |
| `hook_and_reel:unfishable_entities` | 无论分类如何都禁止钓出的实体 |
| `hook_and_reel:grapple_pull_blacklist` | `allowPullBosses` 关闭时禁止拖拽的实体 |
| `hook_and_reel:fishable_entities` | 内置分类标签的汇总，供其他数据包引用；当前随机选择直接读取各分类标签 |

### 方块标签

| 标签 | 作用 |
|---|---|
| `hook_and_reel:grapple_immovable` | 钩锁不可拖拽的方块 |
| `hook_and_reel:grapple_multiblock_unsafe` | 不安全多方块结构 |
| `hook_and_reel:swing_unhookable` | 不可作为挂钩锚点的方块 |

使用数据包扩展标签不会绕过配置、生成空间、权限、硬度和其他服务器安全检查。

## 从源码构建

需要 Java 21。克隆仓库后在项目根目录执行：

Windows PowerShell：

```powershell
.\gradlew.bat build
```

Linux / macOS：

```bash
./gradlew build
```

构建会执行编译、JUnit 测试和 Fabric GameTest。生成的可发布 JAR 位于 `build/libs/hook_and_reel-1.0.0.jar`，带 `-sources` 后缀的文件是源码 JAR。

## 许可证

本项目由 `ikunkk02-afk` 以 [MIT License](LICENSE) 发布。

## English summary

Hook & Reel is a Fabric mod for Minecraft 1.21.1 that extends vanilla fishing rods with configurable fishing luck, entity catches, grappling pulls, movable block retrieval, anchor-based reel-up movement, rappelling, and wall clinging. Install it on both the client and server with Fabric API and Cloth Config API; Mod Menu is optional.
