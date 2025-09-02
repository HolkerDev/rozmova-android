package eu.rozmova.app.modules.learn.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BucketCard(
    progress: Int,
    wordList: List<String>,
    onPracticeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            // Bucket Title
            Text(
                text = "Your Learning Bucket",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress / 100.toFloat() },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                drawStopIndicator = {},
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Word List Section
            Text(
                text = "Currently Learning",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Word List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                wordList.forEach { word ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Learned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = word,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

//            // More Items Indicator
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                Icon(
//                    imageVector = Icons.Default.MoreHoriz,
//                    contentDescription = "More words",
//                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
//                    modifier = Modifier.size(16.dp),
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = "$additionalWordsCount more words to learn",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    fontStyle = FontStyle.Italic,
//                )
//            }

            Spacer(modifier = Modifier.height(24.dp))

            // Practice Button
            Button(
                onClick = onPracticeClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Practice",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LearningBucketCardPreview() {
    MaterialTheme {
        BucketCard(
            onPracticeClick = {
                println("Practice button clicked!")
            },
            progress = 32,
            wordList = listOf("Hallo", "Danke", "Tschuss", "Guten Tag", "Ja"),
        )
    }
}
