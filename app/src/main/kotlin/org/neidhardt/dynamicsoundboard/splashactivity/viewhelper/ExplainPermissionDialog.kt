package org.neidhardt.dynamicsoundboard.splashactivity.viewhelper

import android.support.v4.app.FragmentManager
import org.neidhardt.dynamicsoundboard.splashactivity.SplashActivity
import org.neidhardt.dynamicsoundboard.views.viewpagerdialog.ViewPagerDialog

/**
 * Created by eric.neidhardt@gmail.com on 12.09.2017.
 */
class ExplainPermissionDialog : ViewPagerDialog() {

	companion object {
		private val TAG = ViewPagerDialog::class.java.name
		fun show(fragmentManager: FragmentManager, viewData: Array<String>) {
			val dialog = ExplainPermissionDialog()
			dialog.setViewData(viewData)
			dialog.show(fragmentManager, TAG)
		}
	}

	override fun getStringTitle(): String? = "Title"

	override fun getStringMessage(): String? = "Message"

	override fun getStringButtonOk(): String? = "Ok"

	override fun onButtonClicked() {
		val callingActivity = this.activity as SplashActivity
		val missingPermissions = callingActivity.getMissingPermissions()
		callingActivity.requestPermissions(missingPermissions)
	}
}