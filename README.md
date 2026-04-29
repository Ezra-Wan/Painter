# Painter (粉刷匠人)

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21-brightgreen)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.0.167-blue)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red)](LICENSE)

Painter 是一个为 Minecraft 1.21+ NeoForge 平台设计的装饰性模组，为玩家提供了强大的墙面粉刷和区域选择功能。

## 📋 功能特性

### 🎨 核心功能
- **油漆刷 (Brush)** - 使用副手持有方块，主手持刷子右键即可将方块表面粉刷成所选方块样式
- **粉笔 (Chulk)** - 精确选择粉刷区域，支持两点选区系统
- **油漆桶 (Paint Bucket)** - 大面积填充染料，以墙面为单位进行喷涂

### 🛠️ 高级功能
- **区域选择系统** - 通过粉笔工具实现精确的区域选择和控制
- **方向识别** - 支持六个方向（东、西、南、北、上、下）的墙面操作
- **兼容性** - 当前版本兼容完整方块和台阶类方块

## 📦 安装说明

### 前置要求
- Minecraft 1.21
- NeoForge 21.0.167 或更高版本
- Laplace API 0.2.50

### 安装步骤
1. 确保已安装对应版本的 NeoForge
2. 下载 Painter 模组文件 (.jar)
3. 将模组文件放入 Minecraft 的 `mods` 文件夹中
4. 启动游戏并享受！

## 🎮 使用方法

### 油漆刷 (Brush)
1. 将想要粉刷的方块放在副手
2. 主手持有油漆刷
3. 右键点击目标方块进行粉刷

### 粉笔 (Chulk) - 区域选择
1. **设置第一点**: 左键点击确定选区起点
2. **设置第二点**: 右键点击确定选区终点
3. **清除选区**: 按下潜行键 + 任意鼠标按键清除当前选区

### 油漆桶 (Paint Bucket)
1. 类似油漆刷的操作方式
2. 以墙面为单位进行大面积喷涂
3. 可配合选区功能控制粉刷范围

## 🔧 开发环境设置

### 前提条件
- Java 21 JDK
- Git
- Gradle (包含在项目中)

### 构建步骤
```bash
# 克隆仓库
git clone https://github.com/SouthernWall_404/Painter.git
cd Painter

# 构建模组
./gradlew build

# 运行客户端测试
./gradlew runClient

# 运行服务器测试
./gradlew runServer
```

### IDE 设置
1. 使用 IntelliJ IDEA 或 Eclipse 打开项目
2. 等待 Gradle 同步完成
3. 如遇依赖问题，运行 `./gradlew --refresh-dependencies`

## 📁 项目结构

```
src/
├── main/
│   ├── java/com/SouthernWall_404/Painter/
│   │   ├── API/              # API 接口
│   │   ├── Client/           # 客户端相关代码
│   │   ├── Common/           # 通用逻辑
│   │   │   ├── Content/      # 内容定义
│   │   │   ├── Data/         # 数据处理
│   │   │   ├── Event/        # 事件处理
│   │   │   ├── Init/         # 初始化模块
│   │   │   ├── Network/      # 网络通信
│   │   │   └── World/        # 世界相关
│   │   └── Painter.java      # 主类
│   └── resources/assets/painter/
│       └── lang/             # 语言文件
└── generated/                # 自动生成的资源
```

## 🌐 多语言支持

目前支持以下语言：
- 简体中文 (zh_cn)

欢迎贡献其他语言的翻译！

## 🤝 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 All Rights Reserved 许可证。未经明确许可，不得复制、修改、分发或使用本项目的任何部分。

## 🔗 相关链接

- [NeoForge 官方文档](https://docs.neoforged.net/)
- [NeoForge Discord](https://discord.neoforged.net/)
- [Laplace API](https://github.com/SouthernWall_404/LaplaceAPI)

## ❓ 常见问题

**Q: 模组不工作怎么办？**
A: 请确认已正确安装 NeoForge 和 Laplace API 前置模组。

**Q: 如何报告 Bug？**
A: 请在 GitHub Issues 中详细描述问题和复现步骤。

**Q: 是否支持其他 Minecraft 版本？**
A: 目前仅支持 Minecraft 1.21，未来可能会考虑移植到其他版本。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- GitHub Issues
- Minecraft 社区论坛

---

*Painter - 让你的建筑创作更加丰富多彩！*