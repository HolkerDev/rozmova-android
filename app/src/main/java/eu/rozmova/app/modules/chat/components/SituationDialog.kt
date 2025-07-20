package eu.rozmova.app.modules.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import eu.rozmova.app.R

@Composable
fun SituationDialog(
    situation: String,
    onClose: () -> Unit,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    AlertDialog(
        properties =
            DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true,
                usePlatformDefaultWidth = false,
            ),
        onDismissRequest = onClose,
        title = {
            Text(
                stringResource(R.string.situation),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = situation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onClose,
            ) {
                Text(stringResource(R.string.close_content_description))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier =
            Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .heightIn(max = screenHeight * 0.8f),
        shape = RoundedCornerShape(16.dp),
    )
}
