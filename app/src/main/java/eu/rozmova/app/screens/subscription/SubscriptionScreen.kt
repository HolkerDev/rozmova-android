package eu.rozmova.app.screens.subscription

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.domain.billing.SubscriptionState
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
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
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.close_content_description))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val subscriptionState = state.subscriptionState) {
                is SubscriptionState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is SubscriptionState.Available -> {
                    PremiumOfferContent(
                        product = subscriptionState.product,
                        isLoading = state.isLoading,
                        onPurchaseClick = { product ->
                            viewModel.purchaseSubscription(context as Activity, product)
                        }
                    )
                }
                
                is SubscriptionState.Subscribed -> {
                    SubscribedContent(
                        status = subscriptionState.status,
                        onRefreshClick = { viewModel.refreshSubscriptions() }
                    )
                }
                
                is SubscriptionState.Error -> {
                    ErrorContent(
                        message = subscriptionState.message,
                        onRetryClick = { viewModel.refreshSubscriptions() }
                    )
                }
                
                is SubscriptionState.NotAvailable -> {
                    ErrorContent(
                        message = "Subscription not available",
                        onRetryClick = { viewModel.refreshSubscriptions() }
                    )
                }
            }
            
            if (state.error != null) {
                LaunchedEffect(state.error) {
                    // Show error snackbar or dialog
                }
            }
            
            if (state.showSuccessMessage) {
                LaunchedEffect(state.showSuccessMessage) {
                    // Show success message
                    viewModel.clearSuccessMessage()
                }
            }
        }
    }
}

@Composable
private fun PremiumOfferContent(
    product: eu.rozmova.app.domain.billing.SubscriptionProduct,
    isLoading: Boolean,
    onPurchaseClick: (eu.rozmova.app.domain.billing.SubscriptionProduct) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.subscription_premium),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = product.formattedPrice,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = stringResource(R.string.subscription_per_month),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PremiumFeaturesList()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { onPurchaseClick(product) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.subscription_subscribe_now))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.subscription_cancel_anytime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PremiumFeaturesList() {
    val features = listOf(
        stringResource(R.string.premium_feature_unlimited_conversations),
        stringResource(R.string.premium_feature_advanced_models),
        stringResource(R.string.premium_feature_priority_support),
        stringResource(R.string.premium_feature_ad_free),
        stringResource(R.string.premium_feature_offline_mode),
        stringResource(R.string.premium_feature_custom_settings)
    )
    
    Column {
        features.forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SubscribedContent(
    status: eu.rozmova.app.domain.billing.SubscriptionStatus,
    onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.subscription_youre_premium),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.subscription_thank_you),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.subscription_status),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    StatusRow(
                        stringResource(R.string.subscription_status), 
                        if (status.isSubscribed) stringResource(R.string.subscription_status_active) else stringResource(R.string.subscription_status_inactive)
                    )
                    StatusRow(
                        stringResource(R.string.subscription_auto_renewing), 
                        if (status.autoRenewing) stringResource(R.string.confirm) else stringResource(R.string.cancel)
                    )
                    StatusRow(
                        stringResource(R.string.subscription_product_id), 
                        status.productId ?: "N/A"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onRefreshClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.subscription_refresh_status))
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.subscription_oops),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetryClick) {
            Text(stringResource(R.string.subscription_retry))
        }
    }
}