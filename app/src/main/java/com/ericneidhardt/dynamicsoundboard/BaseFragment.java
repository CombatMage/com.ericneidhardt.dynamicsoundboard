package com.ericneidhardt.dynamicsoundboard;


import android.app.Fragment;
import com.ericneidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment;

public abstract class BaseFragment extends Fragment
{
	public BaseActivity getBaseActivity()
	{
		return (BaseActivity)this.getActivity();
	}

	public NavigationDrawerFragment getNavigationDrawer()
	{
		return (NavigationDrawerFragment)this.getFragmentManager().findFragmentByTag(NavigationDrawerFragment.TAG);
	}

	public SoundSheetManagerFragment getSoundSheetManagerFragment()
	{
		return (SoundSheetManagerFragment)this.getFragmentManager().findFragmentByTag(SoundSheetManagerFragment.TAG);
	}

	public ServiceManagerFragment getServiceManagerFragment()
	{
		return (ServiceManagerFragment)this.getFragmentManager().findFragmentByTag(ServiceManagerFragment.TAG);
	}
}
