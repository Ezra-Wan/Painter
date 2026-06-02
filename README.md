# Painter (粉刷匠人)

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21-brightgreen)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.0.167-blue)](https://neoforged.net/)
[![Mod Version](https://img.shields.io/badge/Version-2.8.7-orange)](https://github.com/SouthernWall_404/Painter/releases)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red)](LICENSE)

Painter 是一个为 Minecraft 1.21 NeoForge 平台设计的高级装饰性模组，为玩家提供了强大的墙面粉刷、纹理定制和区域选择功能。通过直观的 tools 系统，玩家可以轻松地将任何方块表面粉刷成其他方块的样式，实现无限创意的建筑装饰。
源仓库https://gitee.com/SouthernWall_404/painter

## 📋 功能特性

### 🎨 核心工具

#### 油漆刷 (Paint Brush)
- **基础粉刷** - 副手持有目标方块，主手持刷子右键点击即可将方块表面粉刷成所选样式
- **多方向支持** - 支持六个方向（东、西、南、北、上、下）的独立粉刷
- **纹理循环** - 支持循环切换纹理 UV 偏移，实现纹理平铺效果
- **属性切换** - 支持切换方块的方向、轴向、开关等属性

#### 粉笔 (Chulk)
- **精确选区** - 通过两点确定法选择粉刷区域
- **墙面识别** - 自动识别并高亮显示选区内的有效墙面
- **潜行清除** - 按下潜行键 + 鼠标按键快速清除选区
- **视觉反馈** - 实时渲染选区边界和可粉刷面

#### 油漆桶 (Paint Bucket)
- **大面积填充** - 以墙面为单位进行批量喷涂
- **选区配合** - 可与粉笔选区系统配合使用，精确控制粉刷范围
- **高效操作** - 一次操作即可完成整个墙面的粉刷

### 🛠️ 高级功能

#### 智能渲染系统
- **视锥剔除** - 自动隐藏不可见的面，优化渲染性能
- **环境光遮蔽 (AO)** - 支持真实的光照效果计算
- **动态刷新** - 区块加载时自动刷新可见性和光照数据
- **缓存优化** - 使用 BakedQuad 缓存减少重复计算

#### 数据持久化
- **附件系统** - 基于 NeoForge Attachment API 存储粉刷数据
- **区块索引** - 使用 PaintChunkInfo 快速定位含粉刷数据的区块
- **NBT 序列化** - 完整的存档保存和加载机制
- **网络同步** - 服务端与客户端数据自动同步

#### 兼容性
- **完整方块** - 支持所有碰撞箱完整的标准方块
- **台阶类方块** - 支持上半台阶、下半台阶和完整台阶
- **方向性方块** - 支持楼梯、活板门等具有方向属性的方块
- **扩展架构** - 基于 AbstractPaint 的可扩展设计，易于添加新方块类型

## 📦 安装说明

### 前置要求
- **Minecraft**: 1.21
- **NeoForge**: 21.0.167 或更高版本
- **Laplace API**: 0.5.1（必需的前置库）

> ⚠️ **重要提示**: Laplace API 是 Painter 的核心依赖，提供了数学运算、渲染引擎和网络同步等基础功能。

### 安装步骤
1. 确保已安装对应版本的 NeoForge
2. 下载 Painter 模组文件 (.jar)
3. 将模组文件放入 Minecraft 的 `mods` 文件夹中
4. 启动游戏并享受！

## 🎮 使用方法

### 油漆刷 (Paint Brush) - 基础粉刷

1. **准备材料**
   - 将想要应用的方块纹理放在副手槽位
   - 主手握住油漆刷

2. **执行粉刷**
   - 右键点击目标方块的任意面
   - 该面将被粉刷成副手方块的纹理

3. **高级操作**
   - **循环纹理**: 对已粉刷的面再次右键可循环切换 UV 偏移
   - **切换方向**: 对于有方向的方块（如楼梯），右键可旋转方向
   - **移除粉刷**: 使用空手或特定工具可恢复原始状态

### 粉笔 (Chulk) - 区域选择

1. **设置选区起点**
   - 左键点击第一个角落方块
   - 屏幕会显示选区预览

2. **设置选区终点**
   - 右键点击对角线的另一个角落
   - 两点之间的所有有效墙面将被标记

3. **查看选区**
   - 选区边界会以高亮线条显示
   - 可粉刷的面会有特殊标记

4. **清除选区**
   - 按下潜行键 (Shift) + 任意鼠标按键
   - 或重新选择新的起点

5. **配合油漆桶使用**
   - 在选区内使用油漆桶可批量粉刷
   - 仅影响选区内的有效墙面

### 油漆桶 (Paint Bucket) - 批量粉刷

1. **基本使用**
   - 副手放置目标纹理方块
   - 主手握住油漆桶
   - 右键点击墙面进行大面积粉刷

2. **选区模式**
   - 先用粉笔划定选区
   - 在选区内使用油漆桶
   - 自动填充所有可粉刷的墙面

3. **注意事项**
   - 油漆桶以"墙面"为单位操作
   - 连续的同方向平面会被视为一个墙面
   - 适合快速装饰大型建筑

## 🔧 开发环境设置

### 前提条件
- **JDK**: Java 21 或更高版本
- **IDE**: IntelliJ IDEA（推荐）或 Eclipse
- **Gradle**: 项目自带 Gradle Wrapper
- **Git**: 版本控制系统

### 快速开始

```bash
# 1. 克隆仓库
git clone https://github.com/SouthernWall_404/Painter.git
cd Painter

# 2. 构建模组
./gradlew build

# 3. 运行测试环境
./gradlew runClient    # 启动客户端
./gradlew runServer    # 启动服务器
./gradlew runGameTestServer  # 运行游戏测试

# 4. 生成数据资源
./gradlew runData      # 运行数据生成器
```

### IDE 配置

#### IntelliJ IDEA
1. 打开项目根目录
2. 等待 Gradle 自动同步（或手动点击 "Reload All Gradle Projects"）
3. 如遇依赖问题，执行：`./gradlew --refresh-dependencies`
4. 确保 JDK 21 已正确配置

#### Eclipse
1. 执行 `./gradlew eclipse` 生成 Eclipse 配置文件
2. 导入项目到 Eclipse
3. 刷新项目以确保依赖正确加载

### 项目结构详解

```
Painter/
├── src/
│   ├── main/
│   │   ├── java/com/SouthernWall_404/Painter/
│   │   │   ├── API/                      # 公共 API 接口
│   │   │   │   ├── Paint/                # 粉刷系统
│   │   │   │   │   ├── API/              # 核心抽象类
│   │   │   │   │   │   ├── IRender       # 渲染接口
│   │   │   │   │   │   ├── AbstractRender # 渲染基类
│   │   │   │   │   │   └── AbstractPaint  # 粉刷基类
│   │   │   │   │   ├── Attachment/       # 数据附件
│   │   │   │   │   │   ├── PaintInfo     # 区块粉刷数据
│   │   │   │   │   │   └── PaintChunkInfo # 区块索引
│   │   │   │   │   ├── Imply/            # 具体实现
│   │   │   │   │   │   ├── SimpleBlockPaint  # 普通方块
│   │   │   │   │   │   └── SlabBlockPaint    # 台阶方块
│   │   │   │   │   └── Util/             # 工具类
│   │   │   │   │       ├── Paint/        # 粉刷辅助
│   │   │   │   │       └── RenderUtil    # 渲染工具
│   │   │   │   └── Tool/                 # 工具系统
│   │   │   │       ├── SelectedZone      # 选区管理
│   │   │   │       └── Wall/             # 墙面处理
│   │   │   ├── Client/                   # 客户端逻辑
│   │   │   │   ├── Config/               # 客户端配置
│   │   │   │   ├── PaintRender.java      # 粉刷渲染器
│   │   │   │   ├── SelectedRenderer.java # 选区渲染器
│   │   │   │   └── CustomRenderTypes.java # 自定义渲染类型
│   │   │   ├── Common/                   # 通用逻辑
│   │   │   │   ├── Config/               # 通用配置
│   │   │   │   ├── Content/              # 物品/方块注册
│   │   │   │   ├── Data/                 # 数据生成
│   │   │   │   ├── Event/                # 事件监听
│   │   │   │   ├── Init/                 # 初始化模块
│   │   │   │   │   ├── ModItems          # 物品注册
│   │   │   │   │   ├── ModAttachments    # 附件注册
│   │   │   │   │   └── ModNetwork        # 网络注册
│   │   │   │   └── World/                # 世界相关
│   │   │   └── Painter.java              # 模组主类
│   │   ├── resources/                    # 静态资源
│   │   │   └── assets/painter/
│   │   │       ├── lang/                 # 语言文件
│   │   │       ├── models/               # 模型文件
│   │   │       └── textures/             # 纹理文件
│   │   └── templates/                    # 模板文件
│   │       └── META-INF/
│   │           └── neoforge.mods.toml    # 模组元数据
│   └── generated/                        # 自动生成的资源
├── build.gradle                          # Gradle 构建脚本
├── gradle.properties                     # 构建配置
└── README.md                             # 项目文档
```

### 关键架构说明

#### AbstractRender 框架
- **IRender 接口**: 定义渲染的基本契约
- **AbstractRender<T, F>**: 泛型基类，T 为渲染对象类型，F 为标志类型
- **AbstractPaint**: 继承自 AbstractRender，专门处理方块粉刷
  - 使用 Direction 作为标志（6个方向）
  - 管理 BlockState 材质映射
  - 处理 UV 偏移和纹理循环
  - 实现 AO 光照计算

#### 数据持久化
- **PaintInfo**: 存储在 LevelChunk 附件中，包含该区块所有粉刷数据
- **PaintChunkInfo**: 全局索引，记录哪些区块包含粉刷数据
- **序列化**: 使用 NeoForge 的 INBTSerializable 接口
- **同步**: 通过 Laplace API 的网络系统自动同步到客户端

#### 渲染流程
1. 玩家视角变化或区块加载时触发 `PaintRender.setChanged()`
2. 下一帧渲染前调用 `redraw()` 重建渲染缓存
3. `getRenderNearby()` 获取玩家周围的粉刷数据
4. 对每个粉刷块调用 `update()` 刷新可见性和 AO
5. 在 `AFTER_TRANSLUCENT_BLOCKS` 阶段执行实际渲染
6. 使用视锥剔除跳过不可见的方块

## ⚙️ 配置选项

Painter 提供了客户端和服务器端配置文件，位于 `.minecraft/config/painter-*.toml`。

### 客户端配置 (painter-client.toml)

```toml
# 粉刷层偏移量，控制粉刷纹理与原方块的间距
# 默认值: 0.005
# 范围: 0.001 - 0.05
paint_offset = 0.005

# 选区边框颜色 (RGB)
# 默认值: #00FF00 (绿色)
selection_color = "#00FF00"

# 是否启用选区渲染
enable_selection_render = true
```

### 服务器配置 (painter-common.toml)

```toml
# 最大粉刷距离（方块）
# 防止远程操作滥用
max_paint_distance = 64

# 是否允许在保护区域使用粉刷工具
allow_in_protected_areas = false
```

> 💡 **提示**: 修改配置后需要重启游戏才能生效。

---

## 🌐 多语言支持

目前支持以下语言：
- 🇨🇳 简体中文 (zh_cn)

欢迎贡献其他语言的翻译！翻译文件位于 `src/main/resources/assets/painter/lang/`。

### 如何贡献翻译

1. 复制 `zh_cn.json` 并重命名为目标语言代码（如 `en_us.json`）
2. 翻译其中的键值对
3. 提交 Pull Request

---

## 🐛 已知问题

### 区块加载延迟渲染

**问题描述**: 远离已粉刷区域后重新加载时，粉刷效果可能不会立即显示。

**临时解决方案**:
- 在附近放置或破坏一个方块触发区块更新
- 或使用光源方块刷新光照

**状态**: ✅ 已通过可见性刷新机制修复，但极端情况下仍可能出现

### 性能考虑

- **大量粉刷块**: 当单个区块内有数百个粉刷块时，可能会影响帧率
- **建议**: 在大型建筑装饰时，分区域逐步进行

### 兼容性限制

- **不支持的方块类型**:
  - 流体方块（水、岩浆）
  - 植物类方块（花、草）
  - 实体方块（画、物品框）
  - 红石元件（部分可能显示异常）

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！无论是 Bug 报告、功能建议还是代码贡献。

### 代码贡献流程

1. **Fork 本仓库**
   ```bash
   git fork https://github.com/SouthernWall_404/Painter.git
   ```

2. **创建功能分支**
   ```bash
   git checkout -b feature/your-feature-name
   # 或
   git checkout -b fix/bug-description
   ```

3. **开发并测试**
   - 遵循现有代码风格
   - 添加必要的注释
   - 确保没有引入新的警告

4. **提交更改**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   # 或
   git commit -m "fix: resolve bug description"
   ```

5. **推送到分支**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **开启 Pull Request**
   - 详细描述你的更改
   - 说明测试方法
   - 关联相关的 Issue

### 代码规范

- **命名**: 使用 camelCase（变量/方法）和 PascalCase（类名）
- **注释**: 为公共 API 添加 Javadoc 注释
- **格式**: 使用 IDE 默认的 Java 格式化规则
- **导入**: 按字母顺序组织导入语句

### Issue 报告模板

报告 Bug 时请包含：
- Minecraft 版本
- NeoForge 版本
- Painter 版本
- Laplace API 版本
- 详细的复现步骤
- 预期行为 vs 实际行为
- 崩溃报告（如果有）

---

## 📄 许可证

本项目采用 **All Rights Reserved** 许可证。

- ❌ 未经明确书面许可，不得复制、修改、分发或使用本项目的任何部分
- ❌ 不得将本模组用于商业用途
- ❌ 不得在本模组基础上创建衍生作品并发布
- ✅ 个人非商业使用不受限制

详细条款请参阅 [LICENSE](LICENSE) 文件。

---

## 🔗 相关链接

### 官方资源
- [NeoForge 官方文档](https://docs.neoforged.net/)
- [NeoForge Discord 社区](https://discord.neoforged.net/)
- [Minecraft 官方网站](https://www.minecraft.net/)

### 项目依赖
- [Laplace API](https://github.com/SouthernWall_404/LaplaceAPI) - 核心前置库
  - 提供数学运算库 (Math37)
  - 提供渲染引擎 (RegulappleEngine)
  - 提供附件系统 (VertinCore)
  - 提供网络同步 (xNetwork)

### 社区
- [GitHub Issues](https://github.com/SouthernWall_404/Painter/issues) - 问题反馈
- [GitHub Releases](https://github.com/SouthernWall_404/Painter/releases) - 版本下载

---

## 📞 联系方式

如有问题、建议或合作意向，请通过以下方式联系：

- 📧 **GitHub Issues**: [提交问题](https://github.com/SouthernWall_404/Painter/issues)
- 💬 **Discord**: SouthernWall_404#0000
- 🎮 **Minecraft 社区论坛**

---

## 🙏 致谢

感谢以下开源项目和社区的贡献：

- **NeoForge Team** - 提供强大的模组开发平台
- **Mojang Studios** - Minecraft 游戏本体
- **Laplace API** - 提供基础框架支持
- **所有贡献者** - 感谢每一位提交 Issue 和 PR 的用户

---

<div align="center">

**Painter** - 让你的建筑创作更加丰富多彩！🎨

*Made with ❤️ by SouthernWall_404*

</div>
