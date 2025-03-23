package eu.rozmova.app.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.rozmova.app.R

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    isDisabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var message by remember { mutableStateOf("") }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.weight(1f),
            enabled = !isDisabled,
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text(stringResource(R.string.type_message)) },
            singleLine = true,
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (message.isNotBlank()) {
                    onSendMessage(message)
                    message = ""
                }
            },
            enabled = !isDisabled && message.isNotBlank(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(R.string.send_message),
            )
        }
    }
}

@Preview
@Composable
private fun MessageInputPreview() {
    MessageInput(onSendMessage = {}, isDisabled = false)
}
