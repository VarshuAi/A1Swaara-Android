package com.varshuai.a1swaara.repositories

import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.models.PipedSession
import com.varshuai.a1swaara.transaction
import app.a1swaara.providers.piped.Piped
import app.a1swaara.providers.piped.models.Instance
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow

interface SyncSettingsRepository {
    fun observePipedSessions(): Flow<List<PipedSession>>
    suspend fun fetchInstances(): Result<List<Instance>>?
    suspend fun login(
        apiBaseUrl: Url,
        username: String,
        password: String
    ): app.a1swaara.providers.piped.models.Session?

    fun saveSession(session: app.a1swaara.providers.piped.models.Session, username: String)
    fun deleteSession(session: PipedSession)
}

object DatabaseSyncSettingsRepository : SyncSettingsRepository {
    override fun observePipedSessions(): Flow<List<PipedSession>> = Database.pipedSessions()

    override suspend fun fetchInstances(): Result<List<Instance>>? = Piped.getInstances()

    override suspend fun login(
        apiBaseUrl: Url,
        username: String,
        password: String
    ): app.a1swaara.providers.piped.models.Session? =
        Piped.login(
            apiBaseUrl = apiBaseUrl,
            username = username,
            password = password
        )?.getOrNull()

    override fun saveSession(
        session: app.a1swaara.providers.piped.models.Session,
        username: String
    ) {
        transaction {
            Database.insert(
                PipedSession(
                    apiBaseUrl = session.apiBaseUrl,
                    username = username,
                    token = session.token
                )
            )
        }
    }

    override fun deleteSession(session: PipedSession) {
        transaction { Database.delete(session) }
    }
}
