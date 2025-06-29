package eu.rozmova.app.modules.scenarioinfo

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class ScenarioInfoState(
    val test: String = "test",
)

@HiltViewModel
class ScenarioInfoVM
    @Inject
    constructor() :
    ViewModel(),
        ContainerHost<ScenarioInfoState, Unit> {
        override val container = container<ScenarioInfoState, Unit>(ScenarioInfoState())
    }
