package com.example.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.database.AppDatabase
import com.example.data.local.database.dao.PlaylistDao
import com.example.data.local.database.dao.VideoDao
import com.example.util.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).addMigrations(MIGRATION_1_2)
            .setQueryCallback({ sqlQuery, bindArgs ->
                Logger.d("실행된 SQL: $sqlQuery, args: $bindArgs")
            }, Executors.newSingleThreadExecutor())
            .build()
    }

    @Provides
    fun providePlaylistDao(database: AppDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideVideoDao(database: AppDatabase): VideoDao {
        return database.videoDao()
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. 새로운 테이블을 생성합니다.
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS videos_new (
                id TEXT NOT NULL,
                playlistId INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                publishTimestamp INTEGER,
                thumbnailUrl TEXT,
                uploaderName TEXT,
                uploaderUrl TEXT,
                uploaderAvatarUrl TEXT,  -- 변경된 컬럼 (nullable)
                uploaderVerified INTEGER NOT NULL,
                duration INTEGER NOT NULL,
                viewCount INTEGER NOT NULL,
                textualUploadDate TEXT,
                streamType TEXT,         -- 변경된 컬럼 (nullable)
                shortFormContent INTEGER NOT NULL,
                PRIMARY KEY(id),
                FOREIGN KEY(playlistId) REFERENCES playlists(playlistId) ON DELETE CASCADE
            )
            """.trimIndent()
            )

            // 2. 기존 테이블의 데이터를 새 테이블로 복사합니다.
            db.execSQL(
                """
            INSERT INTO videos_new (
                id, playlistId, title, description, publishTimestamp, thumbnailUrl,
                uploaderName, uploaderUrl, uploaderAvatarUrl, uploaderVerified, duration, viewCount,
                textualUploadDate, streamType, shortFormContent
            )
            SELECT 
                id, playlistId, title, description, publishTimestamp, thumbnailUrl,
                uploaderName, uploaderUrl, uploaderAvatars, uploaderVerified, duration, viewCount,
                textualUploadDate, streamType, shortFormContent
            FROM videos
            """.trimIndent()
            )

            // 3. 기존 테이블 삭제
            db.execSQL("DROP TABLE videos")

            // 4. 새 테이블의 이름을 기존 테이블 이름으로 변경
            db.execSQL("ALTER TABLE videos_new RENAME TO videos")

            // 5. 최종 테이블에 인덱스를 생성합니다.
            db.execSQL("CREATE INDEX IF NOT EXISTS index_videos_playlistId ON videos(playlistId)")
        }
    }


}