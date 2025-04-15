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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0002\b\u0005\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0019\u0010\u0003\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u0005\u0010\u0006R\u000e\u0010\b\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u000b\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\f\u0010\u0006R\u000e\u0010\r\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b!\u00a8\u0006\u000e"}, d2 = {"Lcom/example/main/components/bottomsheet/GraphicsLayerConstants;", "", "()V", "DEFAULT_HEIGHT", "Landroidx/compose/ui/unit/Dp;", "getDEFAULT_HEIGHT-D9Ej5fM", "()F", "F", "FULLY_EXPANDED", "", "MIN_SCALE", "PEEK_HEIGHT", "getPEEK_HEIGHT-D9Ej5fM", "SCALE_THRESHOLD", "main_debug"})
public final class GraphicsLayerConstants {
    public static final float FULLY_EXPANDED = 0.0F;
    public static final float SCALE_THRESHOLD = 0.2F;
    public static final float MIN_SCALE = 0.3F;
    private static final float PEEK_HEIGHT = 0.0F;
    private static final float DEFAULT_HEIGHT = 0.0F;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.main.components.bottomsheet.GraphicsLayerConstants INSTANCE = null;
    
    private GraphicsLayerConstants() {
        super();
    }
}