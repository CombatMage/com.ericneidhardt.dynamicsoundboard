package org.neidhardt.dynamicsoundboard.infoactivity.viewhelper

import android.content.Context
import android.content.pm.PackageManager
import android.preference.Preference
import android.util.AttributeSet
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.misc.Logger

/**
 * File created by eric.neidhardt on 17.02.2015.
 */
class VersionPreference : Preference {

	private val TAG = javaClass.name

	@Suppress("unused")
	constructor(context: Context) : super(context) {
		this.init(context)
	}

	@Suppress("unused")
	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		this.init(context)
	}

	@Suppress("unused")
	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		this.init(context)
	}

	private fun init(context: Context) {
		var versionName = context.getString(R.string.app_name)
		try {
			val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
			versionName = versionName + " " + packageInfo.versionName + " (" + packageInfo.versionCode + ")"
		}
		catch (e: PackageManager.NameNotFoundException) {
			Logger.d(TAG, e.message) // should never happen, because this is the app  package
		}

		super.setSummary(versionName)
	}
}
