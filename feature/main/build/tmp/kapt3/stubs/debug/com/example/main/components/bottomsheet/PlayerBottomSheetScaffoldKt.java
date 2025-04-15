package com.example.main.components.bottomsheet;

import android.graphics.Rect;
import android.view.ViewTreeObserver;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.SheetState;
import androidx.compose.material3.SheetValue;
import androidx.compose.runtime.Composable;
import androidx.compose.runtime.State;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.unit.Dp;
import com.example.main.BuildConfig;
import com.example.main.MainViewModel;
import com.example.main.components.appbar.SearchWidgetState;
import com.example.util.Logger;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000P\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\u001a\u0088\u0001\u0010\u0000\u001a\u00020\u00012\u0015\b\u0002\u0010\u0002\u001a\u000f\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0003\u00a2\u0006\u0002\b\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0012\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010\f2\u0017\u0010\u0013\u001a\u0013\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00010\f\u00a2\u0006\u0002\b\u0004H\u0007\u001a(\u0010\u0014\u001a\u00020\n2\u0006\u0010\u0015\u001a\u00020\n2\u0006\u0010\u0016\u001a\u00020\n2\u0006\u0010\u0017\u001a\u00020\n2\u0006\u0010\u0018\u001a\u00020\nH\u0002\u001a\r\u0010\u0019\u001a\u00020\u001aH\u0007\u00a2\u0006\u0002\u0010\u001b\u001a\u000e\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001e0\u001dH\u0007\u00a8\u0006\u001f"}, d2 = {"PlayerBottomSheetScaffold", "", "topAppBar", "Lkotlin/Function0;", "Landroidx/compose/runtime/Composable;", "mainViewModel", "Lcom/example/main/MainViewModel;", "bottomSheetState", "Landroidx/compose/material3/SheetState;", "normalizedOffset", "", "setNormalizedOffset", "Lkotlin/Function1;", "searchWidgetState", "Lcom/example/main/components/appbar/SearchWidgetState;", "innerPadding", "Landroidx/compose/foundation/layout/PaddingValues;", "onNavigateToChannelScreen", "", "content", "calculateDragProgress", "currentOffset", "expandedOffset", "partiallyExpandedOffset", "hiddenOffset", "getNavigationBarHeightDp", "Landroidx/compose/ui/unit/Dp;", "()F", "keyboardAsState", "Landroidx/compose/runtime/State;", "", "main_debug"})
public final class PlayerBottomSheetScaffoldKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void PlayerBottomSheetScaffold(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function0<kotlin.Unit> topAppBar, @org.jetbrains.annotations.NotNull()
    com.example.main.MainViewModel mainViewModel, @org.jetbrains.annotations.NotNull()
    androidx.compose.material3.SheetState bottomSheetState, float normalizedOffset, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> setNormalizedOffset, @org.jetbrains.annotations.NotNull()
    com.example.main.components.appbar.SearchWidgetState searchWidgetState, @org.jetbrains.annotations.NotNull()
    androidx.compose.foundation.layout.PaddingValues innerPadding, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToChannelScreen, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super androidx.compose.foundation.layout.PaddingValues, kotlin.Unit> content) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    private static final float calculateDragProgress(float currentOffset, float expandedOffset, float partiallyExpandedOffset, float hiddenOffset) {
        return 0.0F;
    }
    
    @androidx.compose.runtime.Composable()
    public static final float getNavigationBarHeightDp() {
        return 0.0F;
    }
    
    @androidx.compose.runtime.Composable()
    @org.jetbrains.annotations.NotNull()
    public static final androidx.compose.runtime.State<java.lang.Boolean> keyboardAsState() {
        return null;
    }
}