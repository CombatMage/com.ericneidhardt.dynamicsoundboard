package org.neidhardt.android_utils.views

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Created by eric.neidhardt@gmail.com on 05.01.2017.
 */
fun EditText.showKeyboard() {
	this.postDelayed({
		val context = this.context
		val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
	},100)
}

fun EditText.hideKeyboard() {
	val context = this.context
	val inputManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
	inputManager.hideSoftInputFromWindow(this.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}