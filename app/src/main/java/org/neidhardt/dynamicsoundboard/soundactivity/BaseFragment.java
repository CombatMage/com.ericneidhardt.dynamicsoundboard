package org.neidhardt.dynamicsoundboard.soundactivity;


import android.app.Fragment;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;

public abstract class BaseFragment extends Fragment
{
	public SoundActivity getBaseActivity()
	{
		return (SoundActivity)this.getActivity();
	}

	public NavigationDrawerFragment getNavigationDrawerFragment()
	{
		return (NavigationDrawerFragment)this.getFragmentManager().findFragmentById(R.id.navigation_drawer_fragment);
	}

	public ServiceManagerFragment getServiceManagerFragment()
	{
		return (ServiceManagerFragment)this.getFragmentManager().findFragmentByTag(ServiceManagerFragment.TAG);
	}

	public SoundSheetFragment getCurrentSoundFragment()
	{
		Fragment currentFragment = this.getFragmentManager().findFragmentById(R.id.main_frame);
		if (currentFragment != null && currentFragment.isVisible() && currentFragment instanceof SoundSheetFragment)
			return (SoundSheetFragment) currentFragment;
		return null;
	}

	public SoundSheetsDataAccess getSoundSheetDataModel()
	{
		return SoundActivity.getSoundSheetsDataModel();
	}
}
