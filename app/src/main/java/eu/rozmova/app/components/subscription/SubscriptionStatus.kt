package eu.rozmova.app.components.subscription

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.utils.SubscriptionManager
import javax.inject.Inject

@Composable
fun SubscriptionGate(
    subscriptionManager: SubscriptionManager,
    premiumContent: @Composable () -> Unit,
    freeContent: @Composable () -> Unit
) {
    val isSubscribed by subscriptionManager.isUserSubscribed().collectAsState(initial = false)
    
    if (isSubscribed) {
        premiumContent()
    } else {
        freeContent()
    }
}

@HiltViewModel
class SubscriptionStatusViewModel @Inject constructor(
    val subscriptionManager: SubscriptionManager
) : ViewModel()