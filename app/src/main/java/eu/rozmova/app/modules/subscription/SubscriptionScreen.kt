package eu.rozmova.app.modules.subscription

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.domain.billing.SubscriptionState
import eu.rozmova.app.modules.subscription.components.PremiumOfferContent
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubscriptionViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SubscriptionSideEffect.ShowError -> {
                // Handle error
            }
            is SubscriptionSideEffect.ShowSuccess -> {
                // Handle success
            }
            is SubscriptionSideEffect.NavigateBack -> {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.subscription_premium)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.close_content_description),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier.Companion
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val subscriptionState = state.subscriptionState) {
                is SubscriptionState.Loading -> {
                    Box(
                        modifier = Modifier.Companion.fillMaxSize(),
                        contentAlignment = Alignment.Companion.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is SubscriptionState.Available -> {
                    PremiumOfferContent(
                        isLoading = state.isLoading,
                        formattedPrice = subscriptionState.product.formattedPrice,
                        onPurchaseClick = {
                            viewModel.purchaseSubscription(context as Activity, subscriptionState.product)
                        },
                    )
                }

                is SubscriptionState.Subscribed -> {
                    SubscribedContent()
                }

                is SubscriptionState.Error -> {
                    ErrorContent(
                        message = subscriptionState.message,
                        onRetryClick = { },
                    )
                }

                is SubscriptionState.NotAvailable -> {
                    ErrorContent(
                        message = "Subscription not available",
                        onRetryClick = { },
                    )
                }
            }

            if (state.error != null) {
                LaunchedEffect(state.error) {
                }
            }

            if (state.showSuccessMessage) {
                LaunchedEffect(state.showSuccessMessage) {
                }
            }
        }
    }
}

@Composable
private fun SubscribedContent() {
    Card(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.subscription_youre_premium),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.subscription_thank_you),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier.Companion
                .fillMaxWidth()
                .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Companion.Medium,
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier.Companion.fillMaxWidth(),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.subscription_oops),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Companion.Bold,
        )

        Spacer(modifier = Modifier.Companion.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Companion.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.Companion.height(16.dp))

        Button(onClick = onRetryClick) {
            Text(stringResource(R.string.subscription_retry))
        }
    }
}

@Preview
@Composable
private fun PreviewSubscribedState() {
    SubscribedContent()
}
