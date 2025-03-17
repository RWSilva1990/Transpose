package com.example.ui.screen.channel

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.domain.model.youtube.channel.ChannelDetail
import com.example.domain.model.youtube.channel.ChannelTabResult
import com.example.transpose.core.ui.R
import com.example.ui.common.PaginatedState
import com.example.ui.components.items.CommonVideoItem
import com.example.ui.components.items.ShortsVideoItem
import com.example.ui.components.scrollbar.EndlessLazyColumn
import com.example.ui.screen.channel.items.ChannelHeader
import com.example.ui.screen.channel.items.PlaylistItem
import com.example.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    channelViewModel: ChannelViewModel,
    channelId: String?,
    bottomSheetState: SheetState,
    onNavigateToPlaylistInfoScreen: (String) -> Unit
) {
    val channelDetailData by channelViewModel.channelDetail.collectAsState()
    val isChannelDetailLoading by channelViewModel.isChannelDetailDataLoading.collectAsState()

    // 각 탭의 상태 가져오기
    val videosState by channelViewModel.channelTabVideos.collectAsState()
    val shortsState by channelViewModel.channelTabShorts.collectAsState()
    val playlistsState by channelViewModel.channelTabPlaylists.collectAsState()

    val videosScrollState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    val shortsScrollState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    val playlistsScrollState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    val subscriberCountFormats = stringArrayResource(R.array.subscriber_count_formats)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    var selectedTabIndex by remember { mutableIntStateOf(0) }


    val currentScrollState = when (selectedTabIndex) {
        0 -> videosScrollState
        1 -> shortsScrollState
        2 -> playlistsScrollState
        else -> videosScrollState
    }

    val isScrolled by remember(selectedTabIndex) {
        derivedStateOf {
            val scrolled =
                currentScrollState.firstVisibleItemIndex > 0 || currentScrollState.firstVisibleItemScrollOffset > 400
            scrolled
        }
    }

    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }

    LaunchedEffect(key1 = channelId) {
        channelId?.let {
            channelViewModel.loadChannelDetail(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 채널 세부 정보 로딩 중일 때 중앙에 로딩 인디케이터 표시
        if (isChannelDetailLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            channelDetailData?.let { channel ->
                val allTabTitles = context.resources.getStringArray(R.array.tab_titles)

                val usedTabIndices = listOf(1, 2, 4)
                val tabTitles = usedTabIndices.map { allTabTitles[it] }.toTypedArray()

                val contentTypeMap = mapOf(
                    1 to "videos",
                    2 to "shorts",
                    4 to "playlists"
                )

                val actualToResourceIndex = mapOf(
                    0 to 1,
                    1 to 2,
                    2 to 4
                )

                val resourceTabIndex = actualToResourceIndex[selectedTabIndex] ?: 1

                if (isScrolled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f)
                            .background(Color.White)
                    ) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTabIndex,
                            edgePadding = 0.dp,
                            indicator = { tabPositions ->
                                if (tabPositions.isNotEmpty() && selectedTabIndex < tabPositions.size) {
                                    TabRowDefaults.Indicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                        color = Color.Black,
                                        height = 3.dp
                                    )
                                }
                            }
                        ) {
                            tabTitles.forEachIndexed { index, tab ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = {
                                        selectedTabIndex = index
                                        channelId?.let {
                                            val resourceIndex = actualToResourceIndex[index] ?: 1
                                            val contentType = contentTypeMap[resourceIndex]
                                            Logger.d("콘텐츠 유형: $contentType")
                                            channelViewModel.loadTabContent(it, contentType)
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = tab,
                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                val contentState = when (selectedTabIndex) {
                    0 -> videosState
                    1 -> shortsState
                    2 -> playlistsState
                    else -> videosState
                }

                val contentType = contentTypeMap[resourceTabIndex] ?: "videos"

                DisplayTabContent(
                    contentType = contentType,
                    contentState = contentState,
                    channelDetail = channel,
                    isScrolled = isScrolled,
                    selectedTabIndex = selectedTabIndex,
                    tabTitles = tabTitles,
                    actualToResourceIndex = actualToResourceIndex,
                    contentTypeMap = contentTypeMap,
                    onTabSelected = { index ->
                        selectedTabIndex = index
                        channelId?.let { id ->
                            val resourceIndex = actualToResourceIndex[index] ?: 1
                            val type = contentTypeMap[resourceIndex]
                            Logger.d("콘텐츠 유형: $type")

                            channelViewModel.onTabChanged(id, type)
                        }
                    },
                    loadMore = {
                        channelId?.let { id ->
                            Logger.d("EndlessLazyColumn이 loadMore 호출: contentType=$contentType")
                            channelViewModel.loadMoreContent(
                                id,
                                contentType
                            )
                        }
                    },
                    scrollState = currentScrollState,
                    bottomSheetState = bottomSheetState,
                    coroutineScope = coroutineScope,
                    channelViewModel = channelViewModel,
                    onNavigateToPlaylistInfoScreen = onNavigateToPlaylistInfoScreen
                )
            } ?: run {
                // 채널 데이터가 없는 경우
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisplayTabContent(
    contentType: String,
    contentState: PaginatedState<ChannelTabResult>?,
    channelDetail: ChannelDetail,
    isScrolled: Boolean,
    selectedTabIndex: Int,
    tabTitles: Array<String>,
    actualToResourceIndex: Map<Int, Int>,
    contentTypeMap: Map<Int, String>,
    onTabSelected: (Int) -> Unit,
    loadMore: () -> Unit,
    scrollState: LazyListState,
    bottomSheetState: SheetState,
    coroutineScope: CoroutineScope,
    channelViewModel: ChannelViewModel,
    onNavigateToPlaylistInfoScreen: (String) -> Unit
) {
    // 현재 탭 상태에 따른 데이터 및 상태 설정
    val items = when (contentState) {
        is PaginatedState.Success -> contentState.items
        else -> emptyList()
    }

    var selectedVideo by remember {
        mutableStateOf(null as ChannelTabResult.VideoResult?)
    }

    val isLoading = contentState is PaginatedState.Loading
    val hasMore = if (contentState is PaginatedState.Success) contentState.hasMore else false
    val isLoadingMore =
        if (contentState is PaginatedState.Success) contentState.isLoadingMore else false
    var isShowingPlaylistDialog by remember {
        mutableStateOf(false)
    }

    if (isLoading) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ChannelHeader(
                    channel = channelDetail,
                    isScrolled = isScrolled,
                    selectedTabIndex = selectedTabIndex,
                    tabTitles = tabTitles,
                    onTabSelected = onTabSelected
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    } else if (items.isEmpty()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ChannelHeader(
                    channel = channelDetail,
                    isScrolled = isScrolled,
                    selectedTabIndex = selectedTabIndex,
                    tabTitles = tabTitles,
                    onTabSelected = onTabSelected
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "이 탭에 표시할 콘텐츠가 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        }
    } else {
        EndlessLazyColumn(
            modifier = Modifier.fillMaxSize(),
            loading = isLoadingMore,
            listState = scrollState,
            hasMoreItems = hasMore,
            items = items,
            headerData = channelDetail,
            itemKey = { item: ChannelTabResult ->
                when (item) {
                    is ChannelTabResult.VideoResult -> "video_${item.video.id}"
                    is ChannelTabResult.PlaylistResult -> "playlist_${item.playlist.id}"
                    is ChannelTabResult.ShortsResult -> "short_${item.video.id}"
                }
            },
            headerContent = { channel ->
                ChannelHeader(
                    channel = channel,
                    isScrolled = isScrolled,
                    selectedTabIndex = selectedTabIndex,
                    tabTitles = tabTitles,
                    onTabSelected = onTabSelected
                )
            },
            itemContent = { index, item: ChannelTabResult ->
                when (item) {
                    is ChannelTabResult.VideoResult -> {
                        CommonVideoItem(
                            item = item.video,
                            onClick = {
                                coroutineScope.launch {
                                    bottomSheetState.expand()
                                }
                                channelViewModel.onMediaClicked(
                                    item.video
                                )
                            },
                            dropDownMenuClick = {
                                channelViewModel.getAllMyPlaylists()
                                selectedVideo = item
                                isShowingPlaylistDialog = true
                            }
                        )
                    }

                    is ChannelTabResult.PlaylistResult -> {
                        PlaylistItem(
                            playlist = item,
                            onClick = {
                                onNavigateToPlaylistInfoScreen(item.playlist.id)
                                channelViewModel.setPlaylistInfo(item.playlist)
                            }
                        )
                    }

                    is ChannelTabResult.ShortsResult -> {
                        ShortsVideoItem(video = item.video,
                            onClick = {
                                coroutineScope.launch {
                                    bottomSheetState.expand()
                                }
                                channelViewModel.onMediaClicked(
                                    item.video
                                )
                            })
                    }
                }
            },
            loadMore = loadMore
        )
    }
}



