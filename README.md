# Hook & Reel

Hook & Reel 是一个面向 Minecraft 1.21.1 的 Fabric 鱼竿扩展模组，使用 Java 21。

当前第一阶段包含：

- 数据驱动的鱼竿附魔“幸运 / Lucky Catch”，最高 III 级。
- 只影响本次钓鱼战利品上下文的额外 Luck，不修改玩家属性或原版战利品表。
- Cloth Config 配置文件与 Mod Menu 配置界面。
- 为后续 PULL / SWING 模式准备的物品 Data Component 架构。

钩锁拖拽、摆荡、绳降和模式切换尚未在本阶段实现。

## 依赖

- Fabric Loader
- Fabric API
- Cloth Config API
- Mod Menu（仅用于客户端配置入口）

## 构建

```powershell
.\gradlew.bat build
```

生成的模组文件位于 `build/libs/`。

## 许可证

本项目使用 [MIT License](LICENSE)。
