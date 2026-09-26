package kcl.tokenlogin.client.auth

import kcl.tokenlogin.client.mixin.MinecraftUserAccessor
import net.minecraft.client.Minecraft
import net.minecraft.client.User
import org.slf4j.LoggerFactory
import java.util.Optional
import java.util.UUID

object UserManager {
	private val logger = LoggerFactory.getLogger("tokenlogin")

	fun setUser(accessToken: String, name: String, profileId: String): Boolean {
		return try {
			val uuid = parseProfileId(profileId)
			val user = User(name, uuid, accessToken, Optional.empty(), Optional.empty())
			applyUser(user)
			logger.info("Set user: {} ({})", name, uuid)
			true
		} catch (e: Exception) {
			logger.error("Failed to set user from token", e)
			false
		}
	}

	fun applyUser(user: User) {
		(Minecraft.getInstance() as MinecraftUserAccessor).setUserField(user)
	}

	fun parseProfileId(uuidString: String): UUID {
		val formatted = if (uuidString.contains("-")) {
			uuidString
		} else {
			"${uuidString.substring(0, 8)}-${uuidString.substring(8, 12)}-${uuidString.substring(12, 16)}-${uuidString.substring(16, 20)}-${uuidString.substring(20)}"
		}
		return UUID.fromString(formatted)
	}
}
