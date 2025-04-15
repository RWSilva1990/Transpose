package com.example.library.navigation;

import androidx.navigation.NavController;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tJ\u0006\u0010\n\u001a\u00020\u0006J\u000e\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\rJ\u000e\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\tJ\u000e\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/example/library/navigation/LibraryNavigationHelper;", "", "navController", "Landroidx/navigation/NavController;", "(Landroidx/navigation/NavController;)V", "navigateBack", "", "navigateToMyLocalFileItem", "type", "", "navigateToMyPlaylist", "navigateToMyPlaylistItem", "itemId", "", "navigateToPlaylistInfo", "playlistId", "navigateToSearchResult", "query", "library_debug"})
public final class LibraryNavigationHelper {
    @org.jetbrains.annotations.NotNull()
    private final androidx.navigation.NavController navController = null;
    
    public LibraryNavigationHelper(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController) {
        super();
    }
    
    /**
     * 라이브러리 탭 -> 내 플레이리스트 화면
     */
    public final void navigateToMyPlaylist() {
    }
    
    /**
     * 라이브러리 탭 -> 내 플레이리스트 아이템 상세
     */
    public final void navigateToMyPlaylistItem(long itemId) {
    }
    
    /**
     * 라이브러리 탭 -> 검색 결과
     */
    public final void navigateToSearchResult(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
    }
    
    /**
     * 라이브러리 탭 -> 내 로컬 파일 화면
     */
    public final void navigateToMyLocalFileItem(@org.jetbrains.annotations.NotNull()
    java.lang.String type) {
    }
    
    public final void navigateToPlaylistInfo(@org.jetbrains.annotations.NotNull()
    java.lang.String playlistId) {
    }
    
    public final void navigateBack() {
    }
}