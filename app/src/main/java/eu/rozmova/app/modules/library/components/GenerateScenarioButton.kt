package eu.rozmova.app.modules.library.components
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Option 1: Creative Spark Button (Magical/AI theme)
// @Composable
// fun CreativeSparkButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
// ) {
//    Card(
//        modifier = modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
//        shape = RoundedCornerShape(16.dp),
//        onClick = { onClick() },
//    ) {
//        Box(
//            modifier =
//                Modifier
//                    .fillMaxWidth()
//                    .background(
//                        brush =
//                            Brush.linearGradient(
//                                colors =
//                                    listOf(
//                                        MaterialTheme.colorScheme.secondaryContainer,
//                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
//                                    ),
//                            ),
//                    ),
//        ) {
//            Row(
//                modifier =
//                    Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 20.dp, vertical = 16.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(16.dp),
//            ) {
//                Surface(
//                    shape = RoundedCornerShape(12.dp),
//                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
//                    modifier = Modifier.size(48.dp),
//                ) {
//                    Box(
//                        contentAlignment = Alignment.Center,
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.AutoFixHigh,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
//                            modifier = Modifier.size(24.dp),
//                        )
//                    }
//                }
//
//                Column(
//                    modifier = Modifier.weight(1f),
//                    verticalArrangement = Arrangement.spacedBy(2.dp),
//                ) {
//                    Text(
//                        text = "Create Custom Scenario",
//                        color = MaterialTheme.colorScheme.onSecondaryContainer,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold,
//                    )
//                    Text(
//                        text = "Generate scenarios tailored to your needs",
//                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
//                        fontSize = 12.sp,
//                    )
//                }
//            }
//        }
//    }
// }
//
// // Option 2: Compact Action Button
// @Composable
// fun CompactCreateButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
// ) {
//    OutlinedCard(
//        modifier = modifier.fillMaxWidth(),
//        onClick = { onClick() },
//        shape = RoundedCornerShape(12.dp),
//        border =
//            CardDefaults.outlinedCardBorder().copy(
//                brush =
//                    Brush.horizontalGradient(
//                        colors =
//                            listOf(
//                                MaterialTheme.colorScheme.outline,
//                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
//                            ),
//                    ),
//            ),
//    ) {
//        Row(
//            modifier =
//                Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(12.dp),
//        ) {
//            Icon(
//                imageVector = Icons.Default.Add,
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.size(20.dp),
//            )
//            Text(
//                text = "Create Custom Scenario",
//                color = MaterialTheme.colorScheme.onSurface,
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//            )
//        }
//    }
// }

// Option 3: AI-Themed Button
@Composable
fun GenerateScenarioButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = "Custom Scenario",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Let AI create a unique conversation for you",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

// // Option 4: Floating Action Style
// @Composable
// fun FloatingCreateButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
// ) {
//    ExtendedFloatingActionButton(
//        onClick = onClick,
//        modifier = modifier,
//        containerColor = MaterialTheme.colorScheme.primaryContainer,
//        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
//        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
//    ) {
//        Icon(
//            imageVector = Icons.Default.CreateNewFolder,
//            contentDescription = null,
//            modifier = Modifier.size(20.dp),
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(
//            text = "Create Custom",
//            fontWeight = FontWeight.Medium,
//            fontSize = 14.sp,
//        )
//    }
// }
//
// // Option 5: Minimal Text Button
// @Composable
// fun MinimalCreateButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
// ) {
//    TextButton(
//        onClick = onClick,
//        modifier = modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(8.dp),
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//        ) {
//            Icon(
//                imageVector = Icons.Default.Add,
//                contentDescription = null,
//                modifier = Modifier.size(18.dp),
//            )
//            Text(
//                text = "Create your own scenario",
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//            )
//        }
//    }
// }

// Demo screen showing all custom scenario buttons
@Composable
@Preview
private fun CustomScenarioButtonDemo() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Custom Scenario Buttons:",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

//        CreativeSparkButton(onClick = { /* Handle click */ })
//
//        CompactCreateButton(onClick = { /* Handle click */ })
//
        GenerateScenarioButton(onClick = { /* Handle click */ })
//
//        FloatingCreateButton(onClick = { /* Handle click */ })
//
//        MinimalCreateButton(onClick = { /* Handle click */ })
    }
}
