package org.neidhardt.dynamicsoundboard.preferences

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import org.neidhardt.dynamicsoundboard.R

/**
 * File created by eric.neidhardt on 21.01.2015.
 */
open class PreferenceActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_preferences)
		this.createActionbar()

		this.fragmentManager.beginTransaction().replace(R.id.main_frame, SoundboardPreferenceFragment()).commit()
	}

	protected open fun createActionbar()
	{
		val toolbar = this.findViewById(R.id.toolbar) as Toolbar
		this.setSupportActionBar(toolbar)
		val actionBar = this.supportActionBar
		actionBar?.setDisplayHomeAsUpEnabled(true)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		val id = item.itemId
		if (id == android.R.id.home)
			this.navigateBack()

		return super.onOptionsItemSelected(item)
	}

	private fun navigateBack()
	{
		val intent = NavUtils.getParentActivityIntent(this)
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
		NavUtils.navigateUpTo(this, intent)
		this.overridePendingTransition(R.anim.anim_nothing, R.anim.anim_slide_out)
	}

	class SoundboardPreferenceFragment : PreferenceFragment()
	{
		override fun onCreate(savedInstanceState: Bundle?)
		{
			super.onCreate(savedInstanceState)
			this.addPreferencesFromResource(R.xml.preferences)
		}
	}

	override fun finish()
	{
		super.finish()
		this.overridePendingTransition(R.anim.anim_nothing, R.anim.anim_slide_out)
	}
}
