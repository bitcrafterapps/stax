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
        val prompt = """
    You are an expert WSOP tournament player and visual analyst. Using this image, calculate the total value of the poker chips based on the visible stacks.
    Stacks that are not full should calculate each chips 
    Each full stack contains 20 chips. Use the following chip values and full stack totals:

    - Black chip = $100 → Full stack = $2,000
    - Pink chip = $500 → Full stack = $10,000
    - Yellow chip = $1,000 → Full stack = $20,000
    - Red chip = $5,000 → Full stack = $100,000
    - Green chip = $25,000 → Full stack = $500,000
    - Light Pink chip = $100,000 → Full stack = $2,000,000
    - Light Green chip = $500,000 → Full stack = $5,000,000
    - Dark Grey chip = $1,000,000 → Full stack = $20,000,000

    Instructions:
    - Count the number of full stacks for each chip color. Denominations are list on center top of chip below WSOP
    - If a stack is not full, estimate the chip count using the visible height, alignment, and spacing. Assume standard poker chip thickness.
    - Only include chips that are clearly visible and identifiable by color.
    - Do not guess if the color is ambiguous or obscured.

    Return only the total value of all chips as a currency do not provide explanation just return the value. Example: $3,500,000
""".trimIndent()

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