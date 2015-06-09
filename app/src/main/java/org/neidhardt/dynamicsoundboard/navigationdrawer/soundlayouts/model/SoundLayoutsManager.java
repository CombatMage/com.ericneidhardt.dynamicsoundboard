package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model;

import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.misc.Util;

import java.util.List;

/**
 * File created by eric.neidhardt on 09.03.2015.
 */
public class SoundLayoutsManager implements SoundLayoutModel
{
	public static final String DB_DEFAULT = "org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManagerFragment.db_default";

	private static final String DB_SOUND_LAYOUTS = "org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManagerFragment.db_sound_layouts";

	private static SoundLayoutsManager instance;

	List<SoundLayout> soundLayouts;
	DaoSession daoSession;

	SoundLayoutsManager()
	{
		this.daoSession = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), DB_SOUND_LAYOUTS);
		this.soundLayouts = this.getSoundLayouts();
	}

	public static SoundLayoutsManager getInstance()
	{
		if (instance == null)
			instance = new SoundLayoutsManager();
		return instance;
	}

	public List<SoundLayout> getSoundLayouts()
	{
		if (this.soundLayouts == null)
			this.soundLayouts = this.daoSession.getSoundLayoutDao().queryBuilder().list();
		if (this.soundLayouts.size() == 0)
		{
			SoundLayout defaultLayout = this.getDefaultSoundLayout();
			defaultLayout.setIsSelected(true);
			this.soundLayouts.add(defaultLayout);
			this.daoSession.getSoundLayoutDao().insert(defaultLayout);
		}
		return this.soundLayouts;
	}

	public SoundLayout getActiveSoundLayout()
	{
		List<SoundLayout> storedLayouts = this.getSoundLayouts();
		for (SoundLayout soundLayout : storedLayouts)
		{
			if (soundLayout.getIsSelected())
				return soundLayout;
		}
		storedLayouts.get(0).setIsSelected(true);
		this.update(storedLayouts.get(0));
		return storedLayouts.get(0);
	}

	public void addSoundLayout(SoundLayout soundLayout)
	{
		List<SoundLayout> storedLayouts = this.getSoundLayouts();
		storedLayouts.add(soundLayout);
		this.daoSession.getSoundLayoutDao().insert(soundLayout);
	}

	public void updateSoundLayoutById(String databaseId, String newLabel)
	{
		SoundLayout layoutToUpdate = this.getSoundLayoutById(databaseId);
		if (layoutToUpdate == null)
			return;

		layoutToUpdate.setLabel(newLabel);
		this.daoSession.update(layoutToUpdate);
	}

	public void update(SoundLayout soundLayout)
	{
		this.daoSession.update(soundLayout);
	}

	public void update(final List<SoundLayout> soundLayouts)
	{
		this.daoSession.runInTx(new Runnable()
		{
			@Override
			public void run()
			{
				for (SoundLayout layout : soundLayouts)
					daoSession.update(layout);
			}
		});
	}

	public String getSuggestedSoundLayoutName()
	{
		return DynamicSoundboardApplication.getSoundboardContext().getResources().getString(R.string.suggested_sound_layout_name) + this.getSoundLayouts().size();
	}

	private SoundLayout getDefaultSoundLayout()
	{
		SoundLayout layout = new SoundLayout();
		String label = DynamicSoundboardApplication.getSoundboardContext().getString(R.string.sound_layout_default);
		layout.setDatabaseId(DB_DEFAULT);
		layout.setLabel(label);
		layout.setIsSelected(true);
		return layout;
	}

	public static String getNewDatabaseIdForLabel(String label)
	{
		return Integer.toString((label + DynamicSoundboardApplication.getRandomNumber()).hashCode());
	}

	public void delete(List<SoundLayout> soundLayoutsToRemove)
	{
		List<SoundLayout> soundLayouts = this.getSoundLayouts();
		soundLayouts.removeAll(soundLayoutsToRemove);

		if (soundLayouts.size() == 0)
			soundLayouts.add(this.getDefaultSoundLayout());

		boolean isSelectionAvailable = false;
		for (SoundLayout soundLayout : soundLayouts)
		{
			if (soundLayout.getIsSelected())
				isSelectionAvailable = true;
		}
		if (!isSelectionAvailable)
			soundLayouts.get(0).setIsSelected(true);

		daoSession.getSoundLayoutDao().deleteAll();
		daoSession.getSoundLayoutDao().insertInTx(soundLayouts);
	}

	public void clear()
	{
		this.soundLayouts = null;
		this.daoSession.getSoundLayoutDao().deleteAll();
	}

	public void setSelected(int position)
	{
		List<SoundLayout> soundLayouts = this.getSoundLayouts();
		int size = soundLayouts.size();
		for (int i = 0; i < size; i++)
		{
			boolean isSelected = i == position;
			soundLayouts.get(i).setIsSelected(isSelected);
		}
		this.update(soundLayouts);
	}

	public SoundLayout getSoundLayoutById(String databaseId)
	{
		for (SoundLayout layout : this.soundLayouts)
		{
			if (layout.getDatabaseId().equals(databaseId))
				return layout;
		}
		return null;
	}
}
