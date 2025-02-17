package eu.rozmova.app.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import eu.rozmova.app.R

@Composable
fun StopChatButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(stringResource(R.string.finish_discussion))
    }
}
