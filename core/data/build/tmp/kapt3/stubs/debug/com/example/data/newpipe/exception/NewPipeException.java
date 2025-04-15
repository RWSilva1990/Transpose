package com.example.data.newpipe.exception;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b7\u0018\u00002\u00060\u0001j\u0002`\u0002:\t\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013B\u001b\b\u0004\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0007R\u0012\u0010\b\u001a\u00020\u0004X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\n\u0082\u0001\t\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u00a8\u0006\u001d"}, d2 = {"Lcom/example/data/newpipe/exception/NewPipeException;", "Ljava/lang/Exception;", "Lkotlin/Exception;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "methodName", "getMethodName", "()Ljava/lang/String;", "ExtractionFailed", "InvalidPlaylistIdOrUrl", "NetworkError", "PageCannotBeLoaded", "ParsingException", "PlaylistItemsFetchFailed", "PlaylistNotFound", "UnknownError", "UnsupportedOperationException", "Lcom/example/data/newpipe/exception/NewPipeException$ExtractionFailed;", "Lcom/example/data/newpipe/exception/NewPipeException$InvalidPlaylistIdOrUrl;", "Lcom/example/data/newpipe/exception/NewPipeException$NetworkError;", "Lcom/example/data/newpipe/exception/NewPipeException$PageCannotBeLoaded;", "Lcom/example/data/newpipe/exception/NewPipeException$ParsingException;", "Lcom/example/data/newpipe/exception/NewPipeException$PlaylistItemsFetchFailed;", "Lcom/example/data/newpipe/exception/NewPipeException$PlaylistNotFound;", "Lcom/example/data/newpipe/exception/NewPipeException$UnknownError;", "Lcom/example/data/newpipe/exception/NewPipeException$UnsupportedOperationException;", "data_debug"})
public abstract class NewPipeException extends java.lang.Exception {
    
    private NewPipeException(java.lang.String message, java.lang.Throwable cause) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String getMethodName();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006R\u0014\u0010\u0002\u001a\u00020\u0003X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/example/data/newpipe/exception/NewPipeException$ExtractionFailed;", "Lcom/example/data/newpipe/exception/NewPipeException;", "methodName", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "getMethodName", "()Ljava/lang/String;", "data_debug"})
    public static final class ExtractionFailed extends com.example.data.newpipe.exception.NewPipeException {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String methodName = null;
        
        public ExtractionFailed(@org.jetbrains.annotations.NotNull()
        java.lang.String methodName, @org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String getMethodName() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0003\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B!\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0007R\u0014\u0010\u0002\u001a\u00020\u0003X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006\n"}, d2 = {"Lcom/example/data/newpipe/exception/NewPipeException$InvalidPlaylistIdOrUrl;", "Lcom/example/data/newpipe/exception/NewPipeException;", "methodName", "", "playlistId", "cause", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V", "getMethodName", "()Ljava/lang/String;", "data_debug"})
    public static final class InvalidPlaylistIdOrUrl extends com.example.data.newpipe.exception.NewPipeException {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String methodName = null;
        
        public InvalidPlaylistIdOrUrl(@org.jetbrains.annotations.NotNull()
        java.lang.String methodName, @org.jetbrains.annotations.NotNull()
        java.lang.String playlistId, @org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String getMethodName() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006R\u0014\u0010\u0002\u001a\u00020\u0003X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/example/data/newpipe/exception/NewPipeException$NetworkError;", "Lcom/example/data/newpipe/exception/NewPipeException;", "methodName", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "getMethodName", "()Ljava/lang/String;", "data_debug"})
    public static final class NetworkError extends com.example.data.newpipe.exception.NewPipeException {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String methodName = null;
        
        public NetworkError(@org.jetbrains.annotations.NotNull()
        java.lang.String methodName, @org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String getMethodName() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006R\u0014\u0010\u0002\u001a\u00020\u0003X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/example/data/newpipe/exception/NewPipeException$PageCannotBeLoaded;", "Lcom/example/data/newpipe/exception/NewPipeException;", "methodName", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "getMethodName", "()Ljava/lang/String;", "data_debug"})
    public static final class PageCannotBeLoaded extends com.example.data.newpipe.exception.NewPipeException {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String methodName = null;
        
        public PageCannotBeLoaded(@org.jetbrains.annotations.NotNull()
        java.lang.String methodName, @org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String getMethodName() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006R\u0014\u0010\u0002\u001a\u00020\u0003X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/example/data/newpipe/exception/NewPipeException$ParsingException;", "Lcom/example/data/newpipe/exception/NewPipeException;", "methodName", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "getMethodName", "()Ljava/lang/String;", "data_debug"})
    public static final class ParsingException extends com.example.data.newpipe.exception.NewPipeException {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String methodName = null;
        
        public ParsingException(@org.jetbrains.annotations.NotNull()
        java.lang.String methodName, @org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String getMethodName() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006R\u0014\u0010\u0002\u001a\u00020\u0003X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/example/data/newpipe/exception/NewPipeException$PlaylistItemsFetchFailed;", "Lcom/example/data/newpipe/exception/NewPipeException;", "methodName", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "getMethodName", "()Ljava/lang/String;", "data_debug"})
    public static final class PlaylistItemsFetchFailed extends com.example.data.newpipe.exception.NewPipeException {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String methodName = null;
        
        public PlaylistItemsFetchFailed(@org.jetbrains.annotations.NotNull()
        java.lang.String methodName, @org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String getMethodName() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006R\u0014\u0010\u0002\u001a\u00020\u0003X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/example/data/newpipe/exception/NewPipeException$PlaylistNotFound;", "Lcom/example/data/newpipe/exception/NewPipeException;", "methodName", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "getMethodName", "()Ljava/lang/String;", "data_debug"})
    public static final class PlaylistNotFound extends com.example.data.newpipe.exception.NewPipeException {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String methodName = null;
        
        public PlaylistNotFound(@org.jetbrains.annotations.NotNull()
        java.lang.String methodName, @org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String getMethodName() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006R\u0014\u0010\u0002\u001a\u00020\u0003X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/example/data/newpipe/exception/NewPipeException$UnknownError;", "Lcom/example/data/newpipe/exception/NewPipeException;", "methodName", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "getMethodName", "()Ljava/lang/String;", "data_debug"})
    public static final class UnknownError extends com.example.data.newpipe.exception.NewPipeException {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String methodName = null;
        
        public UnknownError(@org.jetbrains.annotations.NotNull()
        java.lang.String methodName, @org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String getMethodName() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006R\u0014\u0010\u0002\u001a\u00020\u0003X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/example/data/newpipe/exception/NewPipeException$UnsupportedOperationException;", "Lcom/example/data/newpipe/exception/NewPipeException;", "methodName", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "getMethodName", "()Ljava/lang/String;", "data_debug"})
    public static final class UnsupportedOperationException extends com.example.data.newpipe.exception.NewPipeException {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String methodName = null;
        
        public UnsupportedOperationException(@org.jetbrains.annotations.NotNull()
        java.lang.String methodName, @org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String getMethodName() {
            return null;
        }
    }
}