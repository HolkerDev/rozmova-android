package eu.rozmova.app.modules.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectJobOnboarding(
    initialState: String?,
    onNext: (job: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedJob by remember { mutableStateOf(initialState ?: "") }
    var needsProfessionalWords by remember { mutableStateOf(initialState != null) }

    Scaffold(
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Content(
                selectedJob = selectedJob,
                onJobSelected = { job ->
                    selectedJob = job
                },
                paddingValues = paddingValues,
                needsProfessionalWords = needsProfessionalWords,
                onProfessionalWordsChanged = { needsProfessionalWords = it },
            )

            // Next button positioned at bottom right
            val isValid = !needsProfessionalWords || selectedJob.isNotEmpty()
            if (isValid) {
                FloatingActionButton(
                    onClick = {
                        onNext(if (needsProfessionalWords) selectedJob else null)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .size(56.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next",
                    )
                }
            } else {
                Spacer(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .size(56.dp),
                )
            }
        }
    }
}

@Composable
private fun Content(
    selectedJob: String,
    onJobSelected: (String) -> Unit,
    paddingValues: PaddingValues,
    needsProfessionalWords: Boolean,
    onProfessionalWordsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
    ) {
        // Header Section
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Work,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "What's your intended profession?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This helps us create relevant language scenarios for your future work",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Professional Words Choice
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Radio button options
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Option 1: I don't need professional words
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = !needsProfessionalWords,
                        onClick = {
                            onProfessionalWordsChanged(false)
                            onJobSelected("")
                        },
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "I don't need professional vocabulary",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Option 2: I want professional words
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = needsProfessionalWords,
                        onClick = { onProfessionalWordsChanged(true) },
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "I want professional vocabulary for my field",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Show profession input only if professional words are needed
            if (needsProfessionalWords) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = selectedJob,
                    onValueChange = onJobSelected,
                    label = {
                        Text("Your profession")
                    },
                    placeholder = {
                        Text("e.g., Software Engineer, Doctor, Teacher, Lawyer...")
                    },
                    trailingIcon = {
                        if (selectedJob.isNotEmpty()) {
                            IconButton(
                                onClick = { onJobSelected("") },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear text",
                                )
                            }
                        }
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                )
            }
        }

        // Bottom spacing
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        Spacer(modifier = Modifier.height(screenHeight * 0.3f))
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectJobOnboardingPreview() {
    MaterialTheme {
        SelectJobOnboarding(
            initialState = "Software Engineer",
            onNext = {},
        )
    }
}
