package org.neidhardt.dynamicsoundboard.splashactivity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import org.neidhardt.android_utils.EnhancedAppCompatActivity
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.misc.hasPermissionPhoneState
import org.neidhardt.dynamicsoundboard.misc.hasPermissionReadStorage
import org.neidhardt.dynamicsoundboard.misc.hasPermissionWriteStorage
import org.neidhardt.dynamicsoundboard.splashactivity.viewhelper.ExplainPermissionDialog
import java.util.*


/**
* Created by eric.neidhardt@sevenval.com on 10.11.2016.
*/
class SplashActivity : EnhancedAppCompatActivity(), SplashActivityContract.View {

	private lateinit var presenter: SplashActivityContract.Presenter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		this.presenter = SplashActivityPresenter(this)

		this.presenter.onCreated()
	}

	override fun openActivity(cls: Class<*>) {
		this.startActivity(Intent(this, cls))
		this.finish()
	}

	override fun finishActivity() {
		this.finish()
	}

	override fun explainPermissions(permissions: Array<String>) {
		ExplainPermissionDialog.show(this.supportFragmentManager, permissions, this)
	}

	override fun onRequestPermissionsResult(
			requestCode: Int,
			permissions: Array<out String>,
			grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			IntentRequest.REQUEST_PERMISSIONS -> { this.presenter.onUserHasChangedPermissions() }
		}
	}

	override fun requestPermissions(permissions: Array<String>) {
		if (permissions.isNotEmpty()) {
			ActivityCompat.requestPermissions(this, permissions, IntentRequest.REQUEST_PERMISSIONS)
		}
	}

	override fun getMissingPermissions(): Array<String> {
		val requiredPermissions = ArrayList<String>()
		if (!this.hasPermissionReadStorage) {
			requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
		}
		if (!this.hasPermissionWriteStorage) {
			requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
		}
		if (!this.hasPermissionPhoneState) {
			requiredPermissions.add(Manifest.permission.READ_PHONE_STATE)
		}
		return requiredPermissions.toTypedArray()
	}
}