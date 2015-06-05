package org.neidhardt.dynamicsoundboard.views;

import android.app.DialogFragment;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
public abstract class BaseDialog extends DialogFragment
{

	public SoundActivity getSoundActivity()
	{
		return (SoundActivity) this.getActivity();
	}

	public ServiceManagerFragment getServiceManagerFragment()
	{
		return (ServiceManagerFragment)this.getFragmentManager().findFragmentByTag(ServiceManagerFragment.TAG);
	}

	public SoundSheetFragment getSoundSheetFragment(String fragmentTag)
	{
		return (SoundSheetFragment)this.getFragmentManager().findFragmentByTag(fragmentTag);
	}

}
