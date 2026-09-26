# Tokenlogin

Fabric + Kotlin **client-only** mod: clipboard token login, plus **Token Refresh** that writes the startup session back.

## Versions

| Minecraft | Branch | Tag | Mod |
|---|---|---|---|
| **26.2** (this tree) | [`26.2`](https://github.com/KClgame/tokenlogin/tree/26.2) | `26.2-v1.0.2` | `1.0.2+26.2` |
| 26.1.2 | [`26.1.2`](https://github.com/KClgame/tokenlogin/tree/26.1.2) / [`main`](https://github.com/KClgame/tokenlogin/tree/main) | `26.1.2-v0.0.1` | `0.0.1` |

Do not merge version branches into each other. Do not install this together with KCl Utils.

## Usage

1. Copy a valid Minecraft access token.
2. Open **Multiplayer**.
3. Multiplayer screen:
   - Top-left **Token Refresh - Restore Main Account** — write back the token captured when the client started.
   - Top-right **TokenLogin - Login From Clipboard** — switch the current session to that token.

The main account is recorded on client start (current `Minecraft.user` access token). Refresh does **not** call Mojang again; it writes that token back into the session.

A copy is also stored at:

```
.minecraft/config/tokenlogin/main-account.json
```

Treat that file as a secret.

## Build

```
.\gradlew.bat build -x test
```

Output: `build/libs/tokenlogin-1.0.2+26.2.jar`
