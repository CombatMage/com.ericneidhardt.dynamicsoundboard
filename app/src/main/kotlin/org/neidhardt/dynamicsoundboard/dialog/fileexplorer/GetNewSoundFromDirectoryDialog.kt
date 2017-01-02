package org.neidhardt.dynamicsoundboard.dialog.fileexplorer

import android.os.Bundle
import android.support.v4.app.FragmentManager

/**
 * Project created by Eric Neidhardt on 30.09.2014.
 */
class GetNewSoundFromDirectoryDialog() : AddNewSoundFromDirectoryDialog() {

	private val TAG = javaClass.name

	constructor(manager: FragmentManager, callingFragmentTag: String) : this() {
		val dialog = GetNewSoundFromDirectoryDialog()

		val args = Bundle()
		args.putString(KEY_CALLING_FRAGMENT_TAG, callingFragmentTag)
		dialog.arguments = args

		dialog.show(manager, TAG)
	}

	override fun canSelectDirectory(): Boolean = true

	override fun canSelectFile(): Boolean = true

	override fun canSelectMultipleFiles(): Boolean = true

	override fun returnResults() {
		val fileList = super.getFileListResult()

		val callingFragment = this.fragmentManager.findFragmentByTag(super.callingFragmentTag)
		if (callingFragment != null && callingFragment is FileResultHandler) {
			callingFragment.onFileResultsAvailable(fileList.toList())
		}

		this.dismiss()
	}

}
