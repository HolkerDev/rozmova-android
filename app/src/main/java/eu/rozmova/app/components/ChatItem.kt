package eu.rozmova.app.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class ChatItem(
    val id: String,
    val title: String,
    val labels: List<String>,
    val isActive: Boolean,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatItem(chat: ChatItem) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = { /* Handle click */ })) {
        Column {
            Text(text = chat.title)
            FlowRow(modifier = Modifier.fillMaxWidth()) {
                chat.labels.forEach { label ->
                    Chip(label)
                }
            }
            // Chat title
            // Chat last message
            // Chat last message time
        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        modifier = Modifier.padding(vertical = 4.dp),
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}