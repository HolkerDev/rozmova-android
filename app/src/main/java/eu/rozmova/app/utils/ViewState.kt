package eu.rozmova.app.utils

sealed class ViewState<out T> {
    data object Loading : ViewState<Nothing>()

    data class Success<T>(
        val data: T,
    ) : ViewState<T>()

    data class Error(
        val error: Throwable? = null,
    ) : ViewState<Nothing>()

    data object Empty : ViewState<Nothing>()
}
