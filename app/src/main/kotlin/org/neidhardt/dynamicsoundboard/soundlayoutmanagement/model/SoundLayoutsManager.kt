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
		SoundLayoutsStorage
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

	private val soundLayouts: MutableList<SoundLayout> = ArrayList()
	private val daoSession: DaoSession = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), DB_SOUND_LAYOUTS)

	init
	{
		this.soundLayouts.addAll(this.daoSession.getSoundLayoutDao().queryBuilder().list())
		if (this.soundLayouts.size() == 0)
		{
			val defaultLayout = this.getDefaultSoundLayout()
			defaultLayout.setIsSelected(true)
			this.soundLayouts.add(defaultLayout)
			this.daoSession.getSoundLayoutDao().insert(defaultLayout)
		}
	}

	override fun getDbSoundLayouts(): DaoSession
	{
		return this.daoSession
	}

	override fun getActiveSoundLayout(): SoundLayout
	{
		for (soundLayout in this.soundLayouts)
		{
			if (soundLayout.getIsSelected())
				return soundLayout
		}
		// no layout is currently selected
		val layout = this.soundLayouts.get(0)
		layout.setIsSelected(true)
		layout.updateItemInDatabaseAsync()
		return layout
	}

	override public fun addSoundLayout(soundLayout: SoundLayout)
	{
		this.soundLayouts.add(soundLayout)
		this.daoSession.getSoundLayoutDao().insert(soundLayout)
	}

	public fun getSuggestedSoundLayoutName(): String
	{
		return DynamicSoundboardApplication.getSoundboardContext().getResources().getString(R.string.suggested_sound_layout_name) + this.soundLayouts.size()
	}

	private fun getDefaultSoundLayout(): SoundLayout
	{
		val layout = SoundLayout()
		val label = DynamicSoundboardApplication.getSoundboardContext().getString(R.string.sound_layout_default)
		layout.setDatabaseId(DB_DEFAULT)
		layout.setLabel(label)
		layout.setIsSelected(true)
		return layout
	}

	override public fun removeSoundLayouts(soundLayoutsToRemove: List<SoundLayout>)
	{
		this.soundLayouts.removeAll(soundLayoutsToRemove)

		if (this.soundLayouts.size() == 0)
			this.soundLayouts.add(this.getDefaultSoundLayout())

		var newSelectionRequired = false
		for (soundLayout in this.soundLayouts)
		{
			if (soundLayout.getIsSelected())
				newSelectionRequired = true

			this.daoSession.getSoundLayoutDao().delete(soundLayout)
		}
		if (this.soundLayouts.size() == 0)
		{
			val defaultLayout = this.getDefaultSoundLayout()
			defaultLayout.setIsSelected(true)
			this.soundLayouts.add(defaultLayout)
			this.daoSession.getSoundLayoutDao().insert(defaultLayout)
			newSelectionRequired = false
		}

		if (!newSelectionRequired)
		{
			this.soundLayouts.get(0).setIsSelected(true)
			this.soundLayouts.get(0).updateItemInDatabaseAsync()
		}
	}

	override public fun setSoundLayoutSelected(position: Int)
	{
		val size = this.soundLayouts.size()
		for (i in 0..size - 1)
		{
			val layout = this.soundLayouts.get(i)
			if (layout.getIsSelected() && i != position)
			{
				layout.setIsSelected(false)
				layout.updateItemInDatabaseAsync()
			}
			else if (i == position)
			{
				layout.setIsSelected(false)
				layout.updateItemInDatabaseAsync()
			}
		}
	}

	override public fun getSoundLayoutById(databaseId: String): SoundLayout?
	{
		for (layout in this.soundLayouts) {
			if (layout.getDatabaseId() == databaseId)
				return layout
		}
		return null
	}


}
