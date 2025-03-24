# 贡献指南

感谢您对SingleFlight项目的关注！本文档提供了为该项目做出贡献的指南和说明。

## 目录

- [行为准则](#行为准则)
- [开发环境设置](#开发环境设置)
- [编码标准](#编码标准)
- [测试](#测试)
- [Pull Request流程](#pull-request流程)
- [问题报告](#问题报告)

## 行为准则

参与本项目时，您应遵守以下行为准则：

- 使用友好和包容的语言
- 尊重不同的观点和经验
- 优雅地接受建设性批评
- 关注对社区最有利的事情
- 对其他社区成员表示同理心

## 开发环境设置

### 前提条件

- Java开发工具包(JDK) 17或更高版本
- Maven 3.6或更高版本
- Git

### 项目设置

1. 在GitHub上fork本仓库
2. 将您的fork克隆到本地：
   ```bash
   git clone https://github.com/您的用户名/singleflight.git
   cd singleflight
   ```
3. 添加原始仓库作为上游远程：
   ```bash
   git remote add upstream https://github.com/原始所有者/singleflight.git
   ```
4. 使用Maven构建项目：
   ```bash
   mvn clean install
   ```

## 编码标准

在为SingleFlight做贡献时，请遵循以下编码标准：

### Java代码风格

- 使用4个空格进行缩进（不使用制表符）
- 遵循Java命名约定：
  - 类名使用`CamelCase`
  - 方法和变量名使用`camelCase`
  - 常量使用`UPPER_SNAKE_CASE`
- 最大行长度为120个字符
- 始终为公共方法和类添加适当的Javadoc注释
- 使用有意义的变量和方法名

### 文档

- 更改功能时更新文档
- 编写清晰简洁的提交消息
- 为新功能包含示例

## 测试

所有贡献都应包含适当的测试：

- 为新功能编写单元测试
- 确保在提交Pull Request之前所有测试都通过
- 争取高测试覆盖率
- 使用Maven运行测试：
  ```bash
  mvn test
  ```

## Pull Request流程

1. 将您的fork更新到最新的上游版本
2. 为您的功能或错误修复创建一个新分支：
   ```bash
   git checkout -b feature/您的功能名称
   ```
   或
   ```bash
   git checkout -b fix/您要修复的问题
   ```
3. 进行更改并提交，使用清晰、描述性的消息
4. 将您的分支推送到您的fork：
   ```bash
   git push origin feature/您的功能名称
   ```
5. 向主仓库提交Pull Request
6. 确保PR描述清楚地描述了问题和解决方案
7. 在PR描述中引用任何相关的问题

### Pull Request审查标准

在您的Pull Request被合并之前，它将被审查以下方面：

- 代码质量和风格
- 测试覆盖率
- 文档
- 与现有代码的兼容性

## 问题报告

报告问题时，请包含：

- 清晰描述性的标题
- 问题的详细描述
- 重现问题的步骤
- 预期行为
- 实际行为
- 环境信息（JDK版本、操作系统等）
- 任何相关的日志或截图

---

感谢您为SingleFlight做出贡献！您的努力有助于使这个项目对每个人都更好。