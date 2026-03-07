# Sunohara Mingdao

基于 Paper 1.21.11 的 Minecraft 服务器濒死救援插件。

## 功能特性

- 🛡️ **自动濒死救援** - 当玩家血量即将耗尽时自动救援一次
- ⚙️ **两种运作模式**
  - **一次性模式** - 每个玩家只能救援一次（永久）
  - **可再生模式** - 支持冷却时间后重复使用
- 💬 **可配置的消息提示** - 独立控制两种模式的消息显示
- ✨ **视觉反馈** - 粒子效果、音效、自定义消息
- 🔧 **灵活配置** - 可自定义恢复血量、冷却时间等参数
- 📝 **完整权限系统** - 支持权限管理
- 📋 **详细日志记录** - 记录所有救援事件
- 🧪 **药水效果支持** - 触发时给予玩家自定义药水效果（如附魔金苹果Buff）

## 安装

1. 确保服务器运行 Paper 1.21.11 或以上版本
2. 将编译后的 JAR 文件放入 `plugins/` 文件夹
3. 重启服务器
4. 查看服务器日志确认插件加载成功
5. 编辑 `plugins/SunoharaMingdao/config.yml` 自定义配置

## 使用

### 命令

#### 查看状态
```
/mingdao status
/mhelp
```
查看个人名刀使用状态、剩余冷却时间等信息

#### 查看帮助
```
/mingdao help
```
显示插件帮助信息

#### 重新加载配置
```
/mingdao reload
```
重新加载配置文件而无需重启服务器（需要管理员权限）

#### 重置玩家状态
```
/mingdao reset <玩家名>
```
重置指定玩家的名刀状态（需要管理员权限）

### 运作原理

名刀会在以下情况触发：

1. **玩家受伤** - 检测到玩家受伤事件
2. **死亡判定** - 受伤后血量 ≤ 0（即将死亡）
3. **名刀认证** - 检查玩家是否可以使用名刀
4. **执行救援** - 恢复指定血量、取消伤害、播放效果和消息提示

### 两种运作模式

#### 一次性模式（ONCE）
- 每个玩家只能使用一次（永久）
- 适合想给予玩家"最后一道防线"的服务器
- 使用后不会再获得救援

#### 可再生模式（REGENERATE）
- 每个玩家可以多次使用，但有冷却时间
- 冷却时间可配置（默认60分钟）
- 使用后需要等待一定时间才能再次使用

## 权限系统

| 权限 | 说明 | 默认值 |
|------|------|-------|
| `sunohara.mingdao.use` | 使用名刀救援功能 | true |
| `sunohara.mingdao.admin` | 管理员权限（重载、重置等） | op |
| `sunohara.mingdao.help` | 查看帮助信息 | true |
| `sunohara.mingdao.*` | 所有名刀权限 | false |

## 配置文件

插件会自动在首次运行时创建 `plugins/SunoharaMingdao/config.yml`

**基础配置示例：**

```yaml
# 功能启用
enabled: true

# 名刀模式：once（一次性）或 regenerate（可再生）
mingdao-mode: "once"

# 一次性模式配置
once-mode:
  show-message: true        # 是否显示救援提示消息

# 可再生模式配置
regenerate-mode:
  cooldown-minutes: 60      # 冷却时间（分钟）
  show-message: true        # 是否显示救援或冷却提示
  show-cooldown: true       # 是否显示具体冷却剩余时间

# 恢复设置
restoration:
  health-restore: 20        # 恢复血量（20=满血，10=半血）
  show-effect: true         # 是否显示粒子效果

# 音效设置
effects:
  play-sound: true          # 是否播放声音
  sound: "UI_TOTEM_ACTIVATED"

# 日志设置
logging:
  log-mingdao-usage: true
  level: "INFO"
```

详见 [config.yml](src/main/resources/config.yml) 文件了解所有配置选项

## 高级配置

### 消息提示系统

插件支持灵活的消息提示控制，可根据模式和需求自定义显示：

**配置消息显示：**

```yaml
# 一次性模式 - 显示救援消息
once-mode:
  show-message: true

# 可再生模式 - 显示救援和冷却消息
regenerate-mode:
  show-message: true
  show-cooldown: true
```

**自定义消息内容：**

```yaml
messages:
  mingdao-saved: "§6[名刀] §a你被名刀救援了！§f获得了 {health} 点生命"
  cooldown-remaining: "§6[名刀] §c名刀还需冷却 {remaining} 分钟"
  already-used: "§6[名刀] §c你已经使用过名刀了！"
  no-permission: "§c你没有使用名刀的权限"
  disabled: "§c名刀插件已禁用"
```

### 效果配置

```yaml
# 视觉效果
restoration:
  health-restore: 20        # 恢复的血量等级
  show-effect: true         # 是否显示粒子效果

# 音效
effects:
  play-sound: true          # 是否播放声音
  sound: "UI_TOTEM_ACTIVATED"
```

### 药水效果/Buff配置

插件支持在名刀触发时给予玩家自定义的药水效果，模拟附魔金苹果等物品的效果。

**配置格式：**

```yaml
restoration:
  buffs:
    - "效果名称:持续时间(秒):等级"
```

**等级说明：** 配置中的等级为 0 起始，与游戏内罗马数字对应关系为：I=0、II=1、III=2、IV=3。例如「恢复 II」应写为 `REGENERATION:20:1`，而非 `REGENERATION:20:2`。

**常用效果列表：**

| 效果名称 | 中文名 | 附魔金苹果对应 |
|---------|--------|---------------|
| REGENERATION | 恢复 | ✓ |
| ABSORPTION | 吸收 | ✓ |
| DAMAGE_RESISTANCE | 抗性提升 | ✓ |
| FIRE_RESISTANCE | 防火 | ✓ |
| SATURATION | 饱和 | ✓ |
| SPEED | 速度 | |
| STRENGTH | 力量 | |
| JUMP | 跳跃提升 | |
| INVISIBILITY | 隐身 | |
| WATER_BREATHING | 水下呼吸 | |
| NIGHT_VISION | 夜视 | |

**附魔金苹果效果配置示例（1.21.11 版本）：**

1.21.11 附魔金苹果效果为：2 分钟伤害吸收 IV、20 秒生命恢复 II、5 分钟抗火、5 分钟抗性提升 I。配置如下：

```yaml
restoration:
  buffs:
    - "ABSORPTION:120:3"       # 伤害吸收 IV，2 分钟
    - "REGENERATION:20:1"      # 生命恢复 II，20 秒
    - "FIRE_RESISTANCE:300:0"  # 抗火 I，5 分钟
    - "DAMAGE_RESISTANCE:300:0" # 抗性提升 I，5 分钟
```

**禁用 Buff：**

```yaml
restoration:
  buffs: []  # 清空列表以禁用 Buff
```

## 使用场景举例

### 场景 1：生存服务器 - 适度保护

一个玩家遇到意外伤害也能再活一次，增加游戏体验：

```yaml
mingdao-mode: "regenerate"
regenerate-mode:
  cooldown-minutes: 120     # 2小时冷却
  show-message: true
restoration:
  health-restore: 10        # 恢复5颗心
```

### 场景 2：极限模式 - 一生一次

用户一生只有一次机会逃脱死亡，珍贵而难忘：

```yaml
mingdao-mode: "once"
once-mode:
  show-message: true
restoration:
  health-restore: 20        # 满血恢复
```

### 场景 3：无敌模式 - 频繁救援

玩家可以频繁使用名刀，降低死亡风险，适合新手服或休闲服：

```yaml
mingdao-mode: "regenerate"
regenerate-mode:
  cooldown-minutes: 5       # 5分钟冷却
  show-message: true
restoration:
  health-restore: 20        # 满血恢复
```

### 场景 4：静默模式 - 无提示救援

玩家不知道自己有名刀保护，营造惊喜感：

```yaml
once-mode:
  show-message: false
regenerate-mode:
  show-message: false
effects:
  play-sound: false
restoration:
  show-effect: false
```

## 故障排查

### 名刀没有触发
- 检查 `enabled: true` 是否已启用
- 确认玩家拥有 `sunohara.mingdao.use` 权限
- 检查玩家是否真的受到致命伤害（血量会降到0或以下）
- 查看服务器日志是否有错误信息

### 看不到消息提示
- 检查对应模式的 `show-message` 配置
- 确认消息配置中的文本不为空
- 用 `/mingdao reload` 重新加载配置

### 看不到效果/听不到声音
- 检查 `restoration.show-effect` 是否为 true
- 检查 `effects.play-sound` 是否为 true
- 确认游戏设置中音效和粒子未被禁用

### 冷却时间不工作
- 确保使用了 `regenerate` 模式
- 检查 `cooldown-minutes` 配置是否合理
- 用 `/mingdao status` 查看实际冷却时间

### 名刀触发条件
- 名刀会在玩家即将死亡（受伤后血量归零）时自动触发
- 不需要配置触发阈值，所有致命伤害都会被拦截

## 项目结构

```
sunohara-mingdao/
├── src/main/java/com/sunohara/mingdao/
│   ├── MingdaoPlugin.java      # 主插件类
│   ├── MingdaoCommand.java     # 命令处理
│   ├── MingdaoManager.java     # 名刀逻辑
│   ├── MingdaoConfig.java      # 配置管理
│   ├── MingdaoMode.java        # 模式枚举
│   └── PlayerMingdaoData.java  # 玩家数据
├── src/main/resources/
│   ├── plugin.yml
│   └── config.yml
├── pom.xml
└── README.md
```

## 项目架构

- **MingdaoPlugin** - 主插件类，事件监听与初始化
- **MingdaoCommand** - 命令处理
- **MingdaoManager** - 名刀逻辑
- **MingdaoConfig** - 配置加载与保存
- **MingdaoMode** - 模式枚举（ONCE、REGENERATE）
- **PlayerMingdaoData** - 玩家名刀数据

## 构建

```bash
mvn clean package
```

编译后的 JAR 文件将生成在 `target/` 目录下。

## 更新日志

### v1.0.0
- **新增：** 基础濒死救援功能
- **新增：** 两种运作模式（一次性/可再生）
- **新增：** 可配置的消息提示系统
- **新增：** 完全自定义的视觉反馈
- **新增：** 完整的权限系统
- **新增：** 管理员配置和重置命令

## 许可证

MIT License

## 支持

如有问题或建议，欢迎提出 Issue 或 Pull Request。
