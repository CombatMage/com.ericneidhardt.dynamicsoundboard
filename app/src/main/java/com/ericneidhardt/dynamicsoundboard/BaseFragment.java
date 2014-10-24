package com.ericneidhardt.dynamicsoundboard;


import android.app.Fragment;
import com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment;
import com.ericneidhardt.dynamicsoundboard.storage.SoundSheetManagerFragment;

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

	public SoundManagerFragment getSoundManagerFragment()
	{
		return (SoundManagerFragment)this.getFragmentManager().findFragmentByTag(SoundManagerFragment.TAG);
	}

	public SoundSheetManagerFragment getSoundSheetManagerFragment()
	{
		return (SoundSheetManagerFragment)this.getFragmentManager().findFragmentByTag(SoundSheetManagerFragment.TAG);
	}
}
