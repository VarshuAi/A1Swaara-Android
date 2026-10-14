package com.varshuai.a1swaara.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.varshuai.a1swaara.models.PipedSession
import com.varshuai.a1swaara.repositories.SyncSettingsRepository
import app.a1swaara.providers.piped.models.Instance
import io.ktor.http.Url

class SyncSettingsViewModel(
    private val repository: SyncSettingsRepository
) : ViewModel() {
    fun observePipedSessions() = repository.observePipedSessions()

    suspend fun fetchInstances(): Result<List<Instance>>? = repository.fetchInstances()

    suspend fun login(apiBaseUrl: Url, username: String, password: String) =
        repository.login(apiBaseUrl = apiBaseUrl, username = username, password = password)

    fun saveSession(session: app.a1swaara.providers.piped.models.Session, username: String) =
        repository.saveSession(session = session, username = username)

    fun deleteSession(session: PipedSession) = repository.deleteSession(session)

    companion object {
        fun factory(repository: SyncSettingsRepository) = viewModelFactory {
            SyncSettingsViewModel(repository = repository)
        }
    }
}
