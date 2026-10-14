package com.varshuai.a1swaara.repositories

import app.a1swaara.providers.piped.Piped
import app.a1swaara.providers.piped.models.Playlist
import app.a1swaara.providers.piped.models.Session
import java.util.UUID

interface PipedPlaylistRepository {
    suspend fun fetchPlaylist(session: Session, playlistId: UUID): Playlist?
}

object ApiPipedPlaylistRepository : PipedPlaylistRepository {
    override suspend fun fetchPlaylist(session: Session, playlistId: UUID): Playlist? =
        Piped.playlist.songs(session = session, id = playlistId)?.getOrNull()
}
