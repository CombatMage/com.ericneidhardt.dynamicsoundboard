package org.neidhardt.dynamicsoundboard.preferences;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Created by eric.neidhardt on 17.02.2015.
 */
public class OpenAboutPreference extends Preference
{

	@SuppressWarnings("unused")
	public OpenAboutPreference(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public OpenAboutPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public OpenAboutPreference(Context context)
	{
		super(context);
		this.init(context);
	}

	private void init(Context context)
	{
		super.setIntent(new Intent(context, AboutActivity.class));
	}
}
