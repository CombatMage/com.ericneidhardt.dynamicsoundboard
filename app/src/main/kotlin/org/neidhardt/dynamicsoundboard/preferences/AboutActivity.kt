package org.neidhardt.dynamicsoundboard.preferences

import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.widget.Toolbar
import org.neidhardt.dynamicsoundboard.R

/**
 * File created by eric.neidhardt on 21.01.2015.
 */
class AboutActivity : PreferenceActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_preferences)

		this.createActionbar()

		this.fragmentManager.beginTransaction().replace(R.id.main_frame, SoundboardAboutFragment()).commit()
	}

	override fun createActionbar()
	{
		val toolbar = this.findViewById(R.id.toolbar) as Toolbar
		toolbar.setTitle(R.string.about)
		this.setSupportActionBar(toolbar)
		if (this.supportActionBar != null)
			this.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
	}

	class SoundboardAboutFragment : PreferenceFragment()
	{
		override fun onCreate(savedInstanceState: Bundle?)
		{
			super.onCreate(savedInstanceState)
			this.addPreferencesFromResource(R.xml.about)
		}
	}

}
