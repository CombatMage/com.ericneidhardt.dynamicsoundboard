package org.neidhardt.dynamicsoundboard.views.spinner

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import org.neidhardt.dynamicsoundboard.R


class CustomSpinner(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), View.OnClickListener
{
	private val spinner: Spinner

	init
	{
		LayoutInflater.from(context).inflate(R.layout.view_spinner, this, true)
		this.spinner = this.findViewById(R.id.spinner) as Spinner
		this.setOnClickListener(this)
	}

	fun setItems(items: List<String>)
	{
		val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
		this.spinner.adapter = adapter
	}

	val selectedItemPosition: Int
		get() = this.spinner.selectedItemPosition

	fun setSelectedItem(position: Int): Any = this.spinner.setSelection(position)

	override fun onClick(v: View)
	{
		this.spinner.performClick()
	}
}
