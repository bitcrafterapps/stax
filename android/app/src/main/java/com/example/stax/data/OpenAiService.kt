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

    suspend fun getChipCount(bitmap: Bitmap, chipHints: String? = null): String {
        val base64Image = bitmapToBase64(bitmap)
        val requestBody = createRequestBody(base64Image, chipHints)

        val response: OpenAiResponse = client.post("https://api.openai.com/v1/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()

        return response.choices.firstOrNull()?.message?.content ?: "Could not get a response."
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // Scale down if needed to keep within OpenAI's ~20MB image limit while staying sharp
        val maxDim = 1920
        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else bitmap
        scaled.compress(Bitmap.CompressFormat.JPEG, 95, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun createRequestBody(base64Image: String, chipHints: String?): OpenAiRequest {
        val chipSection = if (!chipHints.isNullOrBlank()) {
            """
CONFIGURED CHIP VALUES FOR THIS SESSION:
$chipHints
Use these denominations when you can match chip colors to the list above.
"""
        } else {
            """
CHIP IDENTIFICATION:
Read the denomination printed on each chip face directly. If the denomination is not legible,
use standard casino chip color conventions as a fallback:
  White=$1, Red=$5, Blue=$10, Green=$25, Black=$100, Purple=$500, Yellow=$1000, Pink=$5000
"""
        }

        val systemPrompt = """
You are a professional poker chip counter with expert-level visual analysis skills.
Your job is to precisely count poker chip stacks from images and calculate accurate totals.
You are methodical, accurate, and never guess when chip values or counts are unclear.
""".trimIndent()

        val userPrompt = """
Carefully analyze the poker chip stacks in this image and calculate the total value.

$chipSection

COUNTING METHOD — follow these steps in order:

STEP 1 — READ DENOMINATIONS:
  - Look closely at each visible chip face for a printed number or denomination text.
  - The denomination is usually printed at the top or center of the chip face.
  - Reading the printed number directly is MORE RELIABLE than guessing from color alone.
  - If a chip face denomination is readable, use that number — do not override it with a color guess.
  - Only fall back to color-based identification if no denomination text is readable.

STEP 2 — COUNT STACKS:
  - A full, uniform casino chip stack contains exactly 20 chips.
  - For each stack, estimate chip count by comparing its height to a full stack at the same angle.
  - Partial stacks: count proportionally (e.g. half-height ≈ 10 chips, quarter-height ≈ 5 chips).
  - Chips lying flat or scattered: count each one individually.
  - Only count chips you can clearly see — ignore chips fully hidden behind others.
  - When in doubt on a partial stack, round DOWN to avoid overestimating.

STEP 3 — CALCULATE:
  - For each chip type: denomination × count = subtotal
  - Sum all subtotals for the grand total.

RESPOND in exactly this format — no extra text before or after:
[Color] chips ($[denom] ea): [count] chips = $[subtotal]
[Color] chips ($[denom] ea): [count] chips = $[subtotal]
TOTAL: $[grand total]

Example:
Black chips (${'$'}100 ea): 40 chips = ${'$'}4,000
Red chips (${'$'}5 ea): 20 chips = ${'$'}100
TOTAL: ${'$'}4,100
""".trimIndent()

        val systemMessage = Message(role = "system", content = listOf(Content(type = "text", text = systemPrompt)))
        val userContent = listOf(
            Content(type = "text", text = userPrompt),
            Content(
                type = "image_url",
                imageUrl = ImageUrl(
                    url = "data:image/jpeg;base64,$base64Image",
                    detail = "high"
                )
            )
        )
        val userMessage = Message(role = "user", content = userContent)

        return OpenAiRequest(
            model = "gpt-4o",
            messages = listOf(systemMessage, userMessage),
            maxTokens = 600
        )
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
    val url: String,
    val detail: String = "high"
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