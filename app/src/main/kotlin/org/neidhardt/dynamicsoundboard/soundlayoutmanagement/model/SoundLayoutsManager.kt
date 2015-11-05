package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model

import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.misc.Util
import java.util.*

/**
 * File created by eric.neidhardt on 09.03.2015.
 */


public class SoundLayoutsManager :
		SoundLayoutsAccess,
		SoundLayoutsStorage,
		SoundLayoutsUtil
{
	companion object
	{
		public val DB_DEFAULT: String = "org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManagerFragment.db_default"

		private val DB_SOUND_LAYOUTS = "org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManagerFragment.db_sound_layouts"

		public fun getNewDatabaseIdForLabel(label: String): String
		{
			return Integer.toString((label + DynamicSoundboardApplication.getRandomNumber()).hashCode())
		}
	}

	private val daoSession: DaoSession = Util.setupDatabase(DynamicSoundboardApplication.getContext(), DB_SOUND_LAYOUTS)
	private val soundLayouts: MutableList<SoundLayout> = ArrayList()

	init
	{
		this.soundLayouts.addAll(this.daoSession.soundLayoutDao.queryBuilder().list())
		if (this.soundLayouts.size() == 0)
		{
			val defaultLayout = this.getDefaultSoundLayout()
			defaultLayout.isSelected = true
			this.soundLayouts.add(defaultLayout)
			this.daoSession.soundLayoutDao.insert(defaultLayout)
		}
	}

	override fun getDbSoundLayouts(): DaoSession
	{
		return this.daoSession
	}

	override fun getSoundLayouts(): List<SoundLayout>
	{
		return this.soundLayouts
	}

	override fun getActiveSoundLayout(): SoundLayout
	{
		for (soundLayout in this.soundLayouts)
		{
			if (soundLayout.isSelected)
				return soundLayout
		}
		// no layout is currently selected
		val layout = this.soundLayouts.get(0)
		layout.isSelected = true
		layout.updateItemInDatabaseAsync()
		return layout
	}

	override public fun addSoundLayout(soundLayout: SoundLayout)
	{
		this.soundLayouts.add(soundLayout)
		this.daoSession.soundLayoutDao.insert(soundLayout)
	}

	override fun getSuggestedName(): String
	{
		return DynamicSoundboardApplication.getContext().resources.getString(R.string.suggested_sound_layout_name) + this.soundLayouts.size()
	}

	private fun getDefaultSoundLayout(): SoundLayout
	{
		val layout = SoundLayout()
		val label = DynamicSoundboardApplication.getContext().getString(R.string.sound_layout_default)
		layout.databaseId = DB_DEFAULT
		layout.label = label
		layout.isSelected = true
		return layout
	}

	override fun removeSoundLayouts(soundLayoutsToRemove: List<SoundLayout>)
	{
		this.soundLayouts.removeAll(soundLayoutsToRemove)

		if (this.soundLayouts.size() == 0)
			this.soundLayouts.add(this.getDefaultSoundLayout())

		var newSelectionRequired = false
		for (soundLayout in this.soundLayouts)
		{
			if (soundLayout.isSelected)
				newSelectionRequired = true

			this.daoSession.soundLayoutDao.delete(soundLayout)
		}
		if (this.soundLayouts.size() == 0)
		{
			val defaultLayout = this.getDefaultSoundLayout()
			defaultLayout.isSelected = true
			this.soundLayouts.add(defaultLayout)
			this.daoSession.soundLayoutDao.insert(defaultLayout)
			newSelectionRequired = false
		}

		if (!newSelectionRequired)
		{
			this.soundLayouts.get(0).isSelected = true
			this.soundLayouts.get(0).updateItemInDatabaseAsync()
		}
	}

	override public fun setSoundLayoutSelected(position: Int)
	{
		val size = this.soundLayouts.size()
		for (i in 0..size - 1)
		{
			val layout = this.soundLayouts.get(i)
			if (layout.isSelected && i != position)
			{
				layout.isSelected = false
				layout.updateItemInDatabaseAsync()
			}
			else if (i == position)
			{
				layout.isSelected = true
				layout.updateItemInDatabaseAsync()
			}
		}
	}

	override public fun getSoundLayoutById(databaseId: String): SoundLayout?
	{
		for (layout in this.soundLayouts) {
			if (layout.databaseId == databaseId)
				return layout
		}
		return null
	}


}
