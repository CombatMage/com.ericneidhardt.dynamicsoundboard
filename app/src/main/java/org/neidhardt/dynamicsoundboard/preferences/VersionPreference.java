package org.neidhardt.dynamicsoundboard.preferences;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.util.AttributeSet;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.misc.Logger;

/**
 * Created by eric.neidhardt on 17.02.2015.
 */
public class VersionPreference extends Preference
{
	private static final String TAG = VersionPreference.class.getName();

	@SuppressWarnings("unused")
	public VersionPreference(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public VersionPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public VersionPreference(Context context)
	{
		super(context);
		this.init(context);
	}

	private void init(Context context)
	{
		String versionName = context.getString(R.string.app_name);
		try
		{
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			versionName = versionName + " " + packageInfo.versionName + " (" + packageInfo.versionCode + ")";
		}
		catch (PackageManager.NameNotFoundException e)
		{
			Logger.d(TAG, e.getMessage()); // should never happen, because this is the app  package
		}
		super.setSummary(versionName);
	}
}
