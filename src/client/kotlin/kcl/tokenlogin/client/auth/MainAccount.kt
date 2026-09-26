package kcl.tokenlogin.client.auth

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.User
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.util.Optional
import java.util.UUID

object MainAccount {
	private val logger = LoggerFactory.getLogger("tokenlogin")
	private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
	private val file = FabricLoader.getInstance().configDir.resolve("tokenlogin/main-account.json")

	@Volatile
	private var capturedThisSession = false

	@Volatile
	private var snapshot: Snapshot? = null

	fun capture(client: Minecraft) {
		if (capturedThisSession) {
			return
		}
		val user = client.user
		val token = user.accessToken
		if (token.isBlank()) {
			logger.warn("Client user has no access token yet")
			return
		}
		val snap = Snapshot(
			name = user.name,
			uuid = user.profileId,
			accessToken = token,
			xuid = user.xuid.orElse(null),
			clientId = user.clientId.orElse(null),
		)
		snapshot = snap
		capturedThisSession = true
		save(snap)
		logger.info("Recorded main account: {} ({})", snap.name, snap.uuid)
	}

	fun name(): String? = current()?.name

	fun token(): String? = current()?.accessToken

	fun restore(): Boolean {
		val snap = current() ?: return false
		UserManager.applyUser(snap.toUser())
		logger.info("Wrote main account token back: {} ({})", snap.name, snap.uuid)
		return true
	}

	private fun current(): Snapshot? = snapshot

	private fun save(snap: Snapshot) {
		try {
			Files.createDirectories(file.parent)
			val json = JsonObject()
			json.addProperty("name", snap.name)
			json.addProperty("uuid", snap.uuid.toString())
			json.addProperty("accessToken", snap.accessToken)
			if (snap.xuid != null) {
				json.addProperty("xuid", snap.xuid)
			}
			if (snap.clientId != null) {
				json.addProperty("clientId", snap.clientId)
			}
			Files.writeString(file, gson.toJson(json))
		} catch (e: Exception) {
			logger.warn("Failed to persist main account", e)
		}
	}

	data class Snapshot(
		val name: String,
		val uuid: UUID,
		val accessToken: String,
		val xuid: String?,
		val clientId: String?,
	) {
		fun toUser(): User = User(
			name,
			uuid,
			accessToken,
			Optional.ofNullable(xuid),
			Optional.ofNullable(clientId),
		)
	}
}
