package com.example.data.newpipe.repository.search;

import com.example.data.newpipe.extractor.search.ContentPager;
import com.example.data.newpipe.repository.base.BaseNewPipeRepository;
import com.example.domain.model.youtube.search.SearchResult;
import com.example.domain.repository.SearchRepository;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u00012\u00020\u0002B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u0006\u001a\u00020\u0007H\u0016J\"\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\tH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\f\u0010\rJ*\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\t2\u0006\u0010\u000f\u001a\u00020\u0010H\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0011\u0010\u0012R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0013"}, d2 = {"Lcom/example/data/newpipe/repository/search/SearchRepositoryImpl;", "Lcom/example/data/newpipe/repository/base/BaseNewPipeRepository;", "Lcom/example/domain/repository/SearchRepository;", "()V", "currentContentPager", "Lcom/example/data/newpipe/extractor/search/ContentPager;", "canLoadMoreSearchResults", "", "loadMoreSearchResults", "Lkotlin/Result;", "", "Lcom/example/domain/model/youtube/search/SearchResult;", "loadMoreSearchResults-IoAF18A", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "search", "query", "", "search-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class SearchRepositoryImpl extends com.example.data.newpipe.repository.base.BaseNewPipeRepository implements com.example.domain.repository.SearchRepository {
    @org.jetbrains.annotations.Nullable()
    private com.example.data.newpipe.extractor.search.ContentPager currentContentPager;
    
    @javax.inject.Inject()
    public SearchRepositoryImpl() {
        super();
    }
    
    @java.lang.Override()
    public boolean canLoadMoreSearchResults() {
        return false;
    }
}