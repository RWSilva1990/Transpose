package com.example.data.newpipe.repository.video;

import com.example.data.newpipe.mapper.InfoItemMapper;
import com.example.data.newpipe.repository.base.BaseNewPipeRepository;
import com.example.domain.model.youtube.video_detail.VideoDetail;
import com.example.domain.repository.VideoRepository;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u00012\u00020\u0002B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0003J$\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\t\u0010\n\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u000b"}, d2 = {"Lcom/example/data/newpipe/repository/video/VideoRepositoryImpl;", "Lcom/example/data/newpipe/repository/base/BaseNewPipeRepository;", "Lcom/example/domain/repository/VideoRepository;", "()V", "fetchVideoDetail", "Lkotlin/Result;", "Lcom/example/domain/model/youtube/video_detail/VideoDetail;", "videoId", "", "fetchVideoDetail-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class VideoRepositoryImpl extends com.example.data.newpipe.repository.base.BaseNewPipeRepository implements com.example.domain.repository.VideoRepository {
    
    @javax.inject.Inject()
    public VideoRepositoryImpl() {
        super();
    }
}