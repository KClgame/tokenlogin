# Tokenlogin

**This branch is Minecraft 26.2.** The older 26.1.2 code stays on [`main`](https://github.com/KClgame/tokenlogin/tree/main).

Fabric + Kotlin **client-only** mod. Clipboard token login, plus restore of the launcher session captured at startup.

Do not install this together with KCl Utils (it already has clipboard token login).

## Usage

1. Copy a valid Minecraft access token.
2. Open **Multiplayer**.
3. Top-right:
   - **TokenLogin - Login From Clipboard** — switch the current session to that token.
   - **Token Refresh - Restore Original** — write back the account that was logged in when the game started.

The original session is captured once when Minecraft launches, and also written to:

```
.minecraft/config/tokenlogin/original.json
```

That file contains an access token. Treat it as a secret.

## Build

```
.\gradlew.bat build -x test
```

Output: `build/libs/tokenlogin-1.0.0.jar`

Use the remapped jar from `build/libs`, not a `-dev` jar.

---

## 中文

**纯客户端** Fabric + Kotlin 模组，面向 **Minecraft 26.2**。

- **从剪贴板登录**：用复制的 access token 切换当前会话。
- **Token Refresh**：游戏启动时记下启动器登录的账号和 token，点按钮写回该会话（切号之后可以还原）。

初始会话会存到 `config/tokenlogin/original.json`，里面有 token，不要外传。

不要和 KCl Utils 同时安装。
