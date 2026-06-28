# 伪装为OPPO - LSPosed 模块

专为 OPPO/一加设备打造，在 LSPosed 框架下伪装 Build 信息为目标设备（一加 Ace 6T），支持按包名精准控制。

## 功能

- 🎭 伪装 `Build.MANUFACTURER / BRAND / MODEL / DEVICE` 等字段
- 🔧 Hook `SystemProperties` native 读取（彻底解决 `SDK_INT` 伪装失效问题）
- 📦 通过 `assets/packages.txt` 配置目标包名，无需重新编译
- 🎯 支持精确包名 + 前缀匹配两种规则

## 默认伪装的包名

| 前缀/包名 | 说明 |
|---|---|
| `com.heytap.*` | 小布助手、浏览器、云服务等 |
| `com.coloros.*` | 安全中心、游戏空间、文件管理等 |
| `com.nearme.*` | 游戏中心、钱包等 |
| `com.oppo.*` / `com.oplus.*` | OPPO 系 App |
| `com.finshell.wallet` | OPPO 钱包 |
| `com.finshell.fin` | OPPO 金融 |

## 使用方法

1. 安装 **LSPosed** 框架
2. 安装本模块 APK
3. LSPosed → 模块 → 勾选「伪装为OPPO」
4. 勾选需要伪装的目标 App
5. 重启设备生效

## 自定义包名

编辑模块 APK 内 `assets/packages.txt`（或直接修改源码重新编译）：

```
# 精确包名
com.tencent.mm

# 前缀匹配（以 . 结尾）
com.heytap.
```

## 编译

```bash
git clone https://github.com/你的用户名/oppo-spoof-lsposed.git
cd oppo-spoof-lsposed
./gradlew assembleRelease
```

或直接使用 GitHub Actions 自动编译（Fork 后推送即自动构建）。

## 下载

前往 [Releases](https://github.com/你的用户名/oppo-spoof-lsposed/releases) 页面下载最新 APK。

## 许可证

MIT
