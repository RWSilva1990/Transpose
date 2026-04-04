package com.example.ui.components.scrollbar

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ui.components.items.LoadingIndicator
import com.example.util.Logger


@Composable
fun <H, T> EndlessLazyColumn(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    hasMoreItems: Boolean,
    items: List<T>,
    headerData: H? = null,
    itemKey: (T) -> Any,
    headerContent: (@Composable (H) -> Unit)? = null,
    itemContent: @Composable (Int, T) -> Unit,
    loadMore: () -> Unit
) {

    val reachedBottom: Boolean by remember(listState) { derivedStateOf { listState.reachedBottom() } }

    LaunchedEffect(reachedBottom, loading, hasMoreItems, listState) {
        if (reachedBottom && !loading && hasMoreItems) {
            Logger.d("바닥 도달: reachedBottom=$reachedBottom, loading=$loading, hasMoreItems=$hasMoreItems")
            loadMore()
        }
    }

    LazyColumn(modifier = modifier, state = listState) {
        if (headerData != null && headerContent != null) {
            item {
                headerContent(headerData)
            }
        }
        items(count = items.size, key = { itemKey(items[it]) }){ index ->
            val item = items[index]
            itemContent(index, item)
            if (index == items.size - 1 && hasMoreItems)
                LoadingIndicator()
        }
    }
}


private fun LazyListState.reachedBottom(): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    val totalItems = this.layoutInfo.totalItemsCount

    if (totalItems <= 1) return false

    return lastVisibleItem != null &&
            lastVisibleItem.index == totalItems - 1 &&
            lastVisibleItem.offset + lastVisibleItem.size <= this.layoutInfo.viewportEndOffset + 20 // 약간의 여유 추가
}