package com.example.ui.screen.search_result;

import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.SheetState;
import androidx.compose.material3.SheetValue;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import com.example.domain.model.youtube.search.SearchResult;
import com.example.transpose.core.ui.R;
import com.example.ui.common.PaginatedState;
import com.example.util.ToastUtil;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000$\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0007\u001a0\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\u00032\u0006\u0010\b\u001a\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u000bH\u0007\u00a8\u0006\f"}, d2 = {"ErrorMessage", "", "message", "", "SearchResultScreen", "searchResultViewModel", "Lcom/example/ui/screen/search_result/SearchResultViewModel;", "query", "bottomSheetState", "Landroidx/compose/material3/SheetState;", "navigateToBack", "Lkotlin/Function0;", "ui_debug"})
public final class SearchResultScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void SearchResultScreen(@org.jetbrains.annotations.NotNull()
    com.example.ui.screen.search_result.SearchResultViewModel searchResultViewModel, @org.jetbrains.annotations.Nullable()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    androidx.compose.material3.SheetState bottomSheetState, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> navigateToBack) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ErrorMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
}