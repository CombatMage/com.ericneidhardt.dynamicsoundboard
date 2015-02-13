package org.neidhardt.dynamicsoundboard.dialog;

import android.app.DialogFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetsManagerFragment;

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
public abstract class BaseDialog extends DialogFragment
{
	public ServiceManagerFragment getServiceManagerFragment()
	{
		return (ServiceManagerFragment)this.getFragmentManager().findFragmentByTag(ServiceManagerFragment.TAG);
	}

	public SoundSheetsManagerFragment getSoundSheetManagerFragment()
	{
		return (SoundSheetsManagerFragment)this.getFragmentManager().findFragmentByTag(SoundSheetsManagerFragment.TAG);
	}
}
