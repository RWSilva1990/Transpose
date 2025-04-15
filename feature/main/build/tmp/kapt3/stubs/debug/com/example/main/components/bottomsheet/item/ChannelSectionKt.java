package com.example.main.components.bottomsheet.item;

import androidx.compose.material3.ButtonDefaults;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.SheetState;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import com.example.domain.model.youtube.video.Video;
import com.example.domain.model.youtube.video_detail.VideoDetail;
import com.example.main.MainViewModel;
import com.example.main.R;
import com.example.util.TextFormatUtil;
import com.example.util.ToastUtil;
import com.example.util.constants.AppColors;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000,\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\u001a@\u0010\u0000\u001a\u00020\u00012\b\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0012\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00010\u000bH\u0007\u001aT\u0010\r\u001a\u00020\u00012\b\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u000b2\u0006\u0010\b\u001a\u00020\t2\u0012\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00010\u000bH\u0007\u001a\b\u0010\u000f\u001a\u00020\u0001H\u0007\u00a8\u0006\u0010"}, d2 = {"ChannelSection", "", "currentVideoData", "Lcom/example/domain/model/youtube/video/Video;", "currentVideoDetail", "Lcom/example/domain/model/youtube/video_detail/VideoDetail;", "mainViewModel", "Lcom/example/main/MainViewModel;", "bottomSheetState", "Landroidx/compose/material3/SheetState;", "onNavigateToChannelScreen", "Lkotlin/Function1;", "", "ChannelSectionContent", "onAddButtonClicked", "ChannelSectionShimmer", "main_debug"})
public final class ChannelSectionKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ChannelSection(@org.jetbrains.annotations.Nullable()
    com.example.domain.model.youtube.video.Video currentVideoData, @org.jetbrains.annotations.Nullable()
    com.example.domain.model.youtube.video_detail.VideoDetail currentVideoDetail, @org.jetbrains.annotations.NotNull()
    com.example.main.MainViewModel mainViewModel, @org.jetbrains.annotations.NotNull()
    androidx.compose.material3.SheetState bottomSheetState, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToChannelScreen) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ChannelSectionContent(@org.jetbrains.annotations.Nullable()
    com.example.domain.model.youtube.video.Video currentVideoData, @org.jetbrains.annotations.Nullable()
    com.example.domain.model.youtube.video_detail.VideoDetail currentVideoDetail, @org.jetbrains.annotations.NotNull()
    com.example.main.MainViewModel mainViewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.domain.model.youtube.video.Video, kotlin.Unit> onAddButtonClicked, @org.jetbrains.annotations.NotNull()
    androidx.compose.material3.SheetState bottomSheetState, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToChannelScreen) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ChannelSectionShimmer() {
    }
}