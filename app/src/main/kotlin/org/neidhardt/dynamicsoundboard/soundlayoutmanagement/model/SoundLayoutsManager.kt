package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model

import android.content.Context
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.daohelper.GreenDaoHelper
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutAddedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutsRemovedEvent
import java.util.*

/**
 * File created by eric.neidhardt on 09.03.2015.
 */
class SoundLayoutsManager(private val context: Context) :
		SoundLayoutsAccess,
		SoundLayoutsStorage,
		SoundLayoutsUtil
{
	companion object {
		const val DB_DEFAULT: String = "org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManagerFragment.db_default"

		private const val DB_SOUND_LAYOUTS = "org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManagerFragment.db_sound_layouts"

		fun getNewDatabaseIdForLabel(label: String): String {
			return Integer.toString((label + SoundboardApplication.randomNumber).hashCode())
		}
	}

	private val daoSession: DaoSession = GreenDaoHelper.setupDatabase(this.context, DB_SOUND_LAYOUTS)
	private val soundLayouts: MutableList<SoundLayout> = ArrayList()

	private val eventBus = EventBus.getDefault()

	init {
		this.soundLayouts.addAll(this.daoSession.soundLayoutDao.queryBuilder().list())
		if (this.soundLayouts.size == 0) {
			val defaultLayout = this.getDefaultSoundLayout()
			defaultLayout.isSelected = true
			this.addSoundLayout(defaultLayout)
		}
	}

	override fun getDbSoundLayouts(): DaoSession = this.daoSession

	override fun getSoundLayouts(): List<SoundLayout> = this.soundLayouts

	override fun getActiveSoundLayout(): SoundLayout {
		this.soundLayouts
				.filter { it.isSelected }
				.forEach { return it }

		// no layout is currently selected
		val layout = this.soundLayouts[0]
		layout.isSelected = true
		layout.updateItemInDatabaseAsync()
		return layout
	}

	override fun addSoundLayout(soundLayout: SoundLayout) {
		this.soundLayouts.add(soundLayout)
		this.daoSession.soundLayoutDao.insert(soundLayout)
	}

	override fun getSuggestedName(): String =
			this.context.resources.getString(R.string.suggested_sound_layout_name) + this.soundLayouts.size

	private fun getDefaultSoundLayout(): SoundLayout {
		val label = this.context.getString(R.string.suggested_sound_layout_name)
		val layout = SoundLayout().apply {
			this.databaseId = DB_DEFAULT
			this.label = label
			this.isSelected = true
		}
		return layout
	}

	override fun removeSoundLayouts(soundLayoutsToRemove: List<SoundLayout>) {
		this.soundLayouts.removeAll(soundLayoutsToRemove)

		var wasActiveLayoutRemoved = false
		for (layout in soundLayoutsToRemove) {
			this.daoSession.soundLayoutDao.delete(layout)
			if (layout.isSelected) // if the current active layout was removed, we need to select another one
				wasActiveLayoutRemoved = true
		}

		if (this.soundLayouts.size == 0) { // make sure there is always at least 1 layout left
			val defaultLayout = this.getDefaultSoundLayout()
			if (defaultLayout.isSelected)
				wasActiveLayoutRemoved = false // no need to set a new sound layout active, because the default layout is active

			this.addSoundLayout(defaultLayout)
			this.eventBus.post(SoundLayoutAddedEvent(defaultLayout))
		}

		if (wasActiveLayoutRemoved) {
			this.soundLayouts[0].isSelected = true
			this.soundLayouts[0].updateItemInDatabaseAsync()
		}

		this.eventBus.post(SoundLayoutsRemovedEvent(soundLayoutsToRemove))
	}

	override fun setSoundLayoutSelected(position: Int) {
		val size = this.soundLayouts.size
		for (i in 0..size - 1) {
			val layout = this.soundLayouts[i]
			if (layout.isSelected && i != position) {
				layout.isSelected = false
				layout.updateItemInDatabaseAsync()
			}
			else if (i == position) {
				layout.isSelected = true
				layout.updateItemInDatabaseAsync()
			}
		}
	}

	override fun getSoundLayoutById(databaseId: String): SoundLayout? {
		return this.soundLayouts.firstOrNull { it.databaseId == databaseId }
	}
}
