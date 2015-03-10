package org.neidhardt.dynamicsoundboard.soundlayouts;

import android.os.Bundle;
import org.neidhardt.dynamicsoundboard.BaseFragment;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.misc.Util;

import java.util.List;

/**
 * Created by eric.neidhardt on 09.03.2015.
 */
public class SoundLayoutsManagerFragment extends BaseFragment
{
	public static final String TAG = SoundLayoutsManagerFragment.class.getName();

	private static final String DB_SOUND_LAYOUTS = "org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManagerFragment.db_sound_layouts";

	private List<SoundLayout> soundLayouts;
	private DaoSession daoSession;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		this.daoSession = Util.setupDatabase(this.getActivity(), DB_SOUND_LAYOUTS);
		this.soundLayouts = this.getSoundLayouts();
	}

	public List<SoundLayout> getSoundLayouts()
	{
		List<SoundLayout> storedLayouts = this.daoSession.getSoundLayoutDao().queryBuilder().list();
		if (storedLayouts.size() == 0)
			storedLayouts.add(this.getDefaultSoundLayout());
		return storedLayouts;
	}

	public SoundLayout getActiveSoundLayout()
	{
		List<SoundLayout> storedLayouts = this.daoSession.getSoundLayoutDao().queryBuilder().list();
		for (SoundLayout soundLayout : storedLayouts)
		{
			if (soundLayout.getIsSelected())
				return soundLayout;
		}
		throw new IllegalStateException("no active sound layout was found");
	}

	private SoundLayout getDefaultSoundLayout()
	{
		SoundLayout layout = new SoundLayout();
		String label = DynamicSoundboardApplication.getSoundboardContext().getString(R.string.sound_layout_default);
		layout.setDatabaseId(Integer.toString((label + DynamicSoundboardApplication.getRandomNumber()).hashCode()));
		layout.setLabel(label);
		layout.setIsSelected(true);
		return layout;
	}
}
