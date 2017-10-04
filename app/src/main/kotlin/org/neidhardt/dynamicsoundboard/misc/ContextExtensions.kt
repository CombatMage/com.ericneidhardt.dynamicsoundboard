package org.neidhardt.dynamicsoundboard.misc

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

/**
 * Created by eric.neidhardt@gmail.com on 05.07.2017.
 */

val Context.hasPermissionReadStorage: Boolean
	get() = ContextCompat.checkSelfPermission(this,
			Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

val Context.hasPermissionWriteStorage: Boolean
	get() = ContextCompat.checkSelfPermission(this,
			Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

val Context.hasPermissionPhoneState: Boolean
	get() = ContextCompat.checkSelfPermission(this,
			Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
