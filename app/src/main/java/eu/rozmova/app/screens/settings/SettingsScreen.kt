package eu.rozmova.app.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.components.bugreport.BugReportDialog
import eu.rozmova.app.domain.INTERFACE_LANGUAGES
import eu.rozmova.app.domain.LEARN_LANGUAGES
import eu.rozmova.app.domain.Language
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun SettingsScreen(
    onNavigateToSubscription: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsScreenViewModel = hiltViewModel(),
) {
    val onLogOutClicked: () -> Unit = {
        viewModel.signOut()
    }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showInterfaceSelectionDialog by remember { mutableStateOf(false) }
    var showBugReportDialog by remember { mutableStateOf(false) }
    var showDeleteDataDialog by remember { mutableStateOf(false) }

    var learnLanguage by remember { mutableStateOf<Language>(Language.GERMAN) }
    var interfaceLanguage by remember { mutableStateOf<Language>(Language.ENGLISH) }

    val state by viewModel.collectAsState()

    if (state.isLoading) {
        LinearProgressIndicator()
    }

    state.langSettings?.let { langSettings ->
        learnLanguage = langSettings.learningLang
        interfaceLanguage = langSettings.interfaceLang
        SettingsContent(
            showInterfaceLanguageDialog = showInterfaceSelectionDialog,
            showLearnLanguageDialog = showLanguageDialog,
            onLogOutClick = onLogOutClicked,
            onLearningLangSelectClick = { showLanguageDialog = true },
            onInterfaceLangSelectClick = { showInterfaceSelectionDialog = true },
            onInterfaceLangSelect = { language ->
                showInterfaceSelectionDialog = false
                viewModel.setLocale(language.code)
            },
            learnLanguage = learnLanguage,
            interfaceLanguage = interfaceLanguage,
            onLearningDialogDismiss = { showLanguageDialog = false },
            onInterfaceDialogDismiss = { showInterfaceSelectionDialog = false },
            onLearnLangSelect = { language ->
                showLanguageDialog = false
                viewModel.setLearningLanguage(language)
            },
            showLearningLanguageSelector = true,
            showBugReportDialog = showBugReportDialog,
            onBugReportClick = { showBugReportDialog = true },
            onBugReportDismiss = { showBugReportDialog = false },
            showDeleteDataDialog = showDeleteDataDialog,
            onDeleteDataClick = { showDeleteDataDialog = true },
            onDeleteDataDismiss = { showDeleteDataDialog = false },
            onDeleteDataConfirm = {
                showDeleteDataDialog = false
                viewModel.deleteUserData()
            },
            onNavigateToSubscription = onNavigateToSubscription,
            isSubscribed = state.isSubscribed,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    showInterfaceLanguageDialog: Boolean,
    showLearnLanguageDialog: Boolean,
    showLearningLanguageSelector: Boolean,
    onLogOutClick: () -> Unit,
    onInterfaceLangSelectClick: () -> Unit,
    onLearningLangSelectClick: () -> Unit,
    onLearningDialogDismiss: () -> Unit,
    onInterfaceDialogDismiss: () -> Unit,
    onInterfaceLangSelect: (Language) -> Unit,
    onLearnLangSelect: (Language) -> Unit,
    showBugReportDialog: Boolean,
    onBugReportClick: () -> Unit,
    onBugReportDismiss: () -> Unit,
    showDeleteDataDialog: Boolean,
    onDeleteDataClick: () -> Unit,
    onDeleteDataDismiss: () -> Unit,
    onDeleteDataConfirm: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    learnLanguage: Language,
    interfaceLanguage: Language,
    isSubscribed: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_page_title)) },
        )
        // Language Settings Section
        Text(
            text = stringResource(R.string.language_settings),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )

        // Learning Language
        if (showLearningLanguageSelector) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.language_to_learn)) },
                supportingContent = { Text(stringResource(learnLanguage.resId)) },
                leadingContent = {
                    Icon(Icons.Default.Language, contentDescription = null)
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Default.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.select_language_content_description),
                    )
                },
                modifier = Modifier.clickable { onLearningLangSelectClick() },
            )
        }

        // Interface Language
        ListItem(
            headlineContent = { Text(stringResource(R.string.interface_language)) },
            supportingContent = { Text(text = stringResource(interfaceLanguage.resId)) },
            leadingContent = {
                Icon(Icons.Default.Translate, contentDescription = null)
            },
            trailingContent = {
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.select_native_language_content_description),
                )
            },
            modifier = Modifier.clickable { onInterfaceLangSelectClick() },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Account Settings Section
        Text(
            text = stringResource(R.string.settings_account),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.subscription_premium)) },
            supportingContent = {
                Text(stringResource(if (!isSubscribed) R.string.subscription_unlock_features else R.string.user_has_premium_description))
            },
            leadingContent = {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            trailingContent = {
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null,
                )
            },
            modifier = Modifier.clickable(onClick = onNavigateToSubscription),
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.propose_idea)) },
            leadingContent = {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            modifier = Modifier.clickable(onClick = onBugReportClick),
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
            modifier = Modifier.clickable(onClick = onLogOutClick),
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.delete_user_data)) },
            supportingContent = { Text(stringResource(R.string.delete_user_data_description)) },
            leadingContent = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            modifier = Modifier.clickable(onClick = onDeleteDataClick),
        )

        if (showBugReportDialog) {
            BugReportDialog(onDismiss = onBugReportDismiss)
        }

        // Delete User Data Confirmation Dialog
        if (showDeleteDataDialog) {
            AlertDialog(
                onDismissRequest = onDeleteDataDismiss,
                title = { Text(stringResource(R.string.delete_user_data_title)) },
                text = {
                    Text(stringResource(R.string.delete_user_data_confirmation))
                },
                confirmButton = {
                    TextButton(
                        onClick = onDeleteDataConfirm,
                    ) {
                        Text(
                            stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDeleteDataDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        // Language Selection Dialog
        if (showLearnLanguageDialog) {
            AlertDialog(
                onDismissRequest = { onLearningDialogDismiss() },
                title = { Text(stringResource(R.string.learn_lang_select)) },
                text = {
                    Column {
                        LEARN_LANGUAGES.forEach { language ->
                            ListItem(
                                headlineContent = { Text(stringResource(language.resId)) },
                                leadingContent = {
                                    RadioButton(
                                        selected = language == learnLanguage,
                                        onClick = null,
                                    )
                                },
                                modifier =
                                    Modifier.clickable {
                                        onLearnLangSelect(language)
                                    },
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onLearningDialogDismiss() }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        // Interface Language Selection Dialog
        if (showInterfaceLanguageDialog) {
            AlertDialog(
                onDismissRequest = { onInterfaceDialogDismiss() },
                title = { Text(stringResource(R.string.interface_lang_select)) },
                text = {
                    Column {
                        INTERFACE_LANGUAGES.forEach { language ->
                            ListItem(
                                headlineContent = { Text(stringResource(language.resId)) },
                                leadingContent = {
                                    RadioButton(
                                        selected = language == interfaceLanguage,
                                        onClick = null,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .clickable {
                                            onInterfaceLangSelect(language)
                                        }.background(MaterialTheme.colorScheme.surface),
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onInterfaceDialogDismiss() }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}
