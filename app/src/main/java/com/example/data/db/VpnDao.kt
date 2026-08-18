package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnDao {
    @Query("SELECT * FROM vpn_configs ORDER BY pingMs IS NULL ASC, pingMs ASC, name ASC")
    fun getAllConfigs(): Flow<List<VpnConfigEntity>>

    @Query("SELECT * FROM vpn_configs WHERE id = :id LIMIT 1")
    suspend fun getConfigById(id: String): VpnConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigs(configs: List<VpnConfigEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: VpnConfigEntity)

    @Query("UPDATE vpn_configs SET pingMs = :pingMs, lastTestedAt = :testedAt WHERE id = :id")
    suspend fun updatePing(id: String, pingMs: Long?, testedAt: Long)

    @Query("DELETE FROM vpn_configs")
    suspend fun clearConfigs()

    @Transaction
    suspend fun replaceAllConfigs(newConfigs: List<VpnConfigEntity>) {
        clearConfigs()
        insertConfigs(newConfigs)
    }

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettingsEntity)
}
