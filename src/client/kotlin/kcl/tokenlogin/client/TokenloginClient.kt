package kcl.tokenlogin.client

import kcl.tokenlogin.Tokenlogin
import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

object TokenloginClient : ClientModInitializer {
	private val logger = LoggerFactory.getLogger(Tokenlogin.MOD_ID)

	override fun onInitializeClient() {
		logger.info("Tokenlogin client ready")
	}
}
