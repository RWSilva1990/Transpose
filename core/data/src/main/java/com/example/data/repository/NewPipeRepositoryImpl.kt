package com.example.data.repository

import com.example.data.newpipe.downloader.NewPipeDownloader
import com.example.data.newpipe.exception.NewPipeException
import com.example.data.newpipe.extractor.PlaylistPager
import com.example.data.newpipe.extractor.ContentPager
import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.model.youtube.search.SearchResult
import com.example.domain.model.youtube.video_detail.VideoDetailData
import com.example.domain.repository.NewPipeRepository
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.stream.StreamExtractor

class NewPipeRepositoryImpl : NewPipeRepository {

    private val youtubeService: YoutubeService = ServiceList.YouTube

    private var currentContentPager: ContentPager? = null

    init {
        NewPipe.init(NewPipeDownloader())
    }

    override suspend fun search(query: String): Result<List<SearchResult>> {
        return try {
            val searchExtractor = youtubeService.getSearchExtractor(query)
            searchExtractor.fetchPage()
            val pager = ContentPager(youtubeService, searchExtractor)
            currentContentPager = pager
            Result.success(pager.getNextPage())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadMoreSearchResults(): Result<List<SearchResult>> {
        val pager = currentContentPager
            ?: return Result.failure(IllegalStateException("No search initiated"))
        return try {
            Result.success(pager.getNextPage())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun canLoadMoreSearchResults(): Boolean {
        return currentContentPager?.isHasNextPage() == true
    }

    override suspend fun fetchPlaylistResult(playlistId: String): Result<Playlist> {
        return try {
            val linkHandler = getPlaylistHandler(playlistId)
            val playlistExtractor = youtubeService.getPlaylistExtractor(linkHandler)
            playlistExtractor.fetchPage()
            Result.success(InfoItemMapper.playlistExtractorToDomain(playlistExtractor))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchPlaylistItemsResult(playlistId: String): Result<List<PlaylistItem>> {
        return try {
            val linkHandler = getPlaylistHandler(playlistId)
            val playlistExtractor = youtubeService.getPlaylistExtractor(linkHandler)
            playlistExtractor.fetchPage()
            val pager = PlaylistPager(youtubeService, playlistExtractor)
            Result.success(pager.getNextPage())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    private fun getPlaylistHandler(playlistId: String): ListLinkHandler {
        val factory: ListLinkHandlerFactory = youtubeService.playlistLHFactory
        return try {
            factory.fromUrl(playlistId)
        } catch (_: Exception) {
            factory.fromId(playlistId)
        }
    }

    private fun getVideoUrl(videoId: String): String {
        return if (videoId.startsWith("https")) videoId else youtubeService.streamLHFactory.getUrl(videoId)
    }

    private fun getStreamExtractor(videoId: String): StreamExtractor {
        return youtubeService.getStreamExtractor(getVideoUrl(videoId))
    }

    private fun getChannelExtractor(linkHandler: ListLinkHandler): ChannelExtractor {
        return youtubeService.getChannelExtractor(linkHandler)
    }

    private fun getChannelTabExtractor(linkHandler: ListLinkHandler): ChannelTabExtractor {
        return youtubeService.getChannelTabExtractor(linkHandler)
    }

    private fun getChannelLinkHandler(channelId: String): ListLinkHandler{
        val factory: ListLinkHandlerFactory = youtubeService.channelLHFactory
        return try {
            factory.fromUrl(channelId)
        } catch (urlParsingException: Exception) {
            try {
                factory.fromId(channelId)
            } catch (idParsingException: Exception) {
                throw NewPipeException.ParsingException(
                    "getChannelLinkHandler",
                    idParsingException
                )
            }
        }
    }

//    override suspend fun fetchPlaylistWithChannelId(channelId: String): Result<List<Playlist>?> {
//        try {
//            val channelLinkHandler = getChannelLinkHandler(channelId)
//
//            val channelExtractor = getChannelExtractor(channelLinkHandler)
//
//            channelExtractor.fetchPage()
//
//            val playlistsTabLinkHandler = channelExtractor.tabs.find { it.contentFilters.contains("playlists") }
//
//            if (playlistsTabLinkHandler != null) {
//                val channelTabExtractor = getChannelTabExtractor(playlistsTabLinkHandler)
//                val pager = ContentPager(youtubeService, channelTabExtractor)
//
//                return Result.success(pager.getNextPage())
//            }
//            return Result.failure(Exception("No playlist in that channel Id"))
//
//        }catch (e: Exception){
//            return Result.failure(e)
//        }
//    }


    override suspend fun fetchVideoDetail(videoId: String): Result<VideoDetailData> {
        return try {
            val extractor = getStreamExtractor(videoId)
            extractor.fetchPage()
            Result.success(
                InfoItemMapper.streamExtractorToVideoDetail(extractor)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
