package kcl.tokenlogin

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Tokenlogin : ModInitializer {
	const val MOD_ID: String = "tokenlogin"

	private val LOGGER = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		LOGGER.info("Tokenlogin loaded")
	}
}
