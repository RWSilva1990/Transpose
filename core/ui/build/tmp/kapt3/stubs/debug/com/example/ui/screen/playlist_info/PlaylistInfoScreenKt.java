package com.example.ui.screen.playlist_info;

import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.SheetState;
import androidx.compose.material3.SheetValue;
import androidx.compose.runtime.Composable;
import com.example.domain.model.youtube.playlist.PlaylistItem;
import com.example.domain.model.youtube.video.Video;
import com.example.ui.common.PaginatedState;
import com.example.util.Logger;
import com.example.util.ToastUtil;
import com.example.transpose.core.ui.R;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u001e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0007\u001a\"\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\u0003H\u0007\u00a8\u0006\n"}, d2 = {"ErrorMessage", "", "message", "", "PlaylistInfoScreen", "bottomSheetState", "Landroidx/compose/material3/SheetState;", "playlistInfoViewModel", "Lcom/example/ui/screen/playlist_info/PlaylistInfoViewModel;", "playlistId", "ui_debug"})
public final class PlaylistInfoScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void PlaylistInfoScreen(@org.jetbrains.annotations.NotNull()
    androidx.compose.material3.SheetState bottomSheetState, @org.jetbrains.annotations.NotNull()
    com.example.ui.screen.playlist_info.PlaylistInfoViewModel playlistInfoViewModel, @org.jetbrains.annotations.Nullable()
    java.lang.String playlistId) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ErrorMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
}