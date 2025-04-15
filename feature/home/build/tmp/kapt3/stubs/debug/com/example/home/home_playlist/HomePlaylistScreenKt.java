package com.example.home.home_playlist;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.SheetState;
import androidx.compose.material3.SheetValue;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import com.example.domain.model.youtube.playlist.Playlist;
import com.example.transpose.feature.home.R;
import com.example.ui.common.UiState;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000L\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a&\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001aR\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00030\u00072\b\b\u0002\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a=\u0010\u0013\u001a\u00020\u00012\u0006\u0010\u0014\u001a\u00020\u00052\u0012\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00180\u00170\u00162\u0017\u0010\u0019\u001a\u0013\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u00010\u000e\u00a2\u0006\u0002\b\u001aH\u0007\u00a8\u0006\u001b"}, d2 = {"ErrorMessage", "", "isVisible", "", "message", "", "onRefresh", "Lkotlin/Function0;", "HomePlaylistScreen", "bottomSheetState", "Landroidx/compose/material3/SheetState;", "homePlaylistViewModel", "Lcom/example/home/home_playlist/HomePlaylistViewModel;", "navigateToPlaylistItemScreen", "Lkotlin/Function1;", "canGoBack", "modifier", "Landroidx/compose/ui/Modifier;", "navigateToBack", "PlaylistSection", "title", "playlistState", "Lcom/example/ui/common/UiState;", "", "Lcom/example/domain/model/youtube/playlist/Playlist;", "itemContent", "Landroidx/compose/runtime/Composable;", "home_debug"})
public final class HomePlaylistScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void HomePlaylistScreen(@org.jetbrains.annotations.NotNull()
    androidx.compose.material3.SheetState bottomSheetState, @org.jetbrains.annotations.NotNull()
    com.example.home.home_playlist.HomePlaylistViewModel homePlaylistViewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> navigateToPlaylistItemScreen, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<java.lang.Boolean> canGoBack, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> navigateToBack) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void PlaylistSection(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    com.example.ui.common.UiState<? extends java.util.List<com.example.domain.model.youtube.playlist.Playlist>> playlistState, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.domain.model.youtube.playlist.Playlist, kotlin.Unit> itemContent) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ErrorMessage(boolean isVisible, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRefresh) {
    }
}