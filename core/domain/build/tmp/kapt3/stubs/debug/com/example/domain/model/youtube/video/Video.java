package com.example.domain.model.youtube.video;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b1\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B\u008d\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\t\u001a\u00020\n\u0012\b\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\r\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u0012\u0006\u0010\u0010\u001a\u00020\b\u0012\u0006\u0010\u0011\u001a\u00020\b\u0012\b\u0010\u0012\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0013\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0014\u001a\u00020\u000f\u00a2\u0006\u0002\u0010\u0015J\t\u0010-\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010.\u001a\u0004\u0018\u00010\u000fH\u00c6\u0003\u00a2\u0006\u0002\u0010*J\t\u0010/\u001a\u00020\bH\u00c6\u0003J\t\u00100\u001a\u00020\bH\u00c6\u0003J\u000b\u00101\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00102\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u00103\u001a\u00020\u000fH\u00c6\u0003J\t\u00104\u001a\u00020\u0003H\u00c6\u0003J\u000b\u00105\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u00106\u001a\u00020\u0003H\u00c6\u0003J\u0010\u00107\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u001eJ\t\u00108\u001a\u00020\nH\u00c6\u0003J\u000b\u00109\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010:\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010;\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u00b4\u0001\u0010<\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\t\u001a\u00020\n2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\b2\b\b\u0002\u0010\u0011\u001a\u00020\b2\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0014\u001a\u00020\u000fH\u00c6\u0001\u00a2\u0006\u0002\u0010=J\u0013\u0010>\u001a\u00020\u000f2\b\u0010?\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010@\u001a\u00020AH\u00d6\u0001J\t\u0010B\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0010\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0017R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0015\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u001f\u001a\u0004\b\u001d\u0010\u001eR\u0011\u0010\u0014\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0013\u0010\u0013\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0017R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u0017R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u0017R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0017R\u0013\u0010\r\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u0017R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u0017R\u0013\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u0017R\u0015\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\n\n\u0002\u0010+\u001a\u0004\b)\u0010*R\u0011\u0010\u0011\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u0019\u00a8\u0006C"}, d2 = {"Lcom/example/domain/model/youtube/video/Video;", "", "id", "", "title", "thumbnailUrl", "description", "publishTimestamp", "", "infoType", "Lorg/schabi/newpipe/extractor/InfoItem$InfoType;", "uploaderName", "uploaderUrl", "uploaderAvatarUrl", "uploaderVerified", "", "duration", "viewCount", "textualUploadDate", "streamType", "shortFormContent", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;Lorg/schabi/newpipe/extractor/InfoItem$InfoType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;JJLjava/lang/String;Ljava/lang/String;Z)V", "getDescription", "()Ljava/lang/String;", "getDuration", "()J", "getId", "getInfoType", "()Lorg/schabi/newpipe/extractor/InfoItem$InfoType;", "getPublishTimestamp", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getShortFormContent", "()Z", "getStreamType", "getTextualUploadDate", "getThumbnailUrl", "getTitle", "getUploaderAvatarUrl", "getUploaderName", "getUploaderUrl", "getUploaderVerified", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getViewCount", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;Lorg/schabi/newpipe/extractor/InfoItem$InfoType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;JJLjava/lang/String;Ljava/lang/String;Z)Lcom/example/domain/model/youtube/video/Video;", "equals", "other", "hashCode", "", "toString", "domain_debug"})
public final class Video {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String thumbnailUrl = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String description = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Long publishTimestamp = null;
    @org.jetbrains.annotations.NotNull()
    private final org.schabi.newpipe.extractor.InfoItem.InfoType infoType = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String uploaderName = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String uploaderUrl = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String uploaderAvatarUrl = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean uploaderVerified = null;
    private final long duration = 0L;
    private final long viewCount = 0L;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String textualUploadDate = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String streamType = null;
    private final boolean shortFormContent = false;
    
    public Video(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.Nullable()
    java.lang.String thumbnailUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.Nullable()
    java.lang.Long publishTimestamp, @org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.InfoItem.InfoType infoType, @org.jetbrains.annotations.Nullable()
    java.lang.String uploaderName, @org.jetbrains.annotations.Nullable()
    java.lang.String uploaderUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String uploaderAvatarUrl, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean uploaderVerified, long duration, long viewCount, @org.jetbrains.annotations.Nullable()
    java.lang.String textualUploadDate, @org.jetbrains.annotations.Nullable()
    java.lang.String streamType, boolean shortFormContent) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getThumbnailUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDescription() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getPublishTimestamp() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final org.schabi.newpipe.extractor.InfoItem.InfoType getInfoType() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUploaderName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUploaderUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUploaderAvatarUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getUploaderVerified() {
        return null;
    }
    
    public final long getDuration() {
        return 0L;
    }
    
    public final long getViewCount() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getTextualUploadDate() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getStreamType() {
        return null;
    }
    
    public final boolean getShortFormContent() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component10() {
        return null;
    }
    
    public final long component11() {
        return 0L;
    }
    
    public final long component12() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component14() {
        return null;
    }
    
    public final boolean component15() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final org.schabi.newpipe.extractor.InfoItem.InfoType component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.video.Video copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.Nullable()
    java.lang.String thumbnailUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.Nullable()
    java.lang.Long publishTimestamp, @org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.InfoItem.InfoType infoType, @org.jetbrains.annotations.Nullable()
    java.lang.String uploaderName, @org.jetbrains.annotations.Nullable()
    java.lang.String uploaderUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String uploaderAvatarUrl, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean uploaderVerified, long duration, long viewCount, @org.jetbrains.annotations.Nullable()
    java.lang.String textualUploadDate, @org.jetbrains.annotations.Nullable()
    java.lang.String streamType, boolean shortFormContent) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}