package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.DialogFragment;
import com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment;
import com.ericneidhardt.dynamicsoundboard.storage.SoundSheetManagerFragment;

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
public abstract class BaseDialog extends DialogFragment
{
	protected SoundManagerFragment getSoundManagerFragment()
	{
		return (SoundManagerFragment)this.getFragmentManager().findFragmentByTag(SoundManagerFragment.TAG);
	}

	protected SoundSheetManagerFragment getSoundSheetManagerFragment()
	{
		return (SoundSheetManagerFragment)this.getFragmentManager().findFragmentByTag(SoundSheetManagerFragment.TAG);
	}
}
