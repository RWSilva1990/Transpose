package com.example.data.local.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.data.local.database.entity.VideoEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@SuppressWarnings({"unchecked", "deprecation"})
public final class VideoDao_Impl implements VideoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VideoEntity> __insertionAdapterOfVideoEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteVideoById;

  public VideoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVideoEntity = new EntityInsertionAdapter<VideoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `videos` (`id`,`playlistId`,`title`,`description`,`publishTimestamp`,`thumbnailUrl`,`uploaderName`,`uploaderUrl`,`uploaderAvatarUrl`,`uploaderVerified`,`duration`,`viewCount`,`textualUploadDate`,`streamType`,`shortFormContent`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VideoEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        statement.bindLong(2, entity.getPlaylistId());
        if (entity.getTitle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTitle());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        if (entity.getPublishTimestamp() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getPublishTimestamp());
        }
        if (entity.getThumbnailUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getThumbnailUrl());
        }
        if (entity.getUploaderName() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getUploaderName());
        }
        if (entity.getUploaderUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getUploaderUrl());
        }
        if (entity.getUploaderAvatarUrl() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getUploaderAvatarUrl());
        }
        final int _tmp = entity.getUploaderVerified() ? 1 : 0;
        statement.bindLong(10, _tmp);
        statement.bindLong(11, entity.getDuration());
        statement.bindLong(12, entity.getViewCount());
        if (entity.getTextualUploadDate() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getTextualUploadDate());
        }
        if (entity.getStreamType() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getStreamType());
        }
        final int _tmp_1 = entity.getShortFormContent() ? 1 : 0;
        statement.bindLong(15, _tmp_1);
      }
    };
    this.__preparedStmtOfDeleteVideoById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM videos WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertVideo(final VideoEntity video, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfVideoEntity.insert(video);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteVideoById(final String videoId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteVideoById.acquire();
        int _argIndex = 1;
        if (videoId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, videoId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteVideoById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getVideosForPlaylist(final long playlistId,
      final Continuation<? super List<VideoEntity>> $completion) {
    final String _sql = "SELECT * FROM videos WHERE playlistId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, playlistId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<VideoEntity>>() {
      @Override
      @NonNull
      public List<VideoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlaylistId = CursorUtil.getColumnIndexOrThrow(_cursor, "playlistId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPublishTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "publishTimestamp");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final int _cursorIndexOfUploaderName = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaderName");
          final int _cursorIndexOfUploaderUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaderUrl");
          final int _cursorIndexOfUploaderAvatarUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaderAvatarUrl");
          final int _cursorIndexOfUploaderVerified = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaderVerified");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfViewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "viewCount");
          final int _cursorIndexOfTextualUploadDate = CursorUtil.getColumnIndexOrThrow(_cursor, "textualUploadDate");
          final int _cursorIndexOfStreamType = CursorUtil.getColumnIndexOrThrow(_cursor, "streamType");
          final int _cursorIndexOfShortFormContent = CursorUtil.getColumnIndexOrThrow(_cursor, "shortFormContent");
          final List<VideoEntity> _result = new ArrayList<VideoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VideoEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final long _tmpPlaylistId;
            _tmpPlaylistId = _cursor.getLong(_cursorIndexOfPlaylistId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final Long _tmpPublishTimestamp;
            if (_cursor.isNull(_cursorIndexOfPublishTimestamp)) {
              _tmpPublishTimestamp = null;
            } else {
              _tmpPublishTimestamp = _cursor.getLong(_cursorIndexOfPublishTimestamp);
            }
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final String _tmpUploaderName;
            if (_cursor.isNull(_cursorIndexOfUploaderName)) {
              _tmpUploaderName = null;
            } else {
              _tmpUploaderName = _cursor.getString(_cursorIndexOfUploaderName);
            }
            final String _tmpUploaderUrl;
            if (_cursor.isNull(_cursorIndexOfUploaderUrl)) {
              _tmpUploaderUrl = null;
            } else {
              _tmpUploaderUrl = _cursor.getString(_cursorIndexOfUploaderUrl);
            }
            final String _tmpUploaderAvatarUrl;
            if (_cursor.isNull(_cursorIndexOfUploaderAvatarUrl)) {
              _tmpUploaderAvatarUrl = null;
            } else {
              _tmpUploaderAvatarUrl = _cursor.getString(_cursorIndexOfUploaderAvatarUrl);
            }
            final boolean _tmpUploaderVerified;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfUploaderVerified);
            _tmpUploaderVerified = _tmp != 0;
            final long _tmpDuration;
            _tmpDuration = _cursor.getLong(_cursorIndexOfDuration);
            final long _tmpViewCount;
            _tmpViewCount = _cursor.getLong(_cursorIndexOfViewCount);
            final String _tmpTextualUploadDate;
            if (_cursor.isNull(_cursorIndexOfTextualUploadDate)) {
              _tmpTextualUploadDate = null;
            } else {
              _tmpTextualUploadDate = _cursor.getString(_cursorIndexOfTextualUploadDate);
            }
            final String _tmpStreamType;
            if (_cursor.isNull(_cursorIndexOfStreamType)) {
              _tmpStreamType = null;
            } else {
              _tmpStreamType = _cursor.getString(_cursorIndexOfStreamType);
            }
            final boolean _tmpShortFormContent;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfShortFormContent);
            _tmpShortFormContent = _tmp_1 != 0;
            _item = new VideoEntity(_tmpId,_tmpPlaylistId,_tmpTitle,_tmpDescription,_tmpPublishTimestamp,_tmpThumbnailUrl,_tmpUploaderName,_tmpUploaderUrl,_tmpUploaderAvatarUrl,_tmpUploaderVerified,_tmpDuration,_tmpViewCount,_tmpTextualUploadDate,_tmpStreamType,_tmpShortFormContent);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
