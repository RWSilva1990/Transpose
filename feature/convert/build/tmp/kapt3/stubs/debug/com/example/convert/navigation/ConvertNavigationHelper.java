package com.example.convert.navigation;

import androidx.navigation.NavController;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\n\u001a\u00020\u000bJ\u0006\u0010\f\u001a\u00020\u000bJ\u000e\u0010\r\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\u000fJ\u000e\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u000fR\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/example/convert/navigation/ConvertNavigationHelper;", "", "navController", "Landroidx/navigation/NavController;", "(Landroidx/navigation/NavController;)V", "canGoBack", "Lkotlin/Function0;", "", "getCanGoBack", "()Lkotlin/jvm/functions/Function0;", "navigateBack", "", "navigateToAudioEdit", "navigateToPlaylistInfo", "playlistId", "", "navigateToSearchResult", "query", "convert_debug"})
public final class ConvertNavigationHelper {
    @org.jetbrains.annotations.NotNull()
    private final androidx.navigation.NavController navController = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function0<java.lang.Boolean> canGoBack = null;
    
    public ConvertNavigationHelper(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.jvm.functions.Function0<java.lang.Boolean> getCanGoBack() {
        return null;
    }
    
    /**
     * Convert 탭 -> 오디오 편집 화면
     */
    public final void navigateToAudioEdit() {
    }
    
    /**
     * Convert 탭 -> 검색 결과
     */
    public final void navigateToSearchResult(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
    }
    
    public final void navigateToPlaylistInfo(@org.jetbrains.annotations.NotNull()
    java.lang.String playlistId) {
    }
    
    public final void navigateBack() {
    }
}