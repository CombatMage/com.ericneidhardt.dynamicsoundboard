package org.neidhardt.dynamicsoundboard.misc

import android.content.Context
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundboardDaoOpenHelper

/**
 * @author eric.neidhardt on 09.06.2016.
 */
object GreenDaoHelper
{
	fun setupDatabase(context: Context, dbName: String): DaoSession {
		val helper = SoundboardDaoOpenHelper(context, dbName, null)
		val db = helper.writableDatabase
		val daoMaster = org.neidhardt.dynamicsoundboard.dao.DaoMaster(db)
		return daoMaster.newSession()
	}
}