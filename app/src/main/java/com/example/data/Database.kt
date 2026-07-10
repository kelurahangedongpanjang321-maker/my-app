package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val nama: String,
    val rt: String,
    val rw: String,
    val email: String,
    val role: String, // "Admin Kelurahan", "RW", "RT"
    val foto: String, // File path or URL
    val nik: String = "",
    val noHp: String = "",
    val alamat: String = "",
    val jabatan: String = ""
)

@Entity(tableName = "kegiatan")
data class KegiatanEntity(
    @PrimaryKey val id: String,
    val tanggal: String,
    val hari: String,
    val judul: String,
    val uraian: String,
    val keterangan: String,
    val lokasi: String, // GPS coordinates & address name
    val createdBy: String,
    val rt: String,
    val rw: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val photoPaths: List<String> = emptyList() // List of local file paths
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: String = "global_settings",
    val namaAplikasi: String = "SIMPEL",
    val logoBase64: String = "", // Custom uploaded logo as Base64 or local path
    val backgroundBase64: String = "", // Custom background as Base64 or local path
    val wallpaperDashboard: String = "",
    val wallpaperLogin: String = "",
    val wallpaperSplash: String = "",
    val wallpaperPdf: String = "",
    val warnaUtama: String = "#005AC1", // Hex main color (Default: Professional Polish Blue)
    val warnaTombol: String = "#005AC1",
    val iconType: String = "default",
    val tulisanSplash: String = "SIMPEL",
    val subTulisanSplash: String = "Sistem Manajemen Pelaporan\nKelurahan Gedong Panjang",
    val taglineSplash: String = "Bersama Membangun Lingkungan yang Tertib, Aman dan Sejahtera",
    val temaMode: String = "Light Mode", // "Dark Mode", "Light Mode", "System"
    val bahasa: String = "Indonesia", // "Indonesia", "Inggris"
    val fontType: String = "Sans-Serif" // "Sans-Serif", "Serif", "Monospace"
)

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",")
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return ""
        return list.joinToString(",")
    }
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
}

@Dao
interface KegiatanDao {
    @Query("SELECT * FROM kegiatan ORDER BY createdAt DESC")
    fun getAllKegiatan(): Flow<List<KegiatanEntity>>

    @Query("SELECT * FROM kegiatan WHERE id = :id LIMIT 1")
    suspend fun getKegiatanById(id: String): KegiatanEntity?

    @Query("SELECT * FROM kegiatan WHERE isSynced = 0")
    suspend fun getUnsyncedKegiatan(): List<KegiatanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKegiatan(kegiatan: KegiatanEntity)

    @Query("UPDATE kegiatan SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM kegiatan WHERE id = :id")
    suspend fun deleteKegiatan(id: String)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 'global_settings' LIMIT 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 'global_settings' LIMIT 1")
    suspend fun getSettingsSync(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)
}

@Database(
    entities = [UserEntity::class, KegiatanEntity::class, SettingsEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun kegiatanDao(): KegiatanDao
    abstract fun settingsDao(): SettingsDao
}
