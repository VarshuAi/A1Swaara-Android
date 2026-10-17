package com.varshuai.a1swaara.repositories

import app.a1swaara.providers.sponsorblock.SponsorBlock
import app.a1swaara.providers.sponsorblock.models.Segment
import app.a1swaara.providers.sponsorblock.requests.segments

interface SponsorBlockRepository {
    suspend fun fetchSegments(videoId: String): Result<List<Segment>>?
}

object ApiSponsorBlockRepository : SponsorBlockRepository {
    override suspend fun fetchSegments(videoId: String): Result<List<Segment>>? =
        SponsorBlock.segments(videoId)
}
