package org.neidhardt.dynamicsoundboard.persistance

import android.content.Context
import com.sevenval.simplestorage.SimpleStorage
import org.neidhardt.dynamicsoundboard.persistance.model.AppData

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */
class AppDataStorage(context: Context) : SimpleStorage<AppData>(context, AppData::class.java) {

}