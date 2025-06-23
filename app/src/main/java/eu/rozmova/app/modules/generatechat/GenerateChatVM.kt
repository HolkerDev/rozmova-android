package eu.rozmova.app.modules.generatechat

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class GenerateChatState(
    val isLoading: Boolean = false,
)

@HiltViewModel
class GenerateChatVM
    @Inject
    constructor() :
    ViewModel(),
        ContainerHost<GenerateChatState, Unit> {
        private val tag = this::class.simpleName
        override val container: Container<GenerateChatState, Unit> = container(GenerateChatState())
    }
