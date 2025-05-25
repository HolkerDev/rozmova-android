@file:OptIn(ExperimentalMaterial3Api::class)

package eu.rozmova.app.modules.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.rozmova.app.R

data class Language(
        val code: String,
        val name: Int,
        val nativeName: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectLanguageOnboarding(
        startLang: String,
        onLangSelect: (langCode: String) -> Unit,
        modifier: Modifier = Modifier,
) {
    var selectedLanguage by remember { mutableStateOf(startLang) }

    val languages =
            listOf(
                    Language("en", R.string.lang_en, "English"),
                    Language("uk", R.string.lang_uk, "Українська"),
                    Language("ru", R.string.lang_ru, "Русский"),
            )

    Scaffold(
            modifier = modifier,
    ) { paddingValues ->
        Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            // Header Section
            Column(
                    modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                        text = stringResource(R.string.onboarding_lang_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                        text = stringResource(R.string.onboarding_lang_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Language List
            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(languages) { language ->
                    LanguageListItem(
                            language = language,
                            isSelected = selectedLanguage == language.code,
                            onLangSelect = {
                                selectedLanguage = language.code
                                onLangSelect(language.code)
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageListItem(
        language: Language,
        isSelected: Boolean,
        onLangSelect: () -> Unit,
        modifier: Modifier = Modifier,
) {
    Surface(
            modifier =
                    modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .selectable(
                                    selected = isSelected,
                                    onClick = onLangSelect,
                                    role = Role.RadioButton,
                            ),
            color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            tonalElevation = if (isSelected) 3.dp else 0.dp,
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            // Language names
            Column(
                    modifier = Modifier.weight(1f),
            ) {
                Text(
                        text = stringResource(language.name),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                )

                Text(
                        text = language.nativeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                )
            }

            RadioButton(
                    selected = isSelected,
                    onClick = null,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectLanguageOnboardingPreview() {
    MaterialTheme {
        SelectLanguageOnboarding(
                startLang = "en",
                onLangSelect = {},
        )
    }
}
