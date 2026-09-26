package kcl.tokenlogin.client.auth

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

object TokenLogin {
	@Volatile
	private var statusMessage: String? = null
	@Volatile
	private var statusColor: Int = 0xFFFFFFFF.toInt()
	@Volatile
	private var statusAt: Long = 0L

	fun loginFromClipboard(client: Minecraft) {
		val raw = client.keyboardHandler.clipboard
		if (raw.isNullOrBlank()) {
			setStatus("剪贴板为空", 0xFFFF0000.toInt())
			return
		}
		val token = raw.trim()
		setStatus("正在验证 Token...", 0xFFFFFFFF.toInt())
		runAuth(client, token) { success ->
			if (UserManager.setUser(token, success.name, success.id)) {
				setStatus("登录成功: ${success.name}", 0xFF00FF00.toInt())
			} else {
				setStatus("无法写入会话", 0xFFFF0000.toInt())
			}
		}
	}

	fun refreshOriginal(client: Minecraft) {
		val token = OriginalSession.originalToken()
		if (token.isNullOrBlank()) {
			setStatus("还没有记录初始 Token", 0xFFFF0000.toInt())
			return
		}
		val original = OriginalSession.originalName()
		val current = client.user
		if (current.name == original && current.accessToken == token) {
			setStatus("当前已是初始账号: $original", 0xFF00FF00.toInt())
			return
		}
		setStatus("正在恢复初始账号...", 0xFFFFFFFF.toInt())
		runAuth(client, token) { success ->
			if (OriginalSession.restore()) {
				setStatus("已恢复初始账号: ${success.name}", 0xFF00FF00.toInt())
			} else {
				setStatus("无法写入初始会话", 0xFFFF0000.toInt())
			}
		}
	}

	fun extractStatus(extractor: GuiGraphicsExtractor, font: Font, width: Int, height: Int) {
		val message = statusMessage ?: return
		if (System.currentTimeMillis() - statusAt > 5000) {
			statusMessage = null
			return
		}
		extractor.centeredText(font, Component.literal(message), width / 2, height / 2, statusColor)
	}

	private fun runAuth(
		client: Minecraft,
		token: String,
		onSuccess: (McTokenAuth.AuthResult.Success) -> Unit,
	) {
		CompletableFuture.supplyAsync { McTokenAuth.authenticate(token) }
			.thenAccept { result ->
				client.execute {
					when (result) {
						is McTokenAuth.AuthResult.Success -> onSuccess(result)
						is McTokenAuth.AuthResult.Failure -> {
							setStatus("失败: ${result.message}", 0xFFFF0000.toInt())
						}
					}
				}
			}
			.exceptionally { error ->
				client.execute { setStatus("错误: ${error.message}", 0xFFFF0000.toInt()) }
				null
			}
	}

	private fun setStatus(message: String, color: Int) {
		statusMessage = message
		statusColor = color
		statusAt = System.currentTimeMillis()
	}
}
