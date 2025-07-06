package eu.rozmova.app.modules.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Option 1: Gradient Card Button
// @Composable
// fun GradientCardButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
// ) {
//    Card(
//        modifier =
//            modifier
//                .fillMaxWidth()
//                .height(64.dp)
//                .clickable { onClick() },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        shape = RoundedCornerShape(16.dp),
//    ) {
//        Box(
//            modifier =
//                Modifier
//                    .fillMaxSize()
//                    .background(
//                        brush =
//                            Brush.horizontalGradient(
//                                colors =
//                                    listOf(
//                                        MaterialTheme.colorScheme.primary,
//                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
//                                    ),
//                            ),
//                    ),
//        ) {
//            Row(
//                modifier =
//                    Modifier
//                        .fillMaxSize()
//                        .padding(horizontal = 20.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween,
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.GridView,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.onPrimary,
//                        modifier = Modifier.size(24.dp),
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Text(
//                        text = "Browse All Scenarios",
//                        color = MaterialTheme.colorScheme.onPrimary,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Medium,
//                    )
//                }
//                Icon(
//                    imageVector = Icons.Default.KeyboardArrowRight,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.onPrimary,
//                    modifier = Modifier.size(20.dp),
//                )
//            }
//        }
//    }
// }
//
// Option 3: Icon Grid Preview Button
// @Composable
// fun IconGridPreviewButton(
//    scenarioCount: Int = 240,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
// ) {
//    Surface(
//        modifier =
//            modifier
//                .fillMaxWidth()
//                .clickable { onClick() },
//        shape = RoundedCornerShape(16.dp),
//        color = MaterialTheme.colorScheme.secondaryContainer,
//        tonalElevation = 1.dp,
//    ) {
//        Column(
//            modifier = Modifier.padding(20.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ) {
//            // Mini grid preview
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//            ) {
//                repeat(4) { index ->
//                    Surface(
//                        modifier = Modifier.size(32.dp),
//                        shape = RoundedCornerShape(8.dp),
//                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
//                    ) {
//                        Box(
//                            contentAlignment = Alignment.Center,
//                        ) {
//                            Text(
//                                text =
//                                    when (index) {
//                                        0 -> "🍽️"
//                                        1 -> "💼"
//                                        2 -> "🛒"
//                                        else -> "✈️"
//                                    },
//                                fontSize = 16.sp,
//                            )
//                        }
//                    }
//                }
//            }
//            Spacer(modifier = Modifier.height(12.dp))
//            Text(
//                text = "$scenarioCount+ Scenarios",
//                color = MaterialTheme.colorScheme.onSecondaryContainer,
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//            )
//            Text(
//                text = "View All",
//                color = MaterialTheme.colorScheme.primary,
//                fontSize = 12.sp,
//                fontWeight = FontWeight.SemiBold,
//            )
//        }
//    }
// }

// // Option 4: Floating Chip Button
// @Composable
// fun FloatingChipButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
// ) {
//    Surface(
//        modifier =
//            modifier
//                .clickable { onClick() }
//                .shadow(
//                    elevation = 8.dp,
//                    shape = RoundedCornerShape(24.dp),
//                ),
//        shape = RoundedCornerShape(24.dp),
//        color = MaterialTheme.colorScheme.primaryContainer,
//        tonalElevation = 3.dp,
//    ) {
//        Row(
//            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            Icon(
//                imageVector = Icons.Default.MenuBook,
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.onPrimaryContainer,
//                modifier = Modifier.size(20.dp),
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(
//                text = "All Scenarios",
//                color = MaterialTheme.colorScheme.onPrimaryContainer,
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//            )
//        }
//    }
// }

@Composable
fun ExploreAllScenariosButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        onClick = { onClick() },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                    ),
                            ),
                    ),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Explore All Scenarios",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Practice conversations in any situation",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun AllScenariosButtonDemo() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ExploreAllScenariosButton(onClick = { /* Handle click */ })
    }
}
