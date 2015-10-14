package org.neidhardt.dynamicsoundboard.fileexplorer

import android.app.FragmentManager
import android.os.Bundle

/**
 * Project created by Eric Neidhardt on 30.09.2014.
 */
public class GetNewSoundFromDirectoryDialog() : AddNewSoundFromDirectoryDialog()
{
	private val TAG = javaClass.name

	public constructor(manager: FragmentManager, callingFragmentTag: String) : this()
	{
		val dialog = GetNewSoundFromDirectoryDialog()

		val args = Bundle()
		args.putString(KEY_CALLING_FRAGMENT_TAG, callingFragmentTag)
		dialog.arguments = args

		dialog.show(manager, TAG)
	}

	override fun canSelectDirectory(): Boolean
	{
		return true
	}

	override fun canSelectFile(): Boolean
	{
		return true
	}

	override fun canSelectMultipleFiles(): Boolean
	{
		return true
	}

	override fun returnResults()
	{
		val fileList = super.getFileListResult();

		val callingFragment = this.fragmentManager.findFragmentByTag(super.callingFragmentTag)
		if (callingFragment != null && callingFragment is FileResultHandler)
		{
			callingFragment.onFileResultsAvailable(fileList)
		}

		this.dismiss()
	}

}
