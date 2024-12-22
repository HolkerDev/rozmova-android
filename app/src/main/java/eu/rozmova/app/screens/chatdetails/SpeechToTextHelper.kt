package eu.rozmova.app.screens.chatdetails
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.ERROR_NO_MATCH
import android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

class SpeechToTextHelper(context: Context) {
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    }

    private val _transcription = mutableStateOf("")
    val transcription: State<String> get() = _transcription

    init {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // Called when speech recognition is ready
            }

            override fun onBeginningOfSpeech() {
                // Called when speech input starts
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Called when the input volume level changes
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Called when more audio data is received
            }

            override fun onEndOfSpeech() {
                // Called when speech input ends
            }

            override fun onError(error: Int) {
                // Handle error
                if (error == ERROR_SPEECH_TIMEOUT || error == ERROR_NO_MATCH) {
                    _transcription.value = "No speech input detected"
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                _transcription.value = matches?.get(0).orEmpty() // Get the first result
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Handle partial results
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Handle events
            }
        })
    }

    fun startListening() {
        speechRecognizer.startListening(recognizerIntent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }

    fun destroy() {
        speechRecognizer.destroy()
    }
}
