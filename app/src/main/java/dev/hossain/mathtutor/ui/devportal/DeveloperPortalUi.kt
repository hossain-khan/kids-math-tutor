package dev.hossain.mathtutor.ui.devportal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@CircuitInject(DeveloperPortalScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperPortalUi(
    state: DeveloperPortalScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Developer Portal") })
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .fillMaxWidth(),
        ) {
            // Data Operations
            if (state.showDataOpsSection) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Data Operations", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { state.eventSink(DeveloperPortalScreen.Event.ClearAppDataClicked) }) {
                            Text("Clear App Data")
                        }

                        // Show result message if present
                        state.clearResultMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = msg)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seed / Simulators
            if (state.showSeedSection) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Seed & Simulators", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { state.eventSink(DeveloperPortalScreen.Event.SeedSessionsClicked) }) {
                            Text("Seed Sample Sessions")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { state.eventSink(DeveloperPortalScreen.Event.ForceBadgeCheckClicked) }) {
                            Text("Run Badge Checks")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Diagnostics / Misc
            if (state.showDiagnosticsSection) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Diagnostics", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { state.eventSink(DeveloperPortalScreen.Event.PlaySuccessSound) }) {
                            Text("Play Success Sound & Haptic")
                        }
                    }
                }
            }
        }

        // Clear confirmation dialog
        if (state.showClearConfirm) {
            var confirmText by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { state.eventSink(DeveloperPortalScreen.Event.CancelClear) },
                title = { Text("Confirm Clear App Data") },
                text = {
                    Column {
                        Text(
                            "This will delete local sessions, game data, and reset preferences.\n\n" +
                                "Type DELETE (all caps) in the field below to confirm.",
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextField(
                            value = confirmText,
                            onValueChange = { confirmText = it },
                            placeholder = { Text("Type DELETE to confirm") },
                        )
                        if (state.clearInProgress) {
                            Spacer(modifier = Modifier.height(12.dp))
                            CircularProgressIndicator()
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            state.eventSink(DeveloperPortalScreen.Event.ConfirmClear(confirmText))
                            confirmText = ""
                        },
                        enabled = confirmText == "DELETE" && !state.clearInProgress,
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { state.eventSink(DeveloperPortalScreen.Event.CancelClear) }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}
