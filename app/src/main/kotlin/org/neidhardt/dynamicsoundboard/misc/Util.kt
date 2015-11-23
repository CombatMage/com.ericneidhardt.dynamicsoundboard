package org.neidhardt.dynamicsoundboard.misc

import android.content.Context
import android.os.Build
import org.neidhardt.dynamicsoundboard.dao.DaoMaster
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundboardDaoOpenHelper

/**
 * Project created by Eric Neidhardt on 30.08.2014.
 */
object AndroidVersion {
    val IS_LOLLIPOP_AVAILABLE = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
}

object GreenDaoHelper{
    fun setupDatabase(context: Context, dbName: String): DaoSession {
        val helper = SoundboardDaoOpenHelper(context, dbName, null)
        val db = helper.writableDatabase
        val daoMaster = DaoMaster(db)
        return daoMaster.newSession()
    }
}
