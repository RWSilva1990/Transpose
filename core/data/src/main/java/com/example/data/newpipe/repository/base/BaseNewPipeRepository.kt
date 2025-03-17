package com.example.data.newpipe.repository.base

import com.example.data.newpipe.downloader.NewPipeDownloader
import com.example.data.newpipe.exception.NewPipeException
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.stream.StreamExtractor

abstract class BaseNewPipeRepository {
    protected val youtubeService: YoutubeService = ServiceList.YouTube

    init {
        NewPipe.init(NewPipeDownloader())
    }

    protected fun getVideoUrl(videoId: String): String {
        return if (videoId.startsWith("https")) videoId else youtubeService.streamLHFactory.getUrl(
            videoId
        )
    }

    protected fun getStreamExtractor(videoId: String): StreamExtractor {
        return youtubeService.getStreamExtractor(getVideoUrl(videoId))
    }

    protected fun getChannelLinkHandler(channelId: String): ListLinkHandler {
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

    protected fun getVideoId(url: String): String {
        return try {
            youtubeService.streamLHFactory.getId(url)
        } catch (e: ParsingException) {
            throw NewPipeException.ParsingException("Failed to extract video ID", e)
        }
    }

    protected fun getPlaylistId(url: String): String {
        return try {
            youtubeService.playlistLHFactory.getId(url)
        } catch (e: ParsingException) {
            throw NewPipeException.ParsingException("Failed to extract playlist ID", e)
        }
    }

    protected fun getChannelId(url: String): String {
        return try {
            youtubeService.channelLHFactory.getId(url).replace("channel/", "")
        } catch (e: ParsingException) {
            throw NewPipeException.ParsingException("Failed to extract channel ID", e)
        }
    }

    protected fun getChannelExtractor(linkHandler: ListLinkHandler): ChannelExtractor {
        return youtubeService.getChannelExtractor(linkHandler)
    }

    protected fun getChannelTabExtractor(linkHandler: ListLinkHandler): ChannelTabExtractor {
        return youtubeService.getChannelTabExtractor(linkHandler)
    }

    protected fun getPlaylistHandler(playlistId: String): ListLinkHandler {
        val factory: ListLinkHandlerFactory = youtubeService.playlistLHFactory
        return try {
            factory.fromUrl(playlistId)
        } catch (_: Exception) {
            factory.fromId(playlistId)
        }
    }

    protected fun determineTabContentType(contentFilters: List<String>): String {
        return when {
            contentFilters.contains("videos") -> "videos"
            contentFilters.contains("shorts") -> "shorts"
            contentFilters.contains("playlists") -> "playlists"
            contentFilters.contains("live") -> "livestreams"
            contentFilters.contains("channels") -> "channels"
            contentFilters.contains("albums") -> "albums"
            else -> "other"
        }
    }
}