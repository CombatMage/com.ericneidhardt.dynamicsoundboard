package org.neidhardt.dynamicsoundboard.splashactivity

import android.Manifest
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SplashActivityPresenter(
		private val view: SplashActivityContract.View
) : SplashActivityContract.Presenter {

	override fun onCreated() {
		val missingPermissions = this.view.getMissingPermissions()
		if (missingPermissions.isNotEmpty()) {
			this.view.explainPermissions(missingPermissions)
		} else {
			this.view.openActivity(SoundActivity::class.java)
		}
	}

	override fun onExplainPermissionDialogClosed() {
		val missingPermissions = this.view.getMissingPermissions()
		if (missingPermissions.isNotEmpty()) {
			this.view.requestPermissions(missingPermissions)
		} else {
			this.view.openActivity(SoundActivity::class.java)
		}
	}

	override fun onUserHasChangedPermissions() {
		val missingPermissions = this.view.getMissingPermissions()
		if (missingPermissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE)
				|| missingPermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			this.view.finishActivity()
		} else {
			this.view.openActivity(SoundActivity::class.java)
		}
	}
}