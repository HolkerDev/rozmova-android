@file:OptIn(ExperimentalFoundationApi::class)

package eu.rozmova.app.modules.onboarding
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.modules.onboarding.components.Hobby
import eu.rozmova.app.modules.onboarding.components.PrivacyNoticeOnboarding
import eu.rozmova.app.modules.onboarding.components.SelectHobbiesOnboarding
import eu.rozmova.app.modules.onboarding.components.SelectLearningLangOnboarding
import eu.rozmova.app.modules.onboarding.components.SelectPronounOnboarding
import kotlinx.coroutines.launch

private data class Handlers(
    val saveAll: (hobbies: Set<String>) -> Unit,
)

@Composable
fun OnboardingScreen(
    onLearn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingVM = hiltViewModel(),
) {
    val lang = viewModel.getCurrentLanguage()
    viewModel.savePronoun("he") // Default salutation

    Content(
        handlers =
            Handlers(
                saveAll = {},
            ),
        startLanguage = lang,
        setInterfaceLang = { langCode -> viewModel.selectLanguage(langCode) },
        onLearnLangSelect = { learningLang ->
            viewModel.saveLearningLanguage(learningLang)
        },
        onOnboardingComplete = {
            viewModel.completeOnboarding()
            onLearn()
        },
        onPronounSelect = { salutationCode ->
            Log.i("Onboarding", "Selected salutation: $salutationCode")
            viewModel.savePronoun(salutationCode)
        },
        modifier = modifier,
    )
}

@Composable
private fun Content(
    handlers: Handlers,
    startLanguage: String,
    setInterfaceLang: (String) -> Unit,
    onLearnLangSelect: (String) -> Unit,
    onPronounSelect: (String) -> Unit,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    var learningLang by remember { mutableStateOf<String>("de") }
    var selectedHobbies by remember { mutableStateOf<Set<Hobby>>(emptySet()) }
    var isValid by remember { mutableStateOf(false) }

    LaunchedEffect(learningLang) {
        onLearnLangSelect(learningLang)
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 ->
                    SelectHobbiesOnboarding(
                        selectedHobbies = selectedHobbies,
                        onHobbyToggle = { hobby ->
                            selectedHobbies =
                                if (hobby in selectedHobbies) {
                                    selectedHobbies - hobby
                                } else {
                                    selectedHobbies + hobby
                                }

                            isValid = selectedHobbies.size >= 2
                        },
                    )
                1 ->
                    SelectLearningLangOnboarding(
                        onLangSelect = {
                            learningLang = it
                        },
                        selectedLang = learningLang,
                    )
                2 ->
                    SelectPronounOnboarding(
                        onPronounSelect = { pronounCode ->
                            onPronounSelect(pronounCode)
                        },
                    )
                3 -> PrivacyNoticeOnboarding()
            }
        }

        // Bottom section with indicators and buttons
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 32.dp),
            ) {
                repeat(4) { index ->
                    PageIndicator(
                        isSelected = index == pagerState.currentPage,
//                        color = onboardingPages[pagerState.currentPage].backgroundColor,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isValid) {
                    FloatingActionButton(
                        onClick = {
                            if (pagerState.currentPage < 3) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onOnboardingComplete()
                            }
                        },
//                    containerColor = onboardingPages[pagerState.currentPage].backgroundColor,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.size(56.dp),
                    ) {
                        Icon(
                            imageVector =
                                if (pagerState.currentPage < 3) {
                                    Icons.Default.ArrowForward
                                } else {
                                    Icons.Default.Check
                                },
                            contentDescription =
                                if (pagerState.currentPage < 3) {
                                    "Next"
                                } else {
                                    "Get Started"
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PageIndicator(
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val width by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 8.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "indicator_width",
    )

    Box(
        modifier =
            modifier
                .height(8.dp)
                .width(width)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isSelected) color else color.copy(alpha = 0.3f),
                ),
    )
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    MaterialTheme {
    }
}
