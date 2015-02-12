package org.neidhardt.dynamicsoundboard;


import android.app.Fragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment;

public abstract class BaseFragment extends Fragment
{
	public BaseActivity getBaseActivity()
	{
		return (BaseActivity)this.getActivity();
	}

	protected NavigationDrawerFragment getNavigationDrawer()
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
