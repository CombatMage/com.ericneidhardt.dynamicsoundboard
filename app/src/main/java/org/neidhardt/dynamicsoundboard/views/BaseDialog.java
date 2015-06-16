package org.neidhardt.dynamicsoundboard.views;

import android.app.DialogFragment;
import android.os.Bundle;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundmanagement.service.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil;

import javax.inject.Inject;

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
public abstract class BaseDialog extends DialogFragment
{

	@Inject protected SoundSheetsDataAccess soundSheetsDataAccess;
	@Inject protected SoundSheetsDataUtil soundSheetsDataUtil;
	@Inject protected SoundSheetsDataStorage soundSheetsDataStorage;

	@Inject protected SoundsDataStorage soundsDataStorage;
	@Inject protected SoundsDataAccess soundsDataAccess;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		DynamicSoundboardApplication.getSoundSheetsDataComponent().inject(this);
		DynamicSoundboardApplication.getSoundsDataComponent().inject(this);
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
