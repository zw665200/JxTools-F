package com.recording.trans.model.dao

import androidx.room.*
import com.recording.trans.bean.FileWithType

@Dao
interface FileDao {

    @Query("SELECT * FROM fileWithType")
    fun getAll(): List<FileWithType>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: FileWithType)

    @Query("SELECT * FROM fileWithType WHERE type = (:type) and size>=(:minSize) and size<(:maxSize) and date>=(:minDate) and date<(:maxDate) order by size Desc")
    fun findPics(type: String, minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type!='video' and type!='voice' and type!='doc' and size>=(:minSize) and size<(:maxSize) and date>=(:minDate) and date<(:maxDate) order by size Desc")
    fun findPics(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='voice' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate)")
    fun findVoices(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='audio' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate)")
    fun findAudio(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='doc' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate)")
    fun findDocs(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='voice' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by size desc")
    fun findVoicesBySizeDesc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='voice' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by size asc")
    fun findVoicesBySizeAsc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='voice' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by date desc")
    fun findVoicesByDateDesc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='voice' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by date asc")
    fun findVoicesByDateAsc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='audio' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by size desc")
    fun findAudioBySizeDesc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='audio' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by size asc")
    fun findAudioBySizeAsc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='audio' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by date desc")
    fun findAudioByDateDesc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='audio' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by date asc")
    fun findAudioByDateAsc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='video' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate)")
    fun findVideos(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='video' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by size desc")
    fun findVideosBySizeDesc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='video' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by size asc")
    fun findVideosBySizeAsc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='video' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by date desc")
    fun findVideosByDateDesc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='video' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by date asc")
    fun findVideosByDateAsc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='doc' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by size desc")
    fun findDocsBySizeDesc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='doc' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by size asc")
    fun findDocsBySizeAsc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='doc' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by date desc")
    fun findDocsByDateDesc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='doc' and size>=(:minSize) and size<(:maxSize) AND date>=(:minDate) and date<(:maxDate) order by date asc")
    fun findDocsByDateAsc(minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='video' OR type='audio' OR type='doc' order by date desc")
    fun findAllByDateDesc(): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='video' OR type='audio' OR type='doc'  order by date asc")
    fun findAllByDateAsc(): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='video' OR type='audio' OR type='doc'  order by size desc")
    fun findAllBySizeDesc(): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE type='video' OR type='audio' OR type='doc'  order by size asc")
    fun findAllBySizeAsc(): List<FileWithType>?

    @Query("SELECT * FROM fileWithType WHERE path = (:path)")
    fun find(path: String): FileWithType?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg user: FileWithType)

    @Delete
    fun delete(vararg user: FileWithType)

    @Query("DELETE from fileWithType")
    fun delete()
}