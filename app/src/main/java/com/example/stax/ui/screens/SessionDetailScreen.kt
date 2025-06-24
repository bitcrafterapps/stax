package com.example.stax.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stax.data.AppDatabase
import com.example.stax.data.SessionDetailViewModel
import com.example.stax.data.SessionDetailViewModelFactory
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SessionDetailViewModel = viewModel(
        factory = SessionDetailViewModelFactory(AppDatabase.getDatabase(context).staxDao(), sessionId)
    )
    val session by viewModel.session.collectAsState()

    var name by remember(session) { mutableStateOf(session?.name ?: "") }
    var date by remember(session) { mutableStateOf(session?.date ?: "") }
    var type by remember(session) { mutableStateOf(session?.type ?: "") }
    var game by remember(session) { mutableStateOf(session?.game ?: "") }
    var buyInAmount by remember(session) { mutableStateOf(session?.buyInAmount?.toString() ?: "0.0") }
    var cashOutAmount by remember(session) { mutableStateOf(session?.cashOutAmount?.toString() ?: "0.0") }
    var notes by remember(session) { mutableStateOf(session?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.updateSession(
                    name = name,
                    date = date,
                    type = type,
                    game = game,
                    buyInAmount = buyInAmount.toDoubleOrNull() ?: 0.0,
                    cashOutAmount = cashOutAmount.toDoubleOrNull() ?: 0.0,
                    notes = notes
                )
                onNavigateBack()
            }) {
                Icon(Icons.Default.Save, contentDescription = "Save Session")
            }
        }
    ) { paddingValues ->
        if (session == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val profitLoss = (cashOutAmount.toDoubleOrNull() ?: 0.0) - (buyInAmount.toDoubleOrNull() ?: 0.0)
                val profitLossFormatted = NumberFormat.getCurrencyInstance().format(profitLoss)
                val profitColor = if (profitLoss >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

                Text(
                    "Profit/Loss: $profitLossFormatted",
                    style = MaterialTheme.typography.headlineSmall,
                    color = profitColor,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Session Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = game,
                    onValueChange = { game = it },
                    label = { Text("Game") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = buyInAmount,
                    onValueChange = { buyInAmount = it },
                    label = { Text("Buy-in Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cashOutAmount,
                    onValueChange = { cashOutAmount = it },
                    label = { Text("Cash-out Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Key Hands / Notes") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
        }
    }
} 