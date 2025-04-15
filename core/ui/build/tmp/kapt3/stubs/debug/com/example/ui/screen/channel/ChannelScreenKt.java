package com.example.ui.screen.channel;

import androidx.compose.foundation.lazy.LazyListState;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.SheetState;
import androidx.compose.material3.SheetValue;
import androidx.compose.material3.TabRowDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import com.example.domain.model.youtube.channel.ChannelDetail;
import com.example.domain.model.youtube.channel.ChannelTabResult;
import com.example.transpose.core.ui.R;
import com.example.ui.common.PaginatedState;
import com.example.util.Logger;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000`\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a6\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\tH\u0007\u001a\u00c9\u0001\u0010\n\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\u00052\u000e\u0010\f\u001a\n\u0012\u0004\u0012\u00020\u000e\u0018\u00010\r2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00142\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00050\u00162\u0012\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00140\u00182\u0012\u0010\u0019\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00050\u00182\u0012\u0010\u001a\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00010\t2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00010\u001c2\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\tH\u0003\u00a2\u0006\u0002\u0010!\u00a8\u0006\""}, d2 = {"ChannelScreen", "", "channelViewModel", "Lcom/example/ui/screen/channel/ChannelViewModel;", "channelId", "", "bottomSheetState", "Landroidx/compose/material3/SheetState;", "onNavigateToPlaylistInfoScreen", "Lkotlin/Function1;", "DisplayTabContent", "contentType", "contentState", "Lcom/example/ui/common/PaginatedState;", "Lcom/example/domain/model/youtube/channel/ChannelTabResult;", "channelDetail", "Lcom/example/domain/model/youtube/channel/ChannelDetail;", "isScrolled", "", "selectedTabIndex", "", "tabTitles", "", "actualToResourceIndex", "", "contentTypeMap", "onTabSelected", "loadMore", "Lkotlin/Function0;", "scrollState", "Landroidx/compose/foundation/lazy/LazyListState;", "coroutineScope", "Lkotlinx/coroutines/CoroutineScope;", "(Ljava/lang/String;Lcom/example/ui/common/PaginatedState;Lcom/example/domain/model/youtube/channel/ChannelDetail;ZI[Ljava/lang/String;Ljava/util/Map;Ljava/util/Map;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Landroidx/compose/foundation/lazy/LazyListState;Landroidx/compose/material3/SheetState;Lkotlinx/coroutines/CoroutineScope;Lcom/example/ui/screen/channel/ChannelViewModel;Lkotlin/jvm/functions/Function1;)V", "ui_debug"})
public final class ChannelScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ChannelScreen(@org.jetbrains.annotations.NotNull()
    com.example.ui.screen.channel.ChannelViewModel channelViewModel, @org.jetbrains.annotations.Nullable()
    java.lang.String channelId, @org.jetbrains.annotations.NotNull()
    androidx.compose.material3.SheetState bottomSheetState, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToPlaylistInfoScreen) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void DisplayTabContent(java.lang.String contentType, com.example.ui.common.PaginatedState<? extends com.example.domain.model.youtube.channel.ChannelTabResult> contentState, com.example.domain.model.youtube.channel.ChannelDetail channelDetail, boolean isScrolled, int selectedTabIndex, java.lang.String[] tabTitles, java.util.Map<java.lang.Integer, java.lang.Integer> actualToResourceIndex, java.util.Map<java.lang.Integer, java.lang.String> contentTypeMap, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onTabSelected, kotlin.jvm.functions.Function0<kotlin.Unit> loadMore, androidx.compose.foundation.lazy.LazyListState scrollState, androidx.compose.material3.SheetState bottomSheetState, kotlinx.coroutines.CoroutineScope coroutineScope, com.example.ui.screen.channel.ChannelViewModel channelViewModel, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToPlaylistInfoScreen) {
    }
}