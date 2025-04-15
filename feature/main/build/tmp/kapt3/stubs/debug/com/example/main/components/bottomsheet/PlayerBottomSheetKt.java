package com.example.main.components.bottomsheet;

import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.SheetState;
import androidx.compose.material3.SheetValue;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection;
import androidx.compose.ui.input.nestedscroll.NestedScrollSource;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.constraintlayout.compose.Dimension;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import com.example.main.MainViewModel;
import com.example.main.R;
import com.example.util.constants.AppColors;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000.\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a4\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00010\tH\u0007\u001a\u0010\u0010\u000b\u001a\u00020\u00072\u0006\u0010\u0006\u001a\u00020\u0007H\u0002\u001a\u0010\u0010\f\u001a\u00020\u00072\u0006\u0010\u0006\u001a\u00020\u0007H\u0002\u001a\u0014\u0010\r\u001a\u00020\u000e*\u00020\u000e2\u0006\u0010\u0006\u001a\u00020\u0007H\u0002\u001a\u0014\u0010\u000f\u001a\u00020\u000e*\u00020\u000e2\u0006\u0010\u0006\u001a\u00020\u0007H\u0002\u00a8\u0006\u0010"}, d2 = {"PlayerBottomSheet", "", "mainViewModel", "Lcom/example/main/MainViewModel;", "bottomSheetState", "Landroidx/compose/material3/SheetState;", "normalizedOffset", "", "onNavigateToChannelScreen", "Lkotlin/Function1;", "", "calculateDefaultScaleX", "calculateScaleFactorY", "bottomSheetAlpha", "Landroidx/compose/ui/Modifier;", "changeMainBackgroundAlpha", "main_debug"})
public final class PlayerBottomSheetKt {
    
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void PlayerBottomSheet(@org.jetbrains.annotations.NotNull()
    com.example.main.MainViewModel mainViewModel, @org.jetbrains.annotations.NotNull()
    androidx.compose.material3.SheetState bottomSheetState, float normalizedOffset, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToChannelScreen) {
    }
    
    private static final androidx.compose.ui.Modifier bottomSheetAlpha(androidx.compose.ui.Modifier $this$bottomSheetAlpha, float normalizedOffset) {
        return null;
    }
    
    private static final androidx.compose.ui.Modifier changeMainBackgroundAlpha(androidx.compose.ui.Modifier $this$changeMainBackgroundAlpha, float normalizedOffset) {
        return null;
    }
    
    private static final float calculateDefaultScaleX(float normalizedOffset) {
        return 0.0F;
    }
    
    private static final float calculateScaleFactorY(float normalizedOffset) {
        return 0.0F;
    }
}