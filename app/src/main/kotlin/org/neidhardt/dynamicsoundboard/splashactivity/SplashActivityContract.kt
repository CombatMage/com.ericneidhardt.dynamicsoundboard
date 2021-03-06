package org.neidhardt.dynamicsoundboard.splashactivity

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
interface SplashActivityContract {
	interface View {
		fun openActivity(cls: Class<*>)
		fun requestPermissions(permissions: Array<String>)
		fun getMissingPermissions(): Array<String>
		fun explainPermissions(permissions: Array<String>)
		fun closeApplication(showClosingInfo: Boolean)
	}
	interface Presenter {
		fun onCreated()
		fun onExplainPermissionDialogClosed()
		fun onUserHasChangedPermissions()
	}
}