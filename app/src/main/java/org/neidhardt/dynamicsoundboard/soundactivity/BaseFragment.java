package org.neidhardt.dynamicsoundboard.soundactivity;


import android.app.Fragment;

public abstract class BaseFragment extends Fragment
{
	public SoundActivity getBaseActivity()
	{
		return (SoundActivity)this.getActivity();
	}

}
