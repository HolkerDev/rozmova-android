package eu.rozmova.app.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimpleToolBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp, horizontal = 16.dp),
        ) {
            onBack?.let {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .clickable { onBack() },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "back arrow",
                        modifier =
                            Modifier
                                .padding(2.dp)
                                .size(30.dp),
                    )
                }
            }

            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontSize = 25.sp,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 40.dp),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.dp),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    SimpleToolBar(title = "Title", onBack = {})
}
