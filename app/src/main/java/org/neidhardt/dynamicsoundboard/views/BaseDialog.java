package org.neidhardt.dynamicsoundboard.views;

import android.app.DialogFragment;
import android.os.Bundle;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil;

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
public abstract class BaseDialog extends DialogFragment
{

	protected SoundSheetsDataAccess soundSheetsDataAccess;
	protected SoundSheetsDataUtil soundSheetsDataUtil;
	protected SoundSheetsDataStorage soundSheetsDataStorage;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		DynamicSoundboardApplication.getSoundSheetsDataComponent().inject(this);
	}

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

	protected void notifyFragment(String fragmentTag)
	{
		NavigationDrawerFragment navigationDrawerFragment = this.getNavigationDrawerFragment();
		SoundSheetFragment fragment = (SoundSheetFragment) this.getFragmentManager().findFragmentByTag(fragmentTag);
		if (fragment != null)
			fragment.notifyDataSetChanged();

		navigationDrawerFragment.getSoundSheetsAdapter().notifyDataSetChanged(); // updates sound count in sound sheet list
	}

	private NavigationDrawerFragment getNavigationDrawerFragment()
	{
		return (NavigationDrawerFragment)this.getFragmentManager().findFragmentByTag(NavigationDrawerFragment.TAG);
	}
}
