package kcl.tokenlogin.client

import kcl.tokenlogin.Tokenlogin
import kcl.tokenlogin.client.auth.MainAccount
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory

object TokenloginClient : ClientModInitializer {
	private val logger = LoggerFactory.getLogger(Tokenlogin.MOD_ID)

	override fun onInitializeClient() {
		try {
			MainAccount.capture(Minecraft.getInstance())
		} catch (e: Exception) {
			logger.debug("Main account capture deferred to first tick", e)
		}
		ClientTickEvents.START_CLIENT_TICK.register { client ->
			MainAccount.capture(client)
		}
		logger.info("Tokenlogin client ready")
	}
}
