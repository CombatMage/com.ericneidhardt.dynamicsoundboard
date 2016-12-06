package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model

import android.content.Context
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.daohelper.GreenDaoHelper
import java.util.*

/**
 * Created by eric.neidhardt on 06.12.2016.
 */
class NewSoundLayoutManager(private val context: Context) : ISoundLayoutManager {

	private val daoSession: DaoSession = GreenDaoHelper.setupDatabase(this.context, SoundLayoutsManager.DB_SOUND_LAYOUTS)

	override val soundLayouts: MutableList<SoundLayout> = ArrayList<SoundLayout>()

	init {
		val count = this.daoSession.soundLayoutDao.count()
		if (count == 0.toLong())
			this.daoSession.soundLayoutDao.insert(this.getDefaultSoundLayout())
		this.soundLayouts.addAll(this.daoSession.soundLayoutDao.queryBuilder().list())
	}

	override fun addSoundLayout(soundLayout: SoundLayout) {
		this.soundLayouts.add(soundLayout)
		this.daoSession.soundLayoutDao.insert(soundLayout)
	}

	override fun updateSoundLayout(update: () -> SoundLayout) {
		update.invoke().updateItemInDatabaseAsync()
	}

	override fun removeSoundLayout(soundLayout: SoundLayout) {
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getSuggestedName(): String {
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	private fun getDefaultSoundLayout(): SoundLayout =
			SoundLayout().apply {
				this.databaseId = SoundLayoutsManager.DB_DEFAULT
				this.label = context.getString(R.string.suggested_sound_layout_name)
				this.isSelected = true
			}
}