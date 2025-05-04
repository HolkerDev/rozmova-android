package eu.rozmova.app.components.bugreport

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.repositories.BugReportRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class BugReportState(
    val isSubmitting: Boolean = false,
)

sealed interface BugReportEvents {
    object BugReportSent : BugReportEvents

    object Error : BugReportEvents
}

@HiltViewModel
class BugReportViewModel
    @Inject
    constructor(
        private val bugReportRepository: BugReportRepository,
    ) : ViewModel(),
        ContainerHost<BugReportState, BugReportEvents> {
        override val container: Container<BugReportState, BugReportEvents> = container(BugReportState())

        fun sendBugReport(
            title: String,
            description: String,
        ) = intent {
            reduce {
                state.copy(isSubmitting = true)
            }

            bugReportRepository
                .sendBugReport(title, description)
                .map {
                    postSideEffect(BugReportEvents.BugReportSent)
                }.mapLeft {
                    postSideEffect(BugReportEvents.Error)
                }
        }
    }
