package com.kcl.tokenlogin.client.auth

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.client.User
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Manages the current Minecraft user (replaces the old Session concept in 26.1.2+).
 *
 * This is the 26.1.2 port of the original SessionManager.
 */
object UserManager {
    private val logger = LoggerFactory.getLogger("tokenlogin")
    private val gson = Gson()

    /**
     * Applies a new user using the given access token.
     * This will fetch the profile from Mojang and replace the current user.
     *
     * @return true if the user was successfully set, false otherwise.
     */
    fun setUser(accessToken: String): Boolean {
        return try {
            val client = Minecraft.getInstance()

            val profileData = fetchProfile(accessToken) ?: return false

            val uuidString = profileData.get("id").asString
            val username = profileData.get("name").asString

            val formattedUuid = if (uuidString.contains("-")) {
                uuidString
            } else {
                "${uuidString.substring(0, 8)}-${uuidString.substring(8, 12)}-${uuidString.substring(12, 16)}-${uuidString.substring(16, 20)}-${uuidString.substring(20)}"
            }

            val uuid = UUID.fromString(formattedUuid)

            val user = User(
                username,
                uuid,
                accessToken,
                Optional.empty(),
                Optional.empty()
            )

            (client as com.kcl.tokenlogin.client.mixin.MinecraftClientAccessor).setUserField(user)

            logger.info("Successfully set user: $username ($uuid)")
            true
        } catch (e: Exception) {
            logger.error("Failed to set user with provided token", e)
            false
        }
    }

    /**
     * Returns the currently active user, or null if not available.
     */
    fun getCurrentUser(): User? {
        return try {
            val client = Minecraft.getInstance()
            (client as? com.kcl.tokenlogin.client.mixin.MinecraftClientAccessor)?.getUserField()
        } catch (e: Exception) {
            logger.warn("Failed to get current user", e)
            null
        }
    }

    private fun fetchProfile(accessToken: String): JsonObject? {
        return try {
            val httpClient = java.net.http.HttpClient.newBuilder().build()
            val request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://api.minecraftservices.com/minecraft/profile"))
                .header("Authorization", "Bearer $accessToken")
                .GET()
                .build()

            val response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                gson.fromJson(response.body(), JsonObject::class.java)
            } else {
                logger.warn("Failed to fetch profile, status code: ${response.statusCode()}")
                null
            }
        } catch (e: Exception) {
            logger.error("Exception while fetching profile", e)
            null
        }
    }
}
