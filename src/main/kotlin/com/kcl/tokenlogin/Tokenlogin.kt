package com.kcl.tokenlogin

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Tokenlogin : ModInitializer {
    private val logger = LoggerFactory.getLogger("tokenlogin")

    override fun onInitialize() {
        logger.info("TokenLogin initialized (26.1.2 port)")
    }
}
