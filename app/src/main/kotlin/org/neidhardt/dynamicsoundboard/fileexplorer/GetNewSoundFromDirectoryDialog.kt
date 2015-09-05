package org.neidhardt.dynamicsoundboard.fileexplorer

import android.app.FragmentManager
import android.os.Bundle
import android.view.View

/**
 * Project created by Eric Neidhardt on 30.09.2014.
 */
public class GetNewSoundFromDirectoryDialog() : AddNewSoundFromDirectoryDialog()
{
	private val TAG = javaClass.getName()

	public constructor(manager: FragmentManager, callingFragmentTag: String) : this()
	{
		val dialog = GetNewSoundFromDirectoryDialog()

		val args = Bundle()
		args.putString(AddNewSoundFromDirectoryDialog.KEY_CALLING_FRAGMENT_TAG, callingFragmentTag)
		dialog.setArguments(args)

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

	override fun returnResults()
	{
		val fileList = super.getFileListResult();

		val callingFragment = this.getFragmentManager().findFragmentByTag(super.callingFragmentTag)
		if (callingFragment != null && callingFragment is FileResultHandler)
		{
			callingFragment.onFileResultsAvailable(fileList)
		}

		this.dismiss()
	}

}
