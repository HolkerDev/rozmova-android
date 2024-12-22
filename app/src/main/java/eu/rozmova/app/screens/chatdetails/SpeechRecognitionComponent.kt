package eu.rozmova.app.screens.chatdetails

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.components.RecordButton
import javax.inject.Inject

@HiltViewModel
class SpeechViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {

    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(application)
    private val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "de-DE")
        putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
    }

    // State to hold recognized speech
    private val _transcription = mutableStateOf("")
    val transcription: State<String> get() = _transcription

    init {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_NO_MATCH) {
                    _transcription.value = "No speech input detected"
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                _transcription.value = matches?.get(0).orEmpty() // Set first recognized result
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    // Start speech recognition
    fun startListening() {
        Log.i("SpeechViewModel", "Start listening...")
        speechRecognizer.startListening(recognizerIntent)
    }

    // Stop speech recognition
    fun stopListening() {
        speechRecognizer.stopListening()
    }

    // Release resources when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
    }
}

@Composable
fun SpeechRecognitionComponent(
    viewModel: SpeechViewModel = hiltViewModel(), context: Context = LocalContext.current
) {
    val transcription = viewModel.transcription.value

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.startListening()
            } else {
                Toast.makeText(
                    context,
                    "Permission denied, cannot access microphone.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    Column(
        modifier = Modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RecordButton(
            onRecord = {
                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED -> viewModel.startListening()
                    else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onStop = { viewModel.stopListening() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Recognized Speech: $transcription")
    }
}

