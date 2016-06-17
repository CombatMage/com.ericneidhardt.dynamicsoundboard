package org.neidhardt.dynamicsoundboard.navigationdrawer;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by Eric.Neidhardt@GMail.com on 17.06.2016.
 */
public class NavigationDrawerFragment2 extends Fragment {

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		ViewDataBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_navigation_drawer, container, false);
		return binding.getRoot();
	}
}
