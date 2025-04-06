package com.example.data.newpipe.extractor.base

import com.example.data.newpipe.exception.NewPipeException
import okio.IOException
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory

abstract class Pager<I : InfoItem, O>(
    protected val streamingService: StreamingService,
    private val extractor: ListExtractor<out InfoItem>
) {
    private var nextPage: Page? = null
    private var hasNextPage = true

    protected val streamLinkHandler: LinkHandlerFactory = streamingService.streamLHFactory
    protected val playlistLinkHandler: ListLinkHandlerFactory = streamingService.playlistLHFactory
    protected val channelLinkHandler: LinkHandlerFactory = streamingService.channelLHFactory

    fun isHasNextPage(): Boolean = hasNextPage

    open fun getNextPage(): List<O> {
        if (!hasNextPage) {
            return emptyList()
        }
        return try {
            if (nextPage == null) {
                extractor.fetchPage()
                process(extractor.initialPage)
            } else {
                process(extractor.getPage(nextPage))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> throw NewPipeException.PageCannotBeLoaded("getNextPage from Pager", e)
                is ExtractionException -> throw NewPipeException.ExtractionFailed("getNextPage from Pager", e)
                else -> throw NewPipeException.UnknownError("getNextPage from Pager", e)
            }
        }
    }

    protected open fun process(page: ListExtractor.InfoItemsPage<out InfoItem>): List<O> {
        nextPage = page.nextPage
        hasNextPage = page.hasNextPage()
        return extract(page)
    }

    protected abstract fun extract(page: ListExtractor.InfoItemsPage<out InfoItem>): List<O>
}
