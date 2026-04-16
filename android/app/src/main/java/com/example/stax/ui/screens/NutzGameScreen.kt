package com.example.stax.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stax.ui.theme.StaxHeaderGradient
import com.example.stax.ui.theme.StaxLoss
import com.example.stax.ui.theme.StaxProfit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

private val nutzRanks = listOf(14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2)
private val nutzSuits = listOf("♠", "♥", "♦", "♣")
private val nutzStreetLabels = listOf("Flop", "Turn", "River")

private data class NutzCard(val rank: Int, val suit: String) {
    val id: String get() = "${rank}_$suit"
    val display: String get() = "${rankLabel(rank)}$suit"
}

private data class NutzHoleCombo(val cards: List<NutzCard>) {
    val canonicalCards: List<NutzCard> = cards.sortedWith(
        compareByDescending<NutzCard> { it.rank }.thenBy { nutzSuits.indexOf(it.suit) }
    )
    val key: String = canonicalCards.joinToString("|") { it.id }
    val label: String = canonicalCards.joinToString(" ") { it.display }
}

private data class HandValue(val category: Int, val tieBreakers: List<Int>) : Comparable<HandValue> {
    override fun compareTo(other: HandValue): Int {
        if (category != other.category) return category.compareTo(other.category)
        val max = maxOf(tieBreakers.size, other.tieBreakers.size)
        for (i in 0 until max) {
            val a = tieBreakers.getOrElse(i) { 0 }
            val b = other.tieBreakers.getOrElse(i) { 0 }
            if (a != b) return a.compareTo(b)
        }
        return 0
    }
}

private data class NutzStageSolution(
    val nuts: List<NutzHoleCombo>,
    val secondNuts: List<NutzHoleCombo>,
    val bestHandName: String
)

private data class NutzHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val playedAt: Long = System.currentTimeMillis(),
    val score: Int,
    val correctAnswers: Int,
    val totalAnswers: Int,
    val boardPreview: String
)

private enum class GuessMode(val label: String) {
    FIRST_ONLY("1st Nuts"),
    FIRST_AND_SECOND("1st + 2nd")
}

private class NutzGameRepository private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("stax_nutz_game", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _history = MutableStateFlow(load())
    val history: StateFlow<List<NutzHistoryEntry>> = _history.asStateFlow()

    fun addEntry(entry: NutzHistoryEntry) {
        _history.value = (listOf(entry) + _history.value).sortedByDescending { it.playedAt }
        prefs.edit().putString("history", gson.toJson(_history.value)).apply()
    }

    private fun load(): List<NutzHistoryEntry> = try {
        val json = prefs.getString("history", null) ?: return emptyList()
        val type = object : TypeToken<List<NutzHistoryEntry>>() {}.type
        gson.fromJson<List<NutzHistoryEntry>>(json, type).orEmpty()
    } catch (_: Exception) {
        emptyList()
    }

    companion object {
        @Volatile private var INSTANCE: NutzGameRepository? = null
        fun getInstance(context: Context): NutzGameRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NutzGameRepository(context).also { INSTANCE = it }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NutzGameScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { NutzGameRepository.getInstance(context) }
    val history by repo.history.collectAsState()

    var roundBoard by remember { mutableStateOf(generateNutzRoundBoard()) }
    var stageIndex by remember { mutableIntStateOf(0) }
    var nutGuess by remember { mutableStateOf<List<NutzCard>>(emptyList()) }
    var secondGuess by remember { mutableStateOf<List<NutzCard>>(emptyList()) }
    var totalScore by remember { mutableIntStateOf(0) }
    var correctAnswers by remember { mutableIntStateOf(0) }
    var totalAnswers by remember { mutableIntStateOf(0) }
    var stageFeedback by remember { mutableStateOf<String?>(null) }
    var stageAnswerSummary by remember { mutableStateOf<List<String>>(emptyList()) }
    var roundComplete by remember { mutableStateOf(false) }
    var activityTab by remember { mutableIntStateOf(0) }
    var guessMode by remember { mutableStateOf(GuessMode.FIRST_AND_SECOND) }
    var guessTab by remember { mutableIntStateOf(0) }

    val board = remember(roundBoard, stageIndex) { roundBoard.take(stageIndex + 3) }
    val stageSolution = remember(roundBoard, stageIndex) { solveStage(board) }
    val secondAvailable = stageSolution.secondNuts.isNotEmpty()
    val secondRequired = guessMode == GuessMode.FIRST_AND_SECOND && secondAvailable
    val boardIds = board.map { it.id }.toSet()
    val nutGuessIds = nutGuess.map { it.id }.toSet()
    val secondGuessIds = secondGuess.map { it.id }.toSet()

    fun resetRound() {
        roundBoard = generateNutzRoundBoard()
        stageIndex = 0
        nutGuess = emptyList()
        secondGuess = emptyList()
        totalScore = 0
        correctAnswers = 0
        totalAnswers = 0
        stageFeedback = null
        stageAnswerSummary = emptyList()
        roundComplete = false
        guessTab = 0
    }

    fun submitStage() {
        val nutCombo = nutGuess.toComboOrNull()
        val secondCombo = secondGuess.toComboOrNull()
        if (nutCombo == null || (secondRequired && secondCombo == null)) return

        val nutCorrect = nutCombo.key in stageSolution.nuts.map { it.key }.toSet()
        val secondCorrect = if (!secondRequired) true else secondCombo!!.key in stageSolution.secondNuts.map { it.key }.toSet()
        val stagePoints = (if (nutCorrect) 10 else -5) + if (!secondRequired) 0 else (if (secondCorrect) 8 else -4)
        totalScore += stagePoints
        correctAnswers += listOf(nutCorrect, secondCorrect).count { it }
        totalAnswers += if (secondRequired) 2 else 1

        val summaries = buildList {
            add("Best made hand: ${stageSolution.bestHandName}")
            add("Nuts: ${formatAnswerCombos(stageSolution.nuts)}")
            if (secondRequired) add("Second nuts: ${formatAnswerCombos(stageSolution.secondNuts)}")
        }
        stageAnswerSummary = summaries
        stageFeedback = "Street score ${if (stagePoints >= 0) "+" else ""}$stagePoints · Nuts ${if (nutCorrect) "correct" else "wrong"}" +
            if (secondRequired) " · 2nd nuts ${if (secondCorrect) "correct" else "wrong"}" else ""

        if (stageIndex == 2) {
            roundComplete = true
            repo.addEntry(
                NutzHistoryEntry(
                    score = totalScore,
                    correctAnswers = correctAnswers,
                    totalAnswers = totalAnswers,
                    boardPreview = roundBoard.joinToString(" ") { it.display }
                )
            )
        }
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(StaxHeaderGradient)) {
                TopAppBar(
                    title = { Text("Nutz Game", color = Color.White, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { resetRound() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "New Round", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SummaryHeaderCard(
                    score = totalScore,
                    correctAnswers = correctAnswers,
                    totalAnswers = totalAnswers,
                    guessMode = guessMode,
                    onGuessModeChange = {
                        if (guessMode != it) {
                            guessMode = it
                            resetRound()
                        }
                    }
                )
            }
            item {
                BoardCard(
                    board = board,
                    stageLabel = nutzStreetLabels[stageIndex]
                )
            }
            item {
                GuessTabsSection(
                    selectedTab = guessTab,
                    secondRequired = secondRequired,
                    secondAvailable = secondAvailable,
                    nutGuess = nutGuess,
                    secondGuess = secondGuess,
                    nutBlockedIds = boardIds + secondGuessIds,
                    secondBlockedIds = boardIds + nutGuessIds,
                    onTabSelected = { guessTab = it },
                    onToggleNut = { card ->
                        val updated = nutGuess.toggleSelectable(card, blockedIds = boardIds + secondGuessIds)
                        nutGuess = updated
                        if (secondRequired && guessTab == 0 && updated.size == 2) {
                            guessTab = 1
                        }
                    },
                    onToggleSecond = { card ->
                        secondGuess = secondGuess.toggleSelectable(card, blockedIds = boardIds + nutGuessIds)
                    }
                )
            }
            item {
                Button(
                    onClick = { submitStage() },
                    enabled = stageFeedback == null && nutGuess.size == 2 && (!secondRequired || secondGuess.size == 2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (stageIndex == 2) "Score River" else "Score ${nutzStreetLabels[stageIndex]}")
                }
            }
            if (stageFeedback != null) {
                item {
                    val nextStreetLabel = nutzStreetLabels.getOrNull(stageIndex + 1) ?: "Next Street"
                    FeedbackCard(
                        feedback = stageFeedback.orEmpty(),
                        answerSummary = stageAnswerSummary,
                        onContinue = {
                            if (roundComplete) {
                                resetRound()
                            } else {
                                stageIndex += 1
                                nutGuess = emptyList()
                                secondGuess = emptyList()
                                stageFeedback = null
                                stageAnswerSummary = emptyList()
                                guessTab = 0
                            }
                        },
                        continueLabel = if (roundComplete) "Play New Round" else "Go To $nextStreetLabel"
                    )
                }
            }
            item {
                ActivitySection(
                    history = history,
                    selectedTab = activityTab,
                    onTabSelected = { activityTab = it }
                )
            }
        }
    }
}

@Composable
private fun SummaryHeaderCard(
    score: Int,
    correctAnswers: Int,
    totalAnswers: Int,
    guessMode: GuessMode,
    onGuessModeChange: (GuessMode) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.84f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactMetricPill(
                    label = "Score",
                    value = score.toString(),
                    modifier = Modifier.weight(1f),
                    valueColor = if (score >= 0) StaxProfit else StaxLoss
                )
                CompactMetricPill(
                    label = "Correct",
                    value = "$correctAnswers/$totalAnswers",
                    modifier = Modifier.weight(1f)
                )
            }
            GuessModeSelector(selectedMode = guessMode, onGuessModeChange = onGuessModeChange)
        }
    }
}

