package com.example.main;

import android.content.Context;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.media3.session.MediaController;
import com.example.domain.model.library.MyPlaylist;
import com.example.domain.model.preferences.RepeatMode;
import com.example.domain.model.youtube.video.Video;
import com.example.domain.model.youtube.video_detail.VideoDetail;
import com.example.domain.repository.ChannelRepository;
import com.example.domain.repository.MyPlaylistDBRepository;
import com.example.domain.repository.PlaybackPreferencesRepository;
import com.example.domain.repository.SuggestionKeywordRepository;
import com.example.domain.repository.VideoRepository;
import com.example.media.manager.AudioEffectsManager;
import com.example.media.manager.MediaPlaybackManager;
import com.example.util.Logger;
import com.example.util.PermissionUtils;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.FlowPreview;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\b\n\u0000\n\u0002\u0010\u000e\n\u0000\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0002"}, d2 = {"SEARCH_QUERY", "", "main_debug"})
public final class MainViewModelKt {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SEARCH_QUERY = "search_query";
}