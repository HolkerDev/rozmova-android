package eu.rozmova.app.modules.convochat.components.shouldfinishdialog

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.Author
import eu.rozmova.app.domain.MessageDto
import eu.rozmova.app.repositories.billing.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.io.File
import javax.inject.Inject

data class ShouldFinishUiState(
    val isSubscribed: Boolean = false,
)

sealed interface ShouldFinishAudioEvents {
    data object StopAll : ShouldFinishAudioEvents
}

@HiltViewModel
class ShouldFinishAudioVM
    @Inject
    constructor(
        private val exoPlayer: ExoPlayer,
        private val subscriptionRepository: SubscriptionRepository,
        application: Application,
    ) : AndroidViewModel(application),
        ContainerHost<ShouldFinishUiState, ShouldFinishAudioEvents> {
        override val container: Container<ShouldFinishUiState, ShouldFinishAudioEvents> =
            container(ShouldFinishUiState())

        init {
            exoPlayer.addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            intent {
                                postSideEffect(ShouldFinishAudioEvents.StopAll)
                            }
                        }
                    }
                },
            )

            fetchSubscription()
        }

        private fun fetchSubscription() =
            intent {
                val isSubscribed = subscriptionRepository.isSubscribed().first()
                reduce { state.copy(isSubscribed = isSubscribed) }
            }

        fun playAudio(message: MessageDto) =
            intent {
                stopPlayer()
                val audioUri = buildAudioUri(message)
                withContext(Dispatchers.Main) {
                    exoPlayer.setMediaItem(MediaItem.fromUri(audioUri))
                    exoPlayer.prepare()
                    exoPlayer.play()
                }
            }

        fun stopAudio() =
            intent {
                stopPlayer()
            }

        private fun buildAudioUri(messageDto: MessageDto): Uri {
            val audioId = messageDto.audioId
            val audioLink = messageDto.link
            val isUser = messageDto.author == Author.USER
            if (audioId == null || audioId.isEmpty()) {
                throw IllegalArgumentException("Audio ID cannot be empty")
            }
            if (isUser) {
                val outputDir =
                    getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                val audioFile = File(outputDir, "$audioId.mp4")
                val audioUri = Uri.fromFile(audioFile)
                return audioUri
            } else {
                return audioLink!!.toUri()
            }
        }

        private suspend fun stopPlayer() =
            withContext(Dispatchers.Main) {
                exoPlayer.stop()
            }
    }