@Composable
private fun BoardCard(board: List<NutzCard>, stageLabel: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.82f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Board", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = stageLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                board.forEach { card ->
                    NutzCardView(card = card, selected = true, enabled = true)
                }
            }
        }
    }
}

@Composable
private fun GuessTabsSection(
    selectedTab: Int,
    secondRequired: Boolean,
    secondAvailable: Boolean,
    nutGuess: List<NutzCard>,
    secondGuess: List<NutzCard>,
    nutBlockedIds: Set<String>,
    secondBlockedIds: Set<String>,
    onTabSelected: (Int) -> Unit,
    onToggleNut: (NutzCard) -> Unit,
    onToggleSecond: (NutzCard) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (secondRequired) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = Color.White
                ) {
                    Tab(selected = selectedTab == 0, onClick = { onTabSelected(0) }, text = { Text("1st Nuts") })
                    Tab(selected = selectedTab == 1, onClick = { onTabSelected(1) }, text = { Text("2nd Nuts") })
                }
                Spacer(Modifier.height(10.dp))
            } else {
                Text(
                    if (secondAvailable) "Guess the 1st nuts" else "Guess the nuts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
            }

            if (!secondRequired && !secondAvailable) {
                Text(
                    "This street only has one distinct top answer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            if (secondRequired && selectedTab == 1) {
                GuessPickerContent(
                    title = "Select the 2nd nuts",
                    selected = secondGuess,
                    blockedIds = secondBlockedIds,
                    onToggle = onToggleSecond
                )
            } else {
                GuessPickerContent(
                    title = if (secondRequired) "Select the 1st nuts" else "Select 2 hole cards",
                    selected = nutGuess,
                    blockedIds = nutBlockedIds,
                    onToggle = onToggleNut
                )
            }
        }
    }
}

