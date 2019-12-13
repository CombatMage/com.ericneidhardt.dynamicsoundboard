package org.neidhardt.dynamicsoundboard.infoactivity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.toolbar_preferences.*
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.infoactivity.viewhelper.SoundboardInfoFragment

/**
 * File created by eric.neidhardt on 21.01.2015.
 */
class InfoActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_preferences)

		this.configureToolbar()

		this.fragmentManager
				.beginTransaction()
				.replace(R.id.main_frame, SoundboardInfoFragment())
				.commit()
	}

	private fun configureToolbar() {
		val toolbar = this.toolbar_preferences
		toolbar.setTitle(R.string.about)
		this.setSupportActionBar(toolbar)
		if (this.supportActionBar != null)
			this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		val id = item.itemId
		if (id == android.R.id.home) {
			this.navigateBack()
			return true
		}

		return super.onOptionsItemSelected(item)
	}

	private fun navigateBack() {
		val intent = NavUtils.getParentActivityIntent(this)
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
		NavUtils.navigateUpTo(this, intent)
		this.overridePendingTransition(R.anim.anim_nothing, R.anim.anim_slide_out)
	}

	override fun finish() {
		super.finish()
		this.overridePendingTransition(R.anim.anim_nothing, R.anim.anim_slide_out)
	}
}
