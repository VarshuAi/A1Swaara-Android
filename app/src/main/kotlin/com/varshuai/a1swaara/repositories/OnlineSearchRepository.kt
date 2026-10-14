package com.varshuai.a1swaara.repositories

import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.models.SearchQuery
import com.varshuai.a1swaara.query
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.models.bodies.SearchSuggestionsBody
import app.a1swaara.providers.youtubemusic.innertube.requests.searchSuggestions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface OnlineSearchRepository {
    fun observeHistory(input: String): Flow<List<SearchQuery>>
    fun observeHistoryCount(): Flow<Int>
    suspend fun fetchSuggestions(input: String): Result<List<String>?>?
    fun deleteHistory(searchQuery: SearchQuery)
    fun clearHistory()
    fun saveHistory(text: String)
}

object DatabaseOnlineSearchRepository : OnlineSearchRepository {
    override fun observeHistory(input: String): Flow<List<SearchQuery>> =
        Database.queries("%$input%")
            .distinctUntilChanged { old, new -> old.size == new.size }

    override fun observeHistoryCount(): Flow<Int> = Database.queriesCount().distinctUntilChanged()

    override suspend fun fetchSuggestions(input: String): Result<List<String>?>? =
        YoutubeMusicInnertube.searchSuggestions(body = SearchSuggestionsBody(input = input))

    override fun deleteHistory(searchQuery: SearchQuery) {
        query { Database.delete(searchQuery) }
    }

    override fun clearHistory() {
        query(Database::clearQueries)
    }

    override fun saveHistory(text: String) {
        query {
            Database.insert(SearchQuery(query = text))
        }
    }
}
