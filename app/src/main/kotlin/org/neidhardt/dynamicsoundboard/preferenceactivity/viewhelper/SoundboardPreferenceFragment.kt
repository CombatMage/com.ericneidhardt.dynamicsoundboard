package org.neidhardt.dynamicsoundboard.preferenceactivity.viewhelper

import android.os.Bundle
import android.preference.PreferenceFragment
import org.neidhardt.dynamicsoundboard.R

/**
 * Created by eric.neidhardt@gmail.com on 31.08.2017.
 */
class SoundboardPreferenceFragment : PreferenceFragment() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.addPreferencesFromResource(R.xml.preferences)
	}
}