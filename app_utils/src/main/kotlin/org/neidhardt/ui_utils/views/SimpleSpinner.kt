package org.neidhardt.ui_utils.views

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.Spinner
import java.util.*

class SimpleSpinner : Spinner {

	constructor(context: Context?) : super(context)

	constructor(context: Context?, mode: Int) : super(context, mode)

	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, mode: Int) : super(context, attrs, defStyleAttr, mode)

	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int, mode: Int) : super(context, attrs, defStyleAttr, defStyleRes, mode)

	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int, mode: Int, popupTheme: Resources.Theme?) : super(context, attrs, defStyleAttr, defStyleRes, mode, popupTheme)

	var items: List<String> = ArrayList<String>()
		set(value) {
			field = value
			val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, value)
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
			this.adapter = adapter
		}

}
