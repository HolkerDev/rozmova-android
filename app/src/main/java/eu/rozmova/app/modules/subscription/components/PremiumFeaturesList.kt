package eu.rozmova.app.modules.subscription.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.rozmova.app.R

@Composable
fun PremiumFeaturesList(modifier: Modifier = Modifier) {
    val features =
        listOf(
            stringResource(R.string.premium_feature_unlimited_conversations),
            stringResource(R.string.premium_feature_dictionary_access),
            stringResource(R.string.premium_feature_transcription_access),
            stringResource(R.string.premium_feature_custom_scenario_generation),
            stringResource(R.string.premium_feature_integrated_translator_access),
        )

    Column(modifier = modifier) {
        features.forEach { feature ->
            Row(
                modifier =
                    Modifier.Companion
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Companion.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.Companion.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.Companion.width(12.dp))
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PremiumFeaturesListPreview() {
    MaterialTheme {
        PremiumFeaturesList(
            modifier = Modifier.Companion.padding(16.dp),
        )
    }
}