@Composable
private fun GuessPickerContent(
    title: String,
    selected: List<NutzCard>,
    blockedIds: Set<String>,
    onToggle: (NutzCard) -> Unit
) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    if (selected.isEmpty()) {
        Text("Select 2 hole cards", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            selected.forEach { NutzCardView(card = it, selected = true, enabled = true) }
        }
    }
    Spacer(Modifier.height(10.dp))
    Text(
        "Swipe left to see lower ranks",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        nutzSuits.forEach { suit ->
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                nutzRanks.forEach { rank ->
                    val card = NutzCard(rank, suit)
                    val selectedNow = selected.any { it.id == card.id }
                    val enabled = selectedNow || !blockedIds.contains(card.id)
                    NutzCardView(
                        card = card,
                        selected = selectedNow,
                        enabled = enabled,
                        onClick = { onToggle(card) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NutzCardView(
    card: NutzCard,
    selected: Boolean,
    enabled: Boolean,
    onClick: (() -> Unit)? = null
) {
    val bg = when {
        selected -> MaterialTheme.colorScheme.primary
        !enabled -> MaterialTheme.colorScheme.surfaceContainer
        else -> Color.White
    }
    val border = when {
        selected -> MaterialTheme.colorScheme.primary
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        else -> Color.White.copy(alpha = 0.65f)
    }
    val fg = if (selected) Color.White else if (card.suit in listOf("♥", "♦")) Color(0xFFCC3B3B) else Color(0xFF1F1F1F)

    Box(
        modifier = Modifier
            .size(width = 32.dp, height = 42.dp)
            .background(bg, RoundedCornerShape(6.dp))
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(card.display, color = if (enabled || selected) fg else fg.copy(alpha = 0.32f), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FeedbackCard(
    feedback: String,
    answerSummary: List<String>,
    onContinue: () -> Unit,
    continueLabel: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(feedback, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            answerSummary.forEach {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
            }
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text(continueLabel)
            }
        }
    }
}

@Composable
private fun ActivitySection(
    history: List<NutzHistoryEntry>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val topRounds = history.sortedByDescending { it.score }.take(5)
    val recentRounds = history.take(8)
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = Color.White
            ) {
                listOf("Leaderboard", "Rounds").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            if (selectedTab == 0) {
                if (topRounds.isEmpty()) {
                    Text("Play a few rounds to build your leaderboard.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    topRounds.forEachIndexed { index, entry ->
                        CompactHistoryRow(
                            title = "#${index + 1}  ${formatHistoryDate(entry.playedAt)}",
                            subtitle = "${entry.correctAnswers}/${entry.totalAnswers} correct",
                            value = "${entry.score} pts",
                            valueColor = if (entry.score >= 0) StaxProfit else StaxLoss
                        )
                    }
                }
            } else {
                if (recentRounds.isEmpty()) {
                    Text("Your round history will show up here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    recentRounds.forEach { entry ->
                        CompactHistoryRow(
                            title = formatHistoryDate(entry.playedAt),
                            subtitle = entry.boardPreview,
                            value = "${entry.score} pts",
                            valueColor = if (entry.score >= 0) StaxProfit else StaxLoss,
                            trailingNote = "${entry.correctAnswers}/${entry.totalAnswers}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun GuessModeSelector(selectedMode: GuessMode, onGuessModeChange: (GuessMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        GuessMode.entries.forEach { mode ->
            Surface(
                modifier = Modifier.weight(1f).clickable { onGuessModeChange(mode) },
                shape = RoundedCornerShape(12.dp),
                color = if (selectedMode == mode) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
                }
            ) {
                Text(
                    text = mode.label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selectedMode == mode) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CompactHistoryRow(
    title: String,
    subtitle: String,
    value: String,
    valueColor: Color,
    trailingNote: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(value, color = valueColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                if (trailingNote != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(trailingNote, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

private fun List<NutzCard>.toggleSelectable(card: NutzCard, blockedIds: Set<String>): List<NutzCard> {
    return when {
        any { it.id == card.id } -> filterNot { it.id == card.id }
        blockedIds.contains(card.id) -> this
        size >= 2 -> this
        else -> (this + card).sortedByDescending { it.rank }
    }
}

private fun List<NutzCard>.toComboOrNull(): NutzHoleCombo? = if (size == 2) NutzHoleCombo(this) else null

private fun generateNutzRoundBoard(): List<NutzCard> =
    fullDeck().shuffled(Random(System.currentTimeMillis())).take(5)

private fun fullDeck(): List<NutzCard> =
    nutzSuits.flatMap { suit -> nutzRanks.map { rank -> NutzCard(rank, suit) } }

private fun solveStage(board: List<NutzCard>): NutzStageSolution {
    val remaining = fullDeck().filterNot { deckCard -> board.any { it.id == deckCard.id } }
    val evaluations = mutableListOf<Pair<NutzHoleCombo, HandValue>>()

    for (i in 0 until remaining.size) {
        for (j in i + 1 until remaining.size) {
            val combo = NutzHoleCombo(listOf(remaining[i], remaining[j]))
            evaluations += combo to bestHandValue(board + combo.cards)
        }
    }

    val distinctValues = evaluations.map { it.second }.distinct().sortedDescending()
    val topValue = distinctValues.firstOrNull()
        ?: return NutzStageSolution(nuts = emptyList(), secondNuts = emptyList(), bestHandName = "N/A")
    val secondValue = distinctValues.getOrNull(1)

    return NutzStageSolution(
        nuts = evaluations.filter { it.second == topValue }.map { it.first },
        secondNuts = evaluations.filter { secondValue != null && it.second == secondValue }.map { it.first },
        bestHandName = handCategoryName(topValue.category)
    )
}

private fun bestHandValue(cards: List<NutzCard>): HandValue {
    var best: HandValue? = null
    for (a in 0 until cards.size - 4) {
        for (b in a + 1 until cards.size - 3) {
            for (c in b + 1 until cards.size - 2) {
                for (d in c + 1 until cards.size - 1) {
                    for (e in d + 1 until cards.size) {
                        val value = evaluateFiveCardHand(listOf(cards[a], cards[b], cards[c], cards[d], cards[e]))
                        if (best == null || value > best!!) best = value
                    }
                }
            }
        }
    }
    return best!!
}

private fun evaluateFiveCardHand(cards: List<NutzCard>): HandValue {
    val ranksDesc = cards.map { it.rank }.sortedDescending()
    val counts = cards.groupingBy { it.rank }.eachCount()
    val countGroups = counts.entries.sortedWith(compareByDescending<Map.Entry<Int, Int>> { it.value }.thenByDescending { it.key })
    val isFlush = cards.map { it.suit }.distinct().size == 1
    val straightHigh = straightHigh(cards.map { it.rank })

    if (isFlush && straightHigh != null) return HandValue(8, listOf(straightHigh))

    if (countGroups.first().value == 4) {
        val quad = countGroups.first().key
        val kicker = countGroups.first { it.value == 1 }.key
        return HandValue(7, listOf(quad, kicker))
    }

    if (countGroups.first().value == 3 && countGroups.getOrNull(1)?.value == 2) {
        return HandValue(6, listOf(countGroups[0].key, countGroups[1].key))
    }

    if (isFlush) return HandValue(5, ranksDesc)
    if (straightHigh != null) return HandValue(4, listOf(straightHigh))

    if (countGroups.first().value == 3) {
        val trip = countGroups.first().key
        val kickers = countGroups.filter { it.value == 1 }.map { it.key }.sortedDescending()
        return HandValue(3, listOf(trip) + kickers)
    }

    if (countGroups.first().value == 2 && countGroups.getOrNull(1)?.value == 2) {
        val pairs = countGroups.filter { it.value == 2 }.map { it.key }.sortedDescending()
        val kicker = countGroups.first { it.value == 1 }.key
        return HandValue(2, pairs + kicker)
    }

    if (countGroups.first().value == 2) {
        val pair = countGroups.first().key
        val kickers = countGroups.filter { it.value == 1 }.map { it.key }.sortedDescending()
        return HandValue(1, listOf(pair) + kickers)
    }

    return HandValue(0, ranksDesc)
}

private fun straightHigh(ranks: List<Int>): Int? {
    val distinct = ranks.distinct().sortedDescending().toMutableList()
    if (14 in distinct) distinct += 1
    for (i in 0..distinct.size - 5) {
        val window = distinct.subList(i, i + 5)
        if (window.zipWithNext().all { (a, b) -> a - 1 == b }) return window.first()
    }
    return null
}

private fun handCategoryName(category: Int): String = when (category) {
    8 -> "Straight Flush"
    7 -> "Four of a Kind"
    6 -> "Full House"
    5 -> "Flush"
    4 -> "Straight"
    3 -> "Three of a Kind"
    2 -> "Two Pair"
    1 -> "One Pair"
    else -> "High Card"
}

private fun rankLabel(rank: Int): String = when (rank) {
    14 -> "A"
    13 -> "K"
    12 -> "Q"
    11 -> "J"
    10 -> "T"
    else -> rank.toString()
}

private fun formatAnswerCombos(combos: List<NutzHoleCombo>): String {
    if (combos.isEmpty()) return "No distinct second nuts"
    val visible = combos.take(4).joinToString(", ") { it.label }
    return if (combos.size > 4) "$visible +${combos.size - 4} more" else visible
}

private fun formatHistoryDate(timestamp: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(timestamp))
