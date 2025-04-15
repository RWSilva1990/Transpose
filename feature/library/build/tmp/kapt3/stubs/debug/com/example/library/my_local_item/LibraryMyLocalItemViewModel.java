package com.example.library.my_local_item;

import android.app.RecoverableSecurityException;
import android.os.Build;
import androidx.lifecycle.ViewModel;
import com.example.domain.repository.LocalFileRepository;
import com.example.domain.model.local_file.LocalFileData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0019\u001a\u00020\u001aJ\u000e\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\bJ\u0006\u0010\u001e\u001a\u00020\u001cJ\u0006\u0010\u001f\u001a\u00020\u001cJ\u0006\u0010 \u001a\u00020\u001cR\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0019\u0010\u0013\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0012R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0012R\u001d\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0012\u00a8\u0006!"}, d2 = {"Lcom/example/library/my_local_item/LibraryMyLocalItemViewModel;", "Landroidx/lifecycle/ViewModel;", "localFileRepository", "Lcom/example/domain/repository/LocalFileRepository;", "(Lcom/example/domain/repository/LocalFileRepository;)V", "_audioFiles", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/example/domain/model/local_file/LocalFileData;", "_errorMessage", "", "_pendingDeleteFile", "_recoverableDeleteEvent", "Landroid/app/RecoverableSecurityException;", "_videoFiles", "audioFiles", "Lkotlinx/coroutines/flow/StateFlow;", "getAudioFiles", "()Lkotlinx/coroutines/flow/StateFlow;", "errorMessage", "getErrorMessage", "recoverableDeleteEvent", "getRecoverableDeleteEvent", "videoFiles", "getVideoFiles", "clearRecoverableDeleteEvent", "", "deleteFile", "Lkotlinx/coroutines/Job;", "file", "loadAudioFiles", "loadVideoFiles", "retryDeleteAfterPermission", "library_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LibraryMyLocalItemViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.LocalFileRepository localFileRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.domain.model.local_file.LocalFileData>> _audioFiles = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.local_file.LocalFileData>> audioFiles = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.domain.model.local_file.LocalFileData>> _videoFiles = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.local_file.LocalFileData>> videoFiles = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _errorMessage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> errorMessage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<android.app.RecoverableSecurityException> _recoverableDeleteEvent = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<android.app.RecoverableSecurityException> recoverableDeleteEvent = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.domain.model.local_file.LocalFileData> _pendingDeleteFile = null;
    
    @javax.inject.Inject()
    public LibraryMyLocalItemViewModel(@org.jetbrains.annotations.NotNull()
    com.example.domain.repository.LocalFileRepository localFileRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.local_file.LocalFileData>> getAudioFiles() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.local_file.LocalFileData>> getVideoFiles() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getErrorMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<android.app.RecoverableSecurityException> getRecoverableDeleteEvent() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job loadAudioFiles() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job loadVideoFiles() {
        return null;
    }
    
    /**
     * 실제 파일 삭제 시도
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job deleteFile(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.local_file.LocalFileData file) {
        return null;
    }
    
    /**
     * 사용자가 시스템 다이얼로그에서 '허용'을 누른 뒤 재시도
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job retryDeleteAfterPermission() {
        return null;
    }
    
    /**
     * 사용자가 거부했거나, 이벤트를 클리어해야 하는 경우
     */
    public final void clearRecoverableDeleteEvent() {
    }
}