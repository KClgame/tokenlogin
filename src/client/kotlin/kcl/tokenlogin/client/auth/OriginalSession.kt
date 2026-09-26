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

object OriginalSession {
	private val logger = LoggerFactory.getLogger("tokenlogin")
	private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
	private val file = FabricLoader.getInstance().configDir.resolve("tokenlogin/original.json")

	@Volatile
	private var snapshot: Snapshot? = null

	fun capture(client: Minecraft) {
		if (snapshot != null) {
			return
		}
		val user = client.user
		val token = user.accessToken
		if (token.isBlank()) {
			logger.warn("Launch session has no access token")
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
		save(snap)
		logger.info("Recorded original session: {} ({})", snap.name, snap.uuid)
	}

	fun originalName(): String? = snapshot()?.name

	fun restore(): Boolean {
		val snap = snapshot() ?: return false
		val user = snap.toUser()
		UserManager.applyUser(user)
		logger.info("Restored original session: {} ({})", snap.name, snap.uuid)
		return true
	}

	fun originalToken(): String? = snapshot()?.accessToken

	private fun snapshot(): Snapshot? {
		snapshot?.let { return it }
		val loaded = loadFromFile() ?: return null
		snapshot = loaded
		return loaded
	}

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
			logger.warn("Failed to persist original session", e)
		}
	}

	private fun loadFromFile(): Snapshot? {
		return try {
			if (!Files.isRegularFile(file)) {
				return null
			}
			val json = gson.fromJson(Files.readString(file), JsonObject::class.java) ?: return null
			val token = json.get("accessToken")?.asString
			val name = json.get("name")?.asString
			val uuidRaw = json.get("uuid")?.asString
			if (token.isNullOrBlank() || name.isNullOrBlank() || uuidRaw.isNullOrBlank()) {
				return null
			}
			Snapshot(
				name = name,
				uuid = UUID.fromString(uuidRaw),
				accessToken = token,
				xuid = json.get("xuid")?.asString,
				clientId = json.get("clientId")?.asString,
			)
		} catch (e: Exception) {
			logger.warn("Failed to load original session", e)
			null
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
