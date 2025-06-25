package com.example.stax.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@Serializable
data class ChipConfig(
    val id: Int,
    @Serializable(with = ColorSerializer::class)
    var color: Color,
    var value: String,
    val colorName: String
)

object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: Color) = encoder.encodeInt(value.toArgb())
    override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeInt())
}

class ChipConfigRepository(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("ChipConfigs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private fun getKey(casinoName: String, gameType: String) = "config_${casinoName}_$gameType"

    fun saveChipConfigs(casinoName: String, gameType: String, configs: List<ChipConfig>) {
        val key = getKey(casinoName, gameType)
        val jsonString = json.encodeToString(configs)
        sharedPreferences.edit().putString(key, jsonString).apply()
    }

    fun loadChipConfigs(casinoName: String, gameType: String): List<ChipConfig> {
        val key = getKey(casinoName, gameType)
        val jsonString = sharedPreferences.getString(key, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString<List<ChipConfig>>(jsonString)
            } catch (e: Exception) {
                getDefaultChipConfigs()
            }
        } else {
            getDefaultChipConfigs()
        }
    }

    private fun getDefaultChipConfigs(): List<ChipConfig> {
        return listOf(
            ChipConfig(id = 1, color = Color.White, value = "1", colorName = "white"),
            ChipConfig(id = 2, color = Color.Red, value = "5", colorName = "red"),
            ChipConfig(id = 3, color = Color.Green, value = "25", colorName = "green"),
            ChipConfig(id = 4, color = Color.Blue, value = "50", colorName = "blue"),
            ChipConfig(id = 5, color = Color.Black, value = "100", colorName = "black"),
            ChipConfig(id = 6, color = Color(0xFF800080), value = "500", colorName = "purple"),
            ChipConfig(id = 7, color = Color.Yellow, value = "1000", colorName = "yellow"),
            ChipConfig(id = 8, color = Color(0xFFFFA500), value = "5000", colorName = "orange"),
            ChipConfig(id = 9, color = Color(0xFF006400), value = "25000", colorName = "dark green"),
            ChipConfig(id = 10, color = Color(0xFF800000), value = "100000", colorName = "burgundy"),
            ChipConfig(id = 11, color = Color(0xFFFFD700), value = "500000", colorName = "gold")
        )
    }
} 