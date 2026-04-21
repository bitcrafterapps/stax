package com.bitcraftapps.stax.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitcraftapps.stax.data.Hand
import com.bitcraftapps.stax.data.VillainCards

private val RANKS     = listOf("A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2")
private val SUITS     = listOf("♠", "♥", "♦", "♣")
private val POSITIONS = listOf("BTN", "CO", "HJ", "LJ", "BB", "SB", "UTG", "EP")
private val RESULTS   = listOf("Won", "Lost", "Folded")

private val HeroGreen    = Color(0xFF2E7D32)
private val HeroGreenBdr = Color(0xFF66BB6A)
private val VillainBlue  = Color(0xFF1565C0)
private val VillainBlueBdr = Color(0xFF42A5F5)

private fun suitColor(suit: String): Color = when (suit) {
    "♥", "♦" -> Color(0xFFEF5350)
    else      -> Color(0xFF212121)
}

private fun resultColor(result: String): Color = when (result) {
    "Won"    -> Color(0xFF4CAF50)
    "Lost"   -> Color(0xFFEF5350)
    "Folded" -> Color(0xFF9E9E9E)
    else     -> Color(0xFF9E9E9E)
}

private fun parseVillainsFromJson(json: String): List<VillainCards> = try {
    val type = object : com.google.gson.reflect.TypeToken<List<VillainCards>>() {}.type
    com.google.gson.Gson().fromJson<List<VillainCards>>(json, type) ?: emptyList()
} catch (_: Exception) { emptyList() }

// ─────────────────────────────────────────────────────────────────────────────
// Public section
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandHistorySection(
    hands: List<Hand>,
    onAddHand: (hc1Rank: String, hc1Suit: String, hc2Rank: String, hc2Suit: String,
                position: String, result: String, notes: String,
                villains: List<VillainCards>) -> Unit,
    onToggleStar: (Hand) -> Unit,
    onDeleteHand: (Hand) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Hand History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = { showSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Hand")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (hands.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hands recorded yet.\nTap \"Add Hand\" to start tracking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                hands.forEach { hand ->
                    HandItem(
                        hand = hand,
                        villains = parseVillainsFromJson(hand.villainsJson),
                        onToggleStar = { onToggleStar(hand) },
                        onDelete = { onDeleteHand(hand) }
                    )
                }
            }
        }
    }

    if (showSheet) {
        AddHandSheet(
            onDismiss = { showSheet = false },
            onSave = { r1, s1, r2, s2, pos, result, notes, villains ->
                onAddHand(r1, s1, r2, s2, pos, result, notes, villains)
                showSheet = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hand list item
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HandItem(
    hand: Hand,
    villains: List<VillainCards>,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hero hole cards
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (hand.holeCard1Rank.isNotBlank())
                        PlayingCardChip(hand.holeCard1Rank, hand.holeCard1Suit)
                    if (hand.holeCard2Rank.isNotBlank())
                        PlayingCardChip(hand.holeCard2Rank, hand.holeCard2Suit)
                }

                // Villain cards with "vs" dividers
                if (villains.isNotEmpty()) {
                    villains.forEach { v ->
                        if (v.card1Rank.isNotBlank() || v.card2Rank.isNotBlank()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "vs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                if (v.card1Rank.isNotBlank())
                                    PlayingCardChip(v.card1Rank, v.card1Suit, isVillain = true)
                                if (v.card2Rank.isNotBlank())
                                    PlayingCardChip(v.card2Rank, v.card2Suit, isVillain = true)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Position + result badges
                if (hand.position.isNotBlank()) {
                    Badge(hand.position, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (hand.result.isNotBlank()) {
                    Badge(hand.result, resultColor(hand.result).copy(alpha = 0.15f), resultColor(hand.result))
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onToggleStar, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (hand.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Star",
                        tint = if (hand.isStarred) Color(0xFFFFB300)
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp))
                }
            }

            if (hand.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(hand.notes, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }

            if (hand.timestamp.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(hand.timestamp, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete Hand?") },
            text = { Text("This hand will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); confirmDelete = false }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun Badge(label: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold, color = textColor)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Playing card chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PlayingCardChip(
    rank: String, suit: String,
    size: Dp = 36.dp,
    selected: Boolean = false,
    isVillain: Boolean = false
) {
    val isRed    = suit == "♥" || suit == "♦"
    val bgColor  = when { selected && isVillain -> VillainBlue; selected -> HeroGreen; else -> Color.White }
    val bdrColor = when { selected && isVillain -> VillainBlueBdr; selected -> HeroGreenBdr; else -> Color(0xFFBDBDBD) }
    val textCol  = if (selected) Color.White else if (isRed) Color(0xFFD32F2F) else Color(0xFF1A1A1A)

    Box(
        modifier = Modifier
            .size(width = size, height = (size.value * 1.35f).dp)
            .background(bgColor, RoundedCornerShape(5.dp))
            .border(if (selected) 2.dp else 0.5.dp, bdrColor, RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(rank, fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.33f).sp, color = textCol,
                lineHeight = (size.value * 0.35f).sp)
            Text(suit, fontSize = (size.value * 0.28f).sp, color = textCol,
                lineHeight = (size.value * 0.30f).sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Hand bottom sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddHandSheet(
    onDismiss: () -> Unit,
    onSave: (r1: String, s1: String, r2: String, s2: String,
             pos: String, result: String, notes: String,
             villains: List<VillainCards>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var heroSelected  by remember { mutableStateOf(listOf<String>()) }  // "Rank|Suit"
    // Each villain is a list of up to 2 "Rank|Suit" strings
    var villains      by remember { mutableStateOf(listOf<List<String>>()) }
    var position      by remember { mutableStateOf("") }
    var result        by remember { mutableStateOf("") }
    var notes         by remember { mutableStateOf("") }

    fun toggleHero(rank: String, suit: String) {
        val key = "$rank|$suit"
        heroSelected = if (key in heroSelected) heroSelected - key
                       else if (heroSelected.size < 2) heroSelected + key
                       else heroSelected
    }

    fun toggleVillain(idx: Int, rank: String, suit: String) {
        val key = "$rank|$suit"
        val current = villains.getOrElse(idx) { emptyList() }
        val updated = if (key in current) current - key
                      else if (current.size < 2) current + key
                      else current
        villains = villains.toMutableList().also { it[idx] = updated }
    }

    fun heroCard(idx: Int, field: String): String {
        val parts = heroSelected.getOrNull(idx)?.split("|") ?: return ""
        return if (field == "rank") parts[0] else parts[1]
    }

    fun buildVillains(): List<VillainCards> = villains.map { cards ->
        val c1 = cards.getOrNull(0)?.split("|")
        val c2 = cards.getOrNull(1)?.split("|")
        VillainCards(
            card1Rank = c1?.getOrNull(0) ?: "",
            card1Suit = c1?.getOrNull(1) ?: "",
            card2Rank = c2?.getOrNull(0) ?: "",
            card2Suit = c2?.getOrNull(1) ?: ""
        )
    }

    fun blockedCardsForVillain(idx: Int): Set<String> =
        (heroSelected + villains.filterIndexed { index, _ -> index != idx }.flatten()).toSet()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Record Hand", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // ── Hero cards ────────────────────────────────────────────────
            SectionHeader("Your Hole Cards")
            CardPreviewRow(
                selected = heroSelected,
                isVillain = false,
                onClear = { heroSelected = emptyList() }
            )
            CardPickerGrid(selected = heroSelected, isVillain = false, onToggle = ::toggleHero)

            // ── Villain cards ─────────────────────────────────────────────
            SectionHeader("Opponents (optional)")
            villains.forEachIndexed { idx, vilCards ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VillainBlue.copy(alpha = 0.07f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Villain ${idx + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = VillainBlueBdr
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            CardPreviewRowInline(selected = vilCards, isVillain = true)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    villains = villains.toMutableList().also { it.removeAt(idx) }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove villain",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                        CardPickerGrid(
                            selected = vilCards,
                            blockedKeys = blockedCardsForVillain(idx),
                            isVillain = true,
                            onToggle = { r, s -> toggleVillain(idx, r, s) }
                        )
                    }
                }
            }
            if (villains.size < 4) {
                TextButton(
                    onClick = { villains = villains + listOf(emptyList()) },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Opponent")
                }
            }

            // ── Result ────────────────────────────────────────────────────
            SectionHeader("Result")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RESULTS.forEach { r ->
                    val isSel = result == r
                    FilterChip(
                        selected = isSel,
                        onClick = { result = if (isSel) "" else r },
                        label = { Text(r, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = resultColor(r).copy(alpha = 0.2f),
                            selectedLabelColor = resultColor(r)
                        )
                    )
                }
            }

            // ── Position ──────────────────────────────────────────────────
            SectionHeader("Position (optional)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                POSITIONS.forEach { p ->
                    val isSel = position == p
                    FilterChip(selected = isSel, onClick = { position = if (isSel) "" else p },
                        label = { Text(p) })
                }
            }

            // ── Notes ─────────────────────────────────────────────────────
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("e.g. flopped top set, villain shoved…") },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            // ── Save ──────────────────────────────────────────────────────
            Button(
                onClick = {
                    onSave(
                        heroCard(0, "rank"), heroCard(0, "suit"),
                        heroCard(1, "rank"), heroCard(1, "suit"),
                        position, result, notes, buildVillains()
                    )
                },
                enabled = heroSelected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save Hand", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun CardPreviewRow(selected: List<String>, isVillain: Boolean, onClear: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (selected.isEmpty()) {
            Text("Select up to 2 cards below", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            selected.forEach { key ->
                val (r, s) = key.split("|")
                PlayingCardChip(r, s, size = 48.dp, selected = true, isVillain = isVillain)
            }
            TextButton(onClick = onClear) { Text("Clear") }
        }
    }
}

@Composable
private fun CardPreviewRowInline(selected: List<String>, isVillain: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (selected.isEmpty()) {
            Text("No cards", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        } else {
            selected.forEach { key ->
                val (r, s) = key.split("|")
                PlayingCardChip(r, s, size = 30.dp, selected = true, isVillain = isVillain)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card picker grid
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CardPickerGrid(
    selected: List<String>,
    blockedKeys: Set<String> = emptySet(),
    isVillain: Boolean,
    onToggle: (String, String) -> Unit
) {
    val selBg  = if (isVillain) VillainBlue else HeroGreen
    val selBdr = if (isVillain) VillainBlueBdr else HeroGreenBdr

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SUITS.forEach { suit ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(suit, fontSize = 14.sp, color = suitColor(suit),
                    fontWeight = FontWeight.Black, modifier = Modifier.width(18.dp))
                RANKS.forEach { rank ->
                    val key        = "$rank|$suit"
                    val isSel      = key in selected
                    val isBlocked  = key in blockedKeys && !isSel
                    val isDisabled = isBlocked || (selected.size >= 2 && !isSel)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when { isSel -> selBg; isDisabled -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.3f); else -> Color.White.copy(alpha = 0.9f) }
                            )
                            .border(0.5.dp, if (isSel) selBdr else Color(0xFFBDBDBD), RoundedCornerShape(4.dp))
                            .clickable(enabled = !isDisabled) { onToggle(rank, suit) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(rank, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = when { isSel -> Color.White; isDisabled -> Color(0xFF9E9E9E); else -> suitColor(suit) })
                    }
                }
            }
        }
    }
}
