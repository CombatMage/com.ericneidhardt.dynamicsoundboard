package org.neidhardt.dynamicsoundboard.splashactivity.viewhelper

import android.support.v4.app.FragmentManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.splashactivity.SplashActivity
import org.neihdardt.viewpagerdialog.ViewPagerDialog
import java.util.ArrayList

/**
 * Created by eric.neidhardt@gmail.com on 12.09.2017.
 */
class ExplainPermissionDialog : ViewPagerDialog() {

	companion object {
		private val TAG = ViewPagerDialog::class.java.name

		fun show(fragmentManager: FragmentManager, missingPermissions: Array<String>) {
			val dialog = ExplainPermissionDialog()

			dialog.setViewData(missingPermissions)
			dialog.show(fragmentManager, TAG)
		}
	}

	override fun setViewData(viewData: Array<String>) {
		val messagesToDisplay = ArrayList<String>()

		// TODO
		super.setViewData(messagesToDisplay.toTypedArray())
	}

	override fun getStringTitle(): String? = this.context?.getString(R.string.dialogexplainpermissions_title)

	override fun getStringMessage(): String? = this.context?.getString(R.string.dialogexplainpermissions_message)

	override fun getStringButtonOk(): String? = this.context?.getString(R.string.dialog_ok)

	override fun onButtonClicked() {
		val callingActivity = this.activity as SplashActivity
		val missingPermissions = callingActivity.getMissingPermissions()
		callingActivity.requestPermissions(missingPermissions)
	}
}