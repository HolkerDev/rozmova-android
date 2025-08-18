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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import eu.rozmova.app.domain.Level
import eu.rozmova.app.modules.onboarding.components.Hobby
import eu.rozmova.app.modules.onboarding.components.PrivacyNoticeOnboarding
import eu.rozmova.app.modules.onboarding.components.SelectHobbiesOnboarding
import eu.rozmova.app.modules.onboarding.components.SelectJobOnboarding
import eu.rozmova.app.modules.onboarding.components.SelectPronounOnboarding
import kotlinx.coroutines.launch

private data class Handlers(
    val saveAll: (pronoun: String, hobbies: Set<Hobby>, job: String?, level: Level) -> Unit,
)

@Composable
fun OnboardingScreen(
    onLearn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingVM = hiltViewModel(),
) {
    val state by viewModel.container.stateFlow.collectAsState()

    Content(
        handlers =
            Handlers(
                saveAll = { pronoun, hobbies, job, level ->
                    viewModel.initUser(
                        job = job,
                        hobbies = hobbies.map { it.name },
                        pronoun = pronoun,
                        level = level,
                    )
                },
            ),
        onOnboardingComplete = {
            onLearn()
        },
        onPronounSelect = { salutationCode ->
            Log.i("Onboarding", "Selected salutation: $salutationCode")
        },
        modifier = modifier,
    )
}

@Composable
private fun Content(
    handlers: Handlers,
    onPronounSelect: (String) -> Unit,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    var selectedHobbies by remember { mutableStateOf<Set<Hobby>>(emptySet()) }
    var selectedJob by remember { mutableStateOf<String?>(null) }
    var selectedPronoun by remember { mutableStateOf("") }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
        ) { page ->
            when (page) {
                0 -> {
                    SelectHobbiesOnboarding(
                        selectedHobbies = selectedHobbies,
                        onHobbyToggle = { hobby ->
                            selectedHobbies =
                                if (hobby in selectedHobbies) {
                                    selectedHobbies - hobby
                                } else {
                                    selectedHobbies + hobby
                                }
                        },
                        onNext = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                    )
                }

                1 -> {
                    SelectJobOnboarding(
                        initialState = selectedJob,
                        onNext = { job ->
                            selectedJob = job
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        },
                    )
                }

                2 -> {
                    SelectPronounOnboarding(
                        onPronounSelect = { pronounCode ->
                            selectedPronoun = pronounCode
                            onPronounSelect(pronounCode)
                        },
                        onNext = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(3)
                            }
                        },
                    )
                }

                3 ->
                    PrivacyNoticeOnboarding(
                        onNext = {
                            handlers.saveAll(
                                selectedPronoun,
                                selectedHobbies,
                                selectedJob,
                                Level.A1,
                            )
                            onOnboardingComplete()
                        },
                    )
            }
        }

        // Bottom section with page indicators
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
        ) {
            // Page indicators (centered)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.Center),
            ) {
                repeat(4) { index ->
                    PageIndicator(
                        isSelected = index == pagerState.currentPage,
                        color = MaterialTheme.colorScheme.primary,
                    )
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
        OnboardingScreen(
            onLearn = {},
        )
    }
}
