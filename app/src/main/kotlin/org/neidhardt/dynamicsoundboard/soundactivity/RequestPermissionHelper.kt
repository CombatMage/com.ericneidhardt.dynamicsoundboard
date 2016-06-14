package org.neidhardt.dynamicsoundboard.soundactivity

import android.Manifest
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import java.util.*

/**
 * File created by eric.neidhardt on 05.11.2015.
 */
interface RequestPermissionHelper
{
	val AppCompatActivity.hasPermissionReadStorage: Boolean
		get() = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

	val AppCompatActivity.hasPermissionWriteStorage: Boolean
		get() = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

	val AppCompatActivity.hasPermissionPhoneState: Boolean
		get() = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

	fun AppCompatActivity.requestPermissionsIfRequired(): List<String> {
		val requiredPermissions = ArrayList<String>()
		if (!this.hasPermissionReadStorage) requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
		if (!this.hasPermissionWriteStorage) requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
		if (!this.hasPermissionPhoneState) requiredPermissions.add(Manifest.permission.READ_PHONE_STATE)

		if (!requiredPermissions.isEmpty()) {
			val array = requiredPermissions.toTypedArray()
			ActivityCompat.requestPermissions(this, array, IntentRequest.REQUEST_PERMISSIONS)
		}
		return requiredPermissions
	}
}
