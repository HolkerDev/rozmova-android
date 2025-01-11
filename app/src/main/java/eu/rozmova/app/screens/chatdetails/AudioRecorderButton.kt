package eu.rozmova.app.screens.chatdetails

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.components.RecordButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AudioRecorderViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val tag = this::class.simpleName

        private var mediaRecorder: MediaRecorder? = null
        private var audioFile: File? = null

        private val _isRecording = MutableStateFlow(false)
        val isRecording = _isRecording.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading = _isLoading.asStateFlow()

        private val _recordedFilePath = MutableStateFlow<String?>(null)
        val recordedFilePath = _recordedFilePath.asStateFlow()

        fun startRecording() {
            try {
                // Create output file
                val outputDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                audioFile = File(outputDir, "recording_${System.currentTimeMillis()}.mp3")

                mediaRecorder =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        MediaRecorder(getApplication())
                    } else {
                        MediaRecorder()
                    }

                mediaRecorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(audioFile?.absolutePath)
                    prepare()
                    start()
                }

                _isRecording.value = true
            } catch (e: Exception) {
                Log.e(tag, "Error starting recording: ${e.message}")
                _isRecording.value = false
            }
        }

        fun stopRecording() {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                _recordedFilePath.value = audioFile?.absolutePath
            } catch (e: Exception) {
                Log.e(tag, "Error stopping recording: ${e.message}")
            } finally {
                _isRecording.value = false
            }
        }

        override fun onCleared() {
            super.onCleared()
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

@Composable
fun AudioRecorderButton(
    onRecordStart: () -> Unit,
    onRecordStop: () -> Unit,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    viewModel: AudioRecorderViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val tag = "AudioRecorderComponent"

    val recordedFilePath by viewModel.recordedFilePath.collectAsState()

    when (recordedFilePath) {
        null -> {}
        else -> {
            Log.i(tag, "Audio recorded to: $recordedFilePath")
            Toast
                .makeText(
                    context,
                    "Recording saved to: $recordedFilePath",
                    Toast.LENGTH_SHORT,
                ).show()
            viewModel.stopRecording()
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    viewModel.startRecording()
                } else {
                    Toast
                        .makeText(
                            context,
                            "Permission denied, cannot access microphone.",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            },
        )

    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxWidth()) {
        RecordButton(
            onRecord = {
                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED -> onRecordStart()
                    else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onStop = onRecordStop,
            isRecording = isRecording,
        )
    }
}
