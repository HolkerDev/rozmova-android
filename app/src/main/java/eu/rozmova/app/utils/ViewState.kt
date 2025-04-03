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

fun <T, R> ViewState<T>.mapSuccess(transform: (T) -> R): ViewState<R> =
    when (this) {
        is ViewState.Success -> ViewState.Success(transform(data))
        is ViewState.Loading -> ViewState.Loading
        is ViewState.Error -> ViewState.Error(error)
        is ViewState.Empty -> ViewState.Empty
    }
