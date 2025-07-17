package eu.rozmova.app.state

import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateRepository
    @Inject
    constructor() {
        private val _refetch = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
        val refetch: MutableSharedFlow<Unit> = _refetch

        private val _fetchChats = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
        val fetchChats: MutableSharedFlow<Unit> = _fetchChats

        suspend fun triggerRefetch() {
            _refetch.emit(Unit)
        }

        suspend fun triggerFetchChats() {
            _fetchChats.emit(Unit)
        }
    }
