package com.example.stax.data

import android.graphics.Bitmap
import android.util.Base64
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

class OpenAiService(private val apiKey: String) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getChipCount(bitmap: Bitmap): String {
        val base64Image = bitmapToBase64(bitmap)
        val requestBody = createRequestBody(base64Image)

        val response: OpenAiResponse = client.post("https://api.openai.com/v1/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()

        return response.choices.firstOrNull()?.message?.content ?: "Could not get a response."
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun createRequestBody(base64Image: String): OpenAiRequest {
        val prompt = "You are an expert poker player. Count the number of poker chips by color in the image and calculate the total dollar value using the following denominations:" +
"Red = $5,Green = $25,Blue = $50,Black = $100,Purple = $500,Yellow = $1000,Maroon = $5000" +
"Return only the total value as a number (e.g., 1250). Do not include any explanation or chip count."

        val content = listOf(
            Content(type = "text", text = prompt),
            Content(type = "image_url", imageUrl = ImageUrl("data:image/jpeg;base64,$base64Image"))
        )
        val message = Message(role = "user", content = content)
        return OpenAiRequest(model = "gpt-4o", messages = listOf(message), maxTokens = 300)
    }
}

@Serializable
data class OpenAiRequest(
    val model: String,
    val messages: List<Message>,
    @SerialName("max_tokens")
    val maxTokens: Int
)

@Serializable
data class Message(
    val role: String,
    val content: List<Content>
)

@Serializable
data class Content(
    val type: String,
    val text: String? = null,
    @SerialName("image_url")
    val imageUrl: ImageUrl? = null
)

@Serializable
data class ImageUrl(
    val url: String
)

@Serializable
data class OpenAiResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: ResponseMessage
)

@Serializable
data class ResponseMessage(
    val content: String
) 