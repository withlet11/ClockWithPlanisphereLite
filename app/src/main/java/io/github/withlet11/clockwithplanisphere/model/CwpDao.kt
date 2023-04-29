package io.github.withlet11.clockwithplanispherelite.model

import androidx.room.*

@Dao
interface CwpDao {
    @Query("SELECT * FROM `hip_list`")
    fun getAllHip(): List<AbstractSkyModel.HipEntry>

    @Query("SELECT * FROM `constellation_lines`")
    fun getAllConstellationLines(): List<AbstractSkyModel.ConstellationLineEntry>

    @Query("SELECT * FROM `milkyway_north`")
    fun getNorthMilkyWay(): List<AbstractSkyModel.NorthMilkyWayDotEntry>

    @Query("SELECT * FROM `milkyway_south`")
    fun getSouthMilkyWay(): List<AbstractSkyModel.SouthMilkyWayDotEntry>
}
