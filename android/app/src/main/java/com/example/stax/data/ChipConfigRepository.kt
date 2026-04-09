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

    fun resetToDefaults(casinoName: String, gameType: String) {
        saveChipConfigs(casinoName, gameType, defaultsFor(gameType))
    }

    fun loadChipConfigs(casinoName: String, gameType: String): List<ChipConfig> {
        val key = getKey(casinoName, gameType)
        val jsonString = sharedPreferences.getString(key, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString<List<ChipConfig>>(jsonString)
            } catch (e: Exception) {
                defaultsFor(gameType)
            }
        } else {
            defaultsFor(gameType)
        }
    }

    private fun defaultsFor(gameType: String): List<ChipConfig> =
        if (gameType == "Tourney") getDefaultTourneyChipConfigs() else getDefaultCashChipConfigs()

    // Standard cash game chip denominations
    private fun getDefaultCashChipConfigs(): List<ChipConfig> {
        return listOf(
            ChipConfig(id = 1,  color = Color(0xFFF5F5F5), value = "1",      colorName = "white"),
            ChipConfig(id = 2,  color = Color(0xFFFF69B4), value = "2",      colorName = "pink"),
            ChipConfig(id = 3,  color = Color(0xFF008080), value = "3",      colorName = "teal"),
            ChipConfig(id = 4,  color = Color(0xFFFF7F7F), value = "4",      colorName = "coral"),
            ChipConfig(id = 5,  color = Color(0xFFCC2200), value = "5",      colorName = "red"),
            ChipConfig(id = 6,  color = Color(0xFF4169E1), value = "10",     colorName = "royal blue"),
            ChipConfig(id = 7,  color = Color(0xFF32CD32), value = "20",     colorName = "lime"),
            ChipConfig(id = 8,  color = Color(0xFF228B22), value = "25",     colorName = "green"),
            ChipConfig(id = 9,  color = Color(0xFF1E90FF), value = "50",     colorName = "blue"),
            ChipConfig(id = 10, color = Color(0xFF1A1A1A), value = "100",    colorName = "black"),
            ChipConfig(id = 11, color = Color(0xFF800080), value = "500",    colorName = "purple"),
            ChipConfig(id = 12, color = Color(0xFFFFD700), value = "1000",   colorName = "yellow"),
            ChipConfig(id = 13, color = Color(0xFFFFA500), value = "5000",   colorName = "orange"),
            ChipConfig(id = 14, color = Color(0xFF006400), value = "25000",  colorName = "dark green"),
            ChipConfig(id = 15, color = Color(0xFF800000), value = "100000", colorName = "burgundy")
        )
    }

    // Standard tournament chip denominations (no dollar value — counts are all that matter)
    private fun getDefaultTourneyChipConfigs(): List<ChipConfig> {
        return listOf(
            ChipConfig(id = 1, color = Color(0xFF228B22), value = "25",      colorName = "green"),
            ChipConfig(id = 2, color = Color(0xFF1A1A1A), value = "100",     colorName = "black"),
            ChipConfig(id = 3, color = Color(0xFF800080), value = "500",     colorName = "purple"),
            ChipConfig(id = 4, color = Color(0xFFFFD700), value = "1000",    colorName = "yellow"),
            ChipConfig(id = 5, color = Color(0xFFFFA500), value = "5000",    colorName = "orange"),
            ChipConfig(id = 6, color = Color(0xFF800000), value = "25000",   colorName = "burgundy"),
            ChipConfig(id = 7, color = Color(0xFF9E9E9E), value = "100000",  colorName = "grey"),
            ChipConfig(id = 8, color = Color(0xFF0D2B6E), value = "1000000", colorName = "dark blue")
        )
    }
} 