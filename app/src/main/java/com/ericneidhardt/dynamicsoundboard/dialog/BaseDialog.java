package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.DialogFragment;
import com.ericneidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment;

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
abstract class BaseDialog extends DialogFragment
{
	ServiceManagerFragment getServiceManagerFragment()
	{
		return (ServiceManagerFragment)this.getFragmentManager().findFragmentByTag(ServiceManagerFragment.TAG);
	}

	SoundSheetManagerFragment getSoundSheetManagerFragment()
	{
		return (SoundSheetManagerFragment)this.getFragmentManager().findFragmentByTag(SoundSheetManagerFragment.TAG);
	}
}
