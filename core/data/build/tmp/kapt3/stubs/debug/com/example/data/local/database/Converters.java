package com.example.data.local.database;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.stream.StreamType;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u0007J\u0010\u0010\b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\tH\u0007J\u0016\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\u0005\u001a\u00020\u0004H\u0007J\u0010\u0010\u000b\u001a\u00020\t2\u0006\u0010\u0005\u001a\u00020\u0004H\u0007\u00a8\u0006\f"}, d2 = {"Lcom/example/data/local/database/Converters;", "", "()V", "fromImageList", "", "value", "", "Lorg/schabi/newpipe/extractor/Image;", "fromStreamType", "Lorg/schabi/newpipe/extractor/stream/StreamType;", "toImageList", "toStreamType", "data_debug"})
public final class Converters {
    
    public Converters() {
        super();
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String fromImageList(@org.jetbrains.annotations.NotNull()
    java.util.List<org.schabi.newpipe.extractor.Image> value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<org.schabi.newpipe.extractor.Image> toImageList(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String fromStreamType(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.stream.StreamType value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final org.schabi.newpipe.extractor.stream.StreamType toStreamType(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
        return null;
    }
}