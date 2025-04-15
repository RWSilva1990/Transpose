package com.example.ui.components.scrollbar;

import androidx.compose.foundation.lazy.LazyListState;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import com.example.util.Logger;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000H\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a\u00b7\u0001\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00072\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u0002H\u00030\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u0001H\u00022\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u0002H\u0003\u0012\u0004\u0012\u00020\u00100\u000f2\u001b\b\u0002\u0010\u0011\u001a\u0015\u0012\u0004\u0012\u0002H\u0002\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000f\u00a2\u0006\u0002\b\u00122\u001d\u0010\u0013\u001a\u0019\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u0002H\u0003\u0012\u0004\u0012\u00020\u00010\u0014\u00a2\u0006\u0002\b\u00122\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00010\u0017H\u0007\u00a2\u0006\u0002\u0010\u0018\u001a\f\u0010\u0019\u001a\u00020\u0007*\u00020\tH\u0002\u00a8\u0006\u001a"}, d2 = {"EndlessLazyColumn", "", "H", "T", "modifier", "Landroidx/compose/ui/Modifier;", "loading", "", "listState", "Landroidx/compose/foundation/lazy/LazyListState;", "hasMoreItems", "items", "", "headerData", "itemKey", "Lkotlin/Function1;", "", "headerContent", "Landroidx/compose/runtime/Composable;", "itemContent", "Lkotlin/Function2;", "", "loadMore", "Lkotlin/Function0;", "(Landroidx/compose/ui/Modifier;ZLandroidx/compose/foundation/lazy/LazyListState;ZLjava/util/List;Ljava/lang/Object;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function2;Lkotlin/jvm/functions/Function0;)V", "reachedBottom", "ui_debug"})
public final class EndlessLazyColumnKt {
    
    @androidx.compose.runtime.Composable()
    public static final <H extends java.lang.Object, T extends java.lang.Object>void EndlessLazyColumn(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, boolean loading, @org.jetbrains.annotations.NotNull()
    androidx.compose.foundation.lazy.LazyListState listState, boolean hasMoreItems, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends T> items, @org.jetbrains.annotations.Nullable()
    H headerData, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super T, ? extends java.lang.Object> itemKey, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super H, kotlin.Unit> headerContent, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super T, kotlin.Unit> itemContent, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> loadMore) {
    }
    
    private static final boolean reachedBottom(androidx.compose.foundation.lazy.LazyListState $this$reachedBottom) {
        return false;
    }
}