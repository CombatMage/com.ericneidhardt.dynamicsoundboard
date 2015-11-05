package org.neidhardt.dynamicsoundboard.soundactivity

import android.Manifest
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.misc.IntentRequest

/**
 * File created by eric.neidhardt on 05.11.2015.
 */
interface RequestPermissionHelper
{
	fun AppCompatActivity.requestPermissionsReadPhoneStateIfRequired()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
		{
			if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE))
				ExplainPermissionDialog(this.fragmentManager, R.string.request_permission_read_phone_state_message, Manifest.permission.READ_PHONE_STATE, false)
			else
				ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), IntentRequest.REQUEST_PERMISSION_READ_PHONE_STATE)
		}
	}

	/**
	 * Checks the given permission state and requests the permission if not already granted
	 * @return true if the permission was requested, false if it was already granted
	 */
	fun AppCompatActivity.requestPermissionsReadStorageIfRequired(): Boolean
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
				ExplainPermissionDialog(this.fragmentManager, R.string.request_permission_read_storage_message, Manifest.permission.READ_EXTERNAL_STORAGE, true)
			else
				ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), IntentRequest.REQUEST_PERMISSION_READ_STORAGE)

			return true
		}
		else
			return false
	}

	fun AppCompatActivity.requestPermissionsWriteStorageIfRequired()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
				ExplainPermissionDialog(this.fragmentManager, R.string.request_permission_write_storage_message, Manifest.permission.WRITE_EXTERNAL_STORAGE, true)
			else
				ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), IntentRequest.REQUEST_PERMISSION_WRITE_STORAGE)
		}
	}
}
