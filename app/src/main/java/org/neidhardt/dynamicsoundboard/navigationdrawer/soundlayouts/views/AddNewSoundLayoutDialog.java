package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views;

import android.app.FragmentManager;
import android.os.Bundle;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutChangedEvent;

/**
 * Created by eric.neidhardt on 12.03.2015.
 */
public class AddNewSoundLayoutDialog extends SoundLayoutDialog
{
	private static final String TAG = AddNewSoundLayoutDialog.class.getName();

	private static final String KEY_SUGGESTED_NAME = "org.neidhardt.dynamicsoundboard.dialog.AddNewSoundLayoutDialog.suggestedName";

	private String suggestedName;

	public static void showInstance(FragmentManager manager, String suggestedName)
	{
		AddNewSoundLayoutDialog dialog = new AddNewSoundLayoutDialog();

		Bundle args = new Bundle();
		args.putString(KEY_SUGGESTED_NAME, suggestedName);
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = this.getArguments();
		if (args != null)
			this.suggestedName = args.getString(KEY_SUGGESTED_NAME);
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.dialog_add_new_sound_layout;
	}

	@Override
	protected String getHintForName()
	{
		return this.suggestedName;
	}

	@Override
	protected void deliverResult()
	{
		String name = super.soundLayoutName.getDisplayedText();
		SoundLayout layout = new SoundLayout();
		layout.setIsSelected(false);
		layout.setDatabaseId(SoundLayoutsManager.getNewDatabaseIdForLabel(name));
		layout.setLabel(name);

		SoundLayoutsManager.getInstance().addSoundLayout(layout);

		EventBus.getDefault().post(new SoundLayoutChangedEvent(layout, SoundLayoutChangedEvent.REQUEST.LAYOUT_LIST_CHANGE));
	}
}
