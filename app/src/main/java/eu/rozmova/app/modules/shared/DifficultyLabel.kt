package eu.rozmova.app.modules.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.toDifficulty

@Composable
fun DifficultyLabel(
    difficulty: DifficultyDto,
    modifier: Modifier = Modifier,
) {
    val diff = difficulty.toDifficulty()

    Surface(
        color = diff.color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier.wrapContentSize(),
    ) {
        Text(
            text = stringResource(diff.labelId),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = diff.color,
        )
    }
}
