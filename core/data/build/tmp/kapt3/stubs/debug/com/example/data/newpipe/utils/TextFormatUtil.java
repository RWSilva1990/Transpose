package com.example.data.newpipe.utils;

import android.os.Build;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u000e\b\u00c7\u0002\u0018\u00002\u00020\u0001:\u0001\u001dB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001a\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\b\u001a\u00020\tJ-\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00060\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0002\u00a2\u0006\u0002\u0010\u0011J\u001f\u0010\u0012\u001a\u00020\u00062\b\u0010\u0013\u001a\u0004\u0018\u00010\f2\b\b\u0002\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\u0014J#\u0010\u0015\u001a\u00020\u00062\b\u0010\u0016\u001a\u0004\u0018\u00010\u00062\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00060\u000e\u00a2\u0006\u0002\u0010\u0018J!\u0010\u0019\u001a\u00020\u00062\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00060\u000e2\u0006\u0010\u001b\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u001cR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/example/data/newpipe/utils/TextFormatUtil;", "", "()V", "df", "Ljava/text/DecimalFormat;", "convertISOToPrettyTime", "", "isoDateString", "locale", "Ljava/util/Locale;", "format", "count", "", "stringArray", "", "isSubscriber", "", "(J[Ljava/lang/String;Z)Ljava/lang/String;", "formatTimestampToPrettyTime", "timestamp", "(Ljava/lang/Long;Ljava/util/Locale;)Ljava/lang/String;", "subscriberCountConverter", "subscriberCountString", "subscriberArray", "(Ljava/lang/String;[Ljava/lang/String;)Ljava/lang/String;", "viewCountCalculator", "viewCountStringArray", "viewCountString", "([Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", "CountUnit", "data_debug"})
public final class TextFormatUtil {
    @org.jetbrains.annotations.NotNull()
    private static final java.text.DecimalFormat df = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.data.newpipe.utils.TextFormatUtil INSTANCE = null;
    
    private TextFormatUtil() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String viewCountCalculator(@org.jetbrains.annotations.NotNull()
    java.lang.String[] viewCountStringArray, @org.jetbrains.annotations.NotNull()
    java.lang.String viewCountString) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String subscriberCountConverter(@org.jetbrains.annotations.Nullable()
    java.lang.String subscriberCountString, @org.jetbrains.annotations.NotNull()
    java.lang.String[] subscriberArray) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String convertISOToPrettyTime(@org.jetbrains.annotations.Nullable()
    java.lang.String isoDateString, @org.jetbrains.annotations.NotNull()
    java.util.Locale locale) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatTimestampToPrettyTime(@org.jetbrains.annotations.Nullable()
    java.lang.Long timestamp, @org.jetbrains.annotations.NotNull()
    java.util.Locale locale) {
        return null;
    }
    
    private final java.lang.String format(long count, java.lang.String[] stringArray, boolean isSubscriber) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u000e\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u001f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0007R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fj\u0002\b\rj\u0002\b\u000ej\u0002\b\u000fj\u0002\b\u0010j\u0002\b\u0011j\u0002\b\u0012\u00a8\u0006\u0013"}, d2 = {"Lcom/example/data/newpipe/utils/TextFormatUtil$CountUnit;", "", "value", "", "koreanIndex", "", "otherIndex", "(Ljava/lang/String;IJII)V", "getKoreanIndex", "()I", "getOtherIndex", "getValue", "()J", "THOUSAND", "TEN_THOUSAND", "HUNDRED_THOUSAND", "MILLION", "HUNDRED_MILLION", "BILLION", "data_debug"})
    public static enum CountUnit {
        /*public static final*/ THOUSAND /* = new THOUSAND(0L, 0, 0) */,
        /*public static final*/ TEN_THOUSAND /* = new TEN_THOUSAND(0L, 0, 0) */,
        /*public static final*/ HUNDRED_THOUSAND /* = new HUNDRED_THOUSAND(0L, 0, 0) */,
        /*public static final*/ MILLION /* = new MILLION(0L, 0, 0) */,
        /*public static final*/ HUNDRED_MILLION /* = new HUNDRED_MILLION(0L, 0, 0) */,
        /*public static final*/ BILLION /* = new BILLION(0L, 0, 0) */;
        private final long value = 0L;
        private final int koreanIndex = 0;
        private final int otherIndex = 0;
        
        CountUnit(long value, int koreanIndex, int otherIndex) {
        }
        
        public final long getValue() {
            return 0L;
        }
        
        public final int getKoreanIndex() {
            return 0;
        }
        
        public final int getOtherIndex() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.example.data.newpipe.utils.TextFormatUtil.CountUnit> getEntries() {
            return null;
        }
    }
}