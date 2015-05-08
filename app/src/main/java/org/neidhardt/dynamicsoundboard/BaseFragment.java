package org.neidhardt.dynamicsoundboard;


import android.app.Fragment;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.SoundSheetsManagerFragment;

public abstract class BaseFragment extends Fragment
{
	public BaseActivity getBaseActivity()
	{
		return (BaseActivity)this.getActivity();
	}

	protected NavigationDrawerFragment getNavigationDrawerFragment()
	{
		return (NavigationDrawerFragment)this.getFragmentManager().findFragmentById(R.id.navigation_drawer_fragment);
	}

	public SoundSheetsManagerFragment getSoundSheetManagerFragment()
	{
		return (SoundSheetsManagerFragment)this.getFragmentManager().findFragmentByTag(SoundSheetsManagerFragment.TAG);
	}

	public ServiceManagerFragment getServiceManagerFragment()
	{
		return (ServiceManagerFragment)this.getFragmentManager().findFragmentByTag(ServiceManagerFragment.TAG);
	}

}
