package kcl.tokenlogin.client.auth

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object McTokenAuth {
	private val client = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(10))
		.build()

	private val gson = Gson()

	fun authenticate(token: String): AuthResult {
		return try {
			val request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.minecraftservices.com/minecraft/profile"))
				.timeout(Duration.ofSeconds(10))
				.header("Authorization", "Bearer $token")
				.GET()
				.build()

			val response = client.send(request, HttpResponse.BodyHandlers.ofString())
			when (response.statusCode()) {
				200 -> {
					val json = gson.fromJson(response.body(), JsonObject::class.java)
					val id = json?.get("id")?.asString
					val name = json?.get("name")?.asString
					if (!id.isNullOrBlank() && !name.isNullOrBlank()) {
						AuthResult.Success(name, id)
					} else {
						AuthResult.Failure("返回格式无效")
					}
				}
				401 -> AuthResult.Failure("Token 无效或已过期")
				404 -> AuthResult.Failure("没有 Minecraft 档案")
				else -> AuthResult.Failure("验证失败: ${response.statusCode()}")
			}
		} catch (e: Exception) {
			AuthResult.Failure("连接错误: ${e.message}")
		}
	}

	sealed class AuthResult {
		data class Success(val name: String, val id: String) : AuthResult()
		data class Failure(val message: String) : AuthResult()
	}
}
