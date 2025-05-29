# Google Play Billing Integration for Rozmova Android

## Overview

This document outlines the Google Play Billing integration for subscription management in the Rozmova Android app. The integration provides a single monthly subscription option for premium features.

## Architecture

The billing integration follows a clean architecture pattern with the following components:

### Core Components

1. **Domain Models** (`domain/billing/`)
   - `SubscriptionProduct`: Represents subscription product details
   - `SubscriptionStatus`: Tracks user's subscription state
   - `BillingResult`: Handles billing operation results
   - `SubscriptionState`: UI state for subscription screens

2. **Billing Service** (`services/billing/`)
   - `BillingService`: Interface defining billing operations
   - `BillingServiceImpl`: Implementation using Google Play Billing Client
   - Handles connection management, product queries, and purchase flows

3. **Repository** (`repositories/billing/`)
   - `SubscriptionRepository`: Abstracts billing operations for UI layer
   - Provides reactive streams for subscription state changes

4. **UI Components** (`screens/subscription/`)
   - `SubscriptionScreen`: Main subscription management UI
   - `SubscriptionViewModel`: Handles UI state and user interactions

## Setup

### 1. Dependencies

The following dependencies are included in `app/build.gradle.kts`:

```kotlin
implementation(libs.androidx.billing.ktx) // Version 7.1.1
```

### 2. Permissions

Required permission in `AndroidManifest.xml`:

```xml
<uses-permission android:name="com.android.vending.BILLING" />
```

### 3. Product Configuration

Current subscription product ID: `rozmova_premium_monthly`

Configure this product in Google Play Console:
- Product ID: `rozmova_premium_monthly`
- Type: Auto-renewing subscription
- Billing period: 1 month
- Grace period: 3 days (recommended)
- Account hold: 30 days (recommended)

## Usage

### Checking Subscription Status

Use `SubscriptionManager` to check if a user is subscribed:

```kotlin
@Composable
fun MyFeature(subscriptionManager: SubscriptionManager = hiltViewModel()) {
    val isSubscribed by subscriptionManager.isUserSubscribed().collectAsState(initial = false)
    
    if (isSubscribed) {
        PremiumFeature()
    } else {
        FreeFeature()
    }
}
```

### Using Subscription Gate

For conditional UI rendering based on subscription status:

```kotlin
@Composable
fun MyScreen(subscriptionManager: SubscriptionManager) {
    SubscriptionGate(
        subscriptionManager = subscriptionManager,
        premiumContent = { PremiumContent() },
        freeContent = { FreeContentWithUpsell() }
    )
}
```

### Navigation to Subscription Screen

From Settings or any other screen:

```kotlin
// In navigation setup
navController.navigate(NavRoutes.Subscription.route)

// From ViewModel or onClick
onNavigateToSubscription = { navController.navigate(NavRoutes.Subscription.route) }
```

## Implementation Details

### Billing Connection Lifecycle

The billing service manages Google Play Billing Client connection:

- **Initialization**: Called when app starts or subscription screen opens
- **Connection**: Automatic retry on failure
- **Cleanup**: Proper disconnection when not needed

### Purchase Flow

1. User taps "Subscribe Now" button
2. System launches Google Play Billing flow
3. User completes purchase through Google Play
4. Purchase is acknowledged automatically
5. Subscription status updates throughout the app

### Error Handling

The system handles various error scenarios:

- **Network errors**: Retry with user notification
- **User cancellation**: Silent handling, return to previous state
- **Service unavailable**: Inform user to try later
- **Invalid products**: Fallback to error state

### Security Considerations

- **Server-side validation**: Implement server-side receipt validation for production
- **Purchase tokens**: Securely store and validate purchase tokens
- **Subscription verification**: Implement periodic subscription status checks

## Testing

### Testing with Google Play Console

1. **Internal Testing**: Use internal testing track for development
2. **License Testing**: Add test accounts in Google Play Console
3. **Sandbox**: Use sandbox environment for automated testing

### Test Scenarios

Test the following scenarios:

1. **First-time subscription**: New user subscribing
2. **Subscription renewal**: Automatic monthly renewal
3. **Subscription cancellation**: User cancels subscription
4. **Subscription expiry**: Handling expired subscriptions
5. **Network failures**: Offline/poor network conditions
6. **App restore**: Restoring purchases on new device

## Configuration

### Environment Setup

1. **Google Play Console**: Configure subscription products
2. **App Signing**: Ensure proper app signing for production
3. **Testing**: Set up test accounts and internal testing

### Product Management

Update subscription details in `BillingServiceImpl`:

```kotlin
companion object {
    const val PREMIUM_SUBSCRIPTION_ID = "rozmova_premium_monthly"
}
```

## Premium Features

Current premium features include:

- Unlimited conversations
- Advanced language models
- Priority support
- Ad-free experience
- Offline mode
- Custom conversation settings

## Monitoring and Analytics

Consider implementing:

- **Purchase analytics**: Track successful subscriptions
- **Churn analysis**: Monitor subscription cancellations
- **Revenue tracking**: Monitor subscription revenue
- **Error logging**: Log billing errors for debugging

## Troubleshooting

### Common Issues

1. **"Item not available"**: Check product configuration in Play Console
2. **"Service unavailable"**: Check Google Play Services availability
3. **Purchase not acknowledged**: Verify acknowledgment flow
4. **Subscription not recognized**: Check purchase token validation

### Debug Tools

- Enable logging in development builds
- Use Google Play Console for purchase verification
- Test with multiple Google accounts
- Verify proper app signing

## Security Notes

⚠️ **Important Security Considerations**

1. **Never store sensitive data**: Don't store payment information locally
2. **Server validation**: Always validate purchases server-side for production
3. **Secure communication**: Use HTTPS for all server communications
4. **Purchase verification**: Implement robust purchase verification

## Future Enhancements

Potential improvements:

1. **Multiple subscription tiers**: Add different subscription levels
2. **Free trial**: Implement free trial period
3. **Promotional offers**: Add introductory pricing
4. **Family sharing**: Support Google Play Family Library
5. **Pause/resume**: Allow subscription pausing

## Resources

- [Google Play Billing Documentation](https://developer.android.com/google/play/billing)
- [Google Play Console](https://play.google.com/console)
- [Billing Testing Guide](https://developer.android.com/google/play/billing/test)