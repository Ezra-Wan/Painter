# Painter 模组远程协作操作规范

本文档基于 GitFlow 工作流，为 Painter 模组的远程协作开发提供标准化操作指南。

当前角色：EzraW

兄弟开发者：SouthernWall_404

## 📋 目录

- [分支策略](#分支策略)
- [工作流程](#工作流程)
- [提交规范](#提交规范)
- [代码审查](#代码审查)
- [版本发布](#版本发布)
- [冲突解决](#冲突解决)
- [最佳实践](#最佳实践)

---

## 🌿 分支策略

Painter 项目采用标准 GitFlow 分支模型，包含以下分支类型：

### 主要分支（长期存在）

| 分支名    | 说明                           | 保护规则                                       |
| --------- | ------------------------------ | ---------------------------------------------- |
| `main`    | 生产环境分支，存储稳定版本     | 禁止直接推送，仅接受来自 release/hotfix 的合并 |
| `develop` | 开发环境分支，包含最新开发成果 | 禁止直接推送，仅接受来自 feature/bugfix 的合并 |

### 辅助分支（临时存在）

| 分支类型   | 命名规范             | 来源分支  | 目标分支           | 生命周期     |
| ---------- | -------------------- | --------- | ------------------ | ------------ |
| 功能分支   | `feature/<功能名称>` | `develop` | `develop`          | 功能开发期间 |
| 修复分支   | `bugfix/<问题描述>`  | `develop` | `develop`          | Bug 修复期间 |
| 发布分支   | `release/<版本号>`   | `develop` | `main` + `develop` | 发布准备期间 |
| 热修复分支 | `hotfix/<问题描述>`  | `main`    | `main` + `develop` | 紧急修复期间 |

### 分支命名示例

```
<类型>/<内容>-<日期>-<作者>
```

```
feature/油漆稀释剂-26.5.10-南墙      # 创作了油漆稀释剂更新-时间2026.05.10-作者南墙
```

---

## 🔄 工作流程

### 1. 开始新功能开发

```bash
- 对develop 分支进行<更新>操作
- 从git Flow插件中start feature
- 设定feature名称
- 开始编码
```

### 2. 完成功能开发

```bash
- 确保 develop 分支是最新的
- 拉取最新 develop
- 并从develop合并到您的本地分支
- 解决可能的冲突
- 推送功能分支到远程仓库
- 在 GitHub 上创建 Pull Request
   - 源分支: feature/功能名称-日期-作者
   - 目标分支: develop
   - 填写 PR 描述，关联相关 Issue
- 等待审查并通过 CI 检查
- 获得维护者批准后合并 PR
```

### 3. 修复 Bug

```bash
- 从 develop 创建 bugfix 分支
- 修复问题并提交
- 推送并创建 PR 到 develop
- 等待审查并合并
- 删除 bugfix 分支


- 如果 Bug 存在于生产环境（main 分支），应使用 hotfix 流程（见第 5 节）
- Bug 修复分支应从 develop 创建，而非 main
```

### 4. 发布新版本

```bash
- 从 develop 创建 release 分支
- 更新版本号和相关配置
   - 修改 gradle.properties 中的 mod_version参数
   - 更新 README.md 中的版本信息
   - 在 logs/ 目录创建或更新 changelog 文件
- 提交版本更新
- 进行最终测试和修复
- 合并到 main 和 develop
- 推送所有更改
- 打标签
- 在 GitHub 上发布 Release
- 删除 release 分支


- release 分支仅用于最后的测试和修复，不应添加新功能
- 使用 `--no-ff` 参数保留合并记录，便于追踪版本历史
```

### 5. 紧急热修复

```bash
- 从 main 创建 hotfix 分支
- 修复紧急问题并提交
- 合并回 main 和 develop
- 推送并打标签
- 在 GitHub 上发布 Hotfix Release
- 删除 hotfix 分支


- hotfix 分支仅用于修复生产环境的紧急问题
- 必须从 main 分支创建，确保修复的是稳定版本
- 修复后必须同时合并到 main 和 develop，避免问题重现
```

---

## 📝 提交规范

Painter 项目采用约定式提交（Conventional Commits）规范：

### 提交消息格式

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### 类型说明

| 类型       | 说明                   | 示例                                         |
| ---------- | ---------------------- | -------------------------------------------- |
| `feat`     | 新功能                 | `feat: add stair block painting support`     |
| `fix`      | Bug 修复               | `fix: resolve texture UV offset issue`       |
| `docs`     | 文档变更               | `docs: update installation guide`            |
| `style`    | 代码格式（不影响功能） | `style: format code according to guidelines` |
| `refactor` | 代码重构               | `refactor: simplify paint rendering logic`   |
| `perf`     | 性能优化               | `perf: optimize chunk loading performance`   |
| `test`     | 测试相关               | `test: add unit tests for paint system`      |
| `chore`    | 构建过程或辅助工具变动 | `chore: update gradle dependencies`          |

### 完整示例

```bash
# 简单提交
git commit -m "feat: add chalk selection tool"

# 带范围的提交
git commit -m "feat(render): implement AO lighting calculation"

# 详细提交
git commit -m "fix(paint): resolve slab rendering issue

Fixed the problem where half slabs were not rendering correctly
when painted on the bottom face.

Closes #123"
```

---

## 🔍 代码审查

### Pull Request 流程

1. **创建 PR**
   - 在 GitHub 上从功能分支向 develop 分支发起 PR
   - 填写清晰的标题和描述
   - 关联相关的 Issue（使用 `Closes #XXX` 或 `Fixes #XXX`）

2. **PR 描述模板**

```markdown
## 变更类型
- [ ] 新功能 (feat)
- [ ] Bug 修复 (fix)
- [ ] 文档更新 (docs)
- [ ] 代码重构 (refactor)
- [ ] 性能优化 (perf)

## 变更说明
简要描述本次变更的内容和目的。

## 测试方法
说明如何测试这些变更，包括：
- 手动测试步骤
- 自动化测试用例
- 截图或录屏（如适用）

## 相关 Issue
Closes #XXX

## 检查清单
- [ ] 代码符合项目规范
- [ ] 已添加必要的注释
- [ ] 已更新相关文档
- [ ] 本地测试通过
- [ ] 没有引入新的警告
```

3. **审查要点**
   - 代码是否符合 Java 编码规范
   - 是否遵循 Painter 架构设计原则
   - 是否有充分的测试覆盖
   - 是否影响现有功能
   - 性能是否有退化

4. **合并要求**
   - 至少需要 1 名维护者批准
   - 所有 CI 检查必须通过
   - 无冲突且与 develop 分支同步

---

## 📦 版本发布

### 版本号规范

Painter 的版本号规范如下

‘build.version.feature-hotfixX'`
- build 表示架构，在总体架构不变的情况下，不做变动
- version 表示版本，表示当前的release版本
- feature 表示功能，表示当前新增功能版本号
- hotfix 后缀用于表示热修复版本

### 发布流程

1. **准备阶段**
   - 从 develop 创建 release 分支
   - 更新版本号和变更日志
   - 进行最终测试

2. **发布阶段**
   - 合并到 main 和 develop
   - 创建 Git 标签
   - 在 GitHub 发布 Release
   - 上传构建产物（JAR 文件）

3. **通知阶段**
   - 更新项目文档
   - 在社区发布公告
   - 通知依赖此模组的其他开发者


## ⚔️ 冲突解决

### 预防冲突

1. **频繁同步**
   - 每天开始工作前更新 develop 分支
2. **模块化开发**
   - 避免多人同时修改同一文件
   - 将大功能拆分为小模块
   - 明确各成员的负责区域

3. **沟通协作**
   - 在团队频道告知正在修改的文件
   - 对于核心文件的变更提前讨论
   - 使用 Issue 跟踪大型变更

### 解决冲突

```bash
# 1. 更新本地 develop 分支
git checkout develop
git pull origin develop

# 2. 切换回功能分支
git checkout feature/your-feature

# 3. 合并 develop（可能出现冲突）
git merge develop

# 4. 如果有冲突，Git 会提示
#    打开冲突文件，查找冲突标记：
#    <<<<<<< HEAD
#    你的代码
#    =======
#    他人的代码
#    >>>>>>> develop

# 5. 手动解决冲突
#    - 保留需要的代码
#    - 删除冲突标记
#    - 确保代码逻辑正确

# 6. 标记冲突已解决
git add <resolved-file>

# 7. 完成合并
git commit -m "merge: resolve conflicts with develop"

# 8. 推送更新
git push origin feature/your-feature
```

### 冲突解决工具推荐

- **IDE 内置工具**: IntelliJ IDEA、VS Code 都有优秀的冲突解决界面
- **专用工具**: Beyond Compare、Meld、KDiff3
- **在线工具**: GitHub 网页编辑器（适用于简单冲突）

---

## ✨ 最佳实践

### 1. 分支管理

- ✅ 每个功能/修复使用独立分支
- ✅ 分支命名清晰且具有描述性
- ✅ 及时删除已合并的分支
- ❌ 不要在 main 或 develop 上直接开发
- ❌ 不要长期存在不活跃的功能分支

### 2. 提交习惯

- ✅ 小而频繁的提交（每次提交一个完整的功能实现所需内容）
- ✅ 使用规范的提交消息
- ✅ 提交前运行测试确保代码可用
- ❌ 不要提交大量无关更改
- ❌ 不要使用模糊的提交消息（如 "update"、"fix"）

### 3. 代码质量

- ✅ 遵循项目代码规范
- ✅ 为新功能编写测试
- ✅ 保持代码简洁和可读性
- ✅ 及时更新文档
- ❌ 不要提交调试代码或注释掉的代码
- ❌ 不要忽略编译器警告

### 4. 协作沟通

- ✅ 在开始大型变更前发起讨论
- ✅ 及时回复代码审查意见
- ✅ 在 PR 中详细说明变更内容
- ✅ 遇到问题时寻求帮助
- ❌ 不要未经讨论就修改核心架构
- ❌ 不要忽视团队成员的反馈

### 5. 持续集成

- ✅ 推送前在本地运行构建和测试
- ✅ 关注 CI 状态，及时修复失败
- ✅ 使用 `.github/workflows/build.yml` 自动化检查
- ❌ 不要忽略 CI 失败
- ❌ 不要强制推送（force push）到共享分支

### 6. 安全注意事项

- ✅ 定期更新依赖库版本
- ✅ 审查第三方库的安全性
- ✅ 不在代码中硬编码敏感信息
- ❌ 不要提交密钥、令牌等敏感数据
- ❌ 不要禁用安全检查

---

## 📞 支持与反馈

如有任何关于协作流程的问题或建议，请通过以下方式联系：

- 📧 **GitHub Issues**: [提交问题](https://github.com/SouthernWall_404/Painter/issues)
- 💬 **Discord**: SouthernWall_404#0000
- 📖 **项目文档**: [README.md](../../README.md)

---

<div align="center">

**遵循规范，高效协作！** 🤝

*最后更新: 2026-05-10*

</div>