package com.example.data.remote.repository;

import com.example.data.remote.mapper.SuggestionKeywordMapper;
import com.example.domain.repository.SuggestionKeywordRepository;
import com.example.data.remote.api.SuggestionKeywordApiService;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J*\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\t\u001a\u00020\bH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\n\u0010\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\f"}, d2 = {"Lcom/example/data/remote/repository/SuggestionKeywordRepositoryImpl;", "Lcom/example/domain/repository/SuggestionKeywordRepository;", "suggestionKeywordApiService", "Lcom/example/data/remote/api/SuggestionKeywordApiService;", "(Lcom/example/data/remote/api/SuggestionKeywordApiService;)V", "getSuggestionKeywords", "Lkotlin/Result;", "", "", "query", "getSuggestionKeywords-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class SuggestionKeywordRepositoryImpl implements com.example.domain.repository.SuggestionKeywordRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.data.remote.api.SuggestionKeywordApiService suggestionKeywordApiService = null;
    
    @javax.inject.Inject()
    public SuggestionKeywordRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.example.data.remote.api.SuggestionKeywordApiService suggestionKeywordApiService) {
        super();
    }
}