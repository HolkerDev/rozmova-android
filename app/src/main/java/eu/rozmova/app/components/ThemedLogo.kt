package eu.rozmova.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.rozmova.app.R

@Composable
fun ThemedLogo() {
    val isDarkTheme = isSystemInDarkTheme()

    Image(
        painter = painterResource(
            if (isDarkTheme) R.drawable.rozmova_dark else R.drawable.rozmova_light
        ),
        modifier = Modifier.size(400.dp),
        contentScale = ContentScale.Fit,
        contentDescription = "Logo"
    )
}