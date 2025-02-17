package eu.rozmova.app.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.domain.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsScreenViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsState()
    val englishLang = Language(stringResource(R.string.lang_en), "en")
    val germanLang = Language(stringResource(R.string.lang_de), "de")
    val greekLang = Language(stringResource(R.string.lang_el), "el")
    val ukrainianLang = Language(stringResource(R.string.lang_uk), "uk")
    val russianLang = Language(stringResource(R.string.lang_ru), "ru")

    val onLogOutClicked: () -> Unit = {
        viewModel.signOut()
    }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showNativeLanguageDialog by remember { mutableStateOf(false) }
    var learnLanguage by remember { mutableStateOf(englishLang) }
    var interfaceLanguage by remember { mutableStateOf(englishLang) }

    val languagesToLearn = listOf(germanLang, greekLang)

    val interfaceLanguages =
        listOf(
            englishLang,
            ukrainianLang,
            russianLang,
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        CenterAlignedTopAppBar(
            title = { Text(stringResource(R.string.settings_page_title)) },
            navigationIcon = {},
        )
        // Language Settings Section
        Text(
            text = stringResource(R.string.language_settings),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )

        // Learning Language
        ListItem(
            headlineContent = { Text(stringResource(R.string.language_to_learn)) },
            supportingContent = { Text(learnLanguage.name) },
            leadingContent = {
                Icon(Icons.Default.Language, contentDescription = null)
            },
            trailingContent = {
                Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = "Select language")
            },
            modifier = Modifier.clickable { showLanguageDialog = true },
        )

        // Native Language
        ListItem(
            headlineContent = { Text(stringResource(R.string.interace_language)) },
            supportingContent = { Text(text = interfaceLanguage.name) },
            leadingContent = {
                Icon(Icons.Default.Translate, contentDescription = null)
            },
            trailingContent = {
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = "Select native language",
                )
            },
            modifier = Modifier.clickable { showNativeLanguageDialog = true },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Account Settings Section
        Text(
            text = stringResource(R.string.settings_account),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.logout)) },
            leadingContent = {
                Icon(
                    Icons.AutoMirrored.Default.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            modifier = Modifier.clickable(onClick = onLogOutClicked),
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.learn_lang_select)) },
            text = {
                Column {
                    languagesToLearn.forEach { language ->
                        ListItem(
                            headlineContent = { Text(language.name) },
                            leadingContent = {
                                RadioButton(
                                    selected = language == learnLanguage,
                                    onClick = {
                                        learnLanguage = language
                                        showLanguageDialog = false
                                    },
                                )
                            },
                            modifier =
                                Modifier.clickable {
                                    learnLanguage = language
                                    showLanguageDialog = false
                                },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Interface Language Selection Dialog
    if (showNativeLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showNativeLanguageDialog = false },
            title = { Text(stringResource(R.string.interface_lang_select)) },
            text = {
                Column {
                    interfaceLanguages.forEach { language ->
                        ListItem(
                            headlineContent = { Text(language.name) },
                            leadingContent = {
                                RadioButton(
                                    selected = language == interfaceLanguage,
                                    onClick = {
                                        interfaceLanguage = language
                                        showNativeLanguageDialog = false
                                    },
                                )
                            },
                            modifier =
                                Modifier
                                    .clickable {
                                        interfaceLanguage = language
                                        showNativeLanguageDialog = false
                                    }.background(MaterialTheme.colorScheme.surface),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNativeLanguageDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
