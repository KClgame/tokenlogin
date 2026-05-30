package com.kcl.tokenlogin.client

import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

object TokenloginClient : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("tokenlogin")

    override fun onInitializeClient() {
        logger.info("TokenLogin client initialized (ported from 1.21.11)")
    }
}
