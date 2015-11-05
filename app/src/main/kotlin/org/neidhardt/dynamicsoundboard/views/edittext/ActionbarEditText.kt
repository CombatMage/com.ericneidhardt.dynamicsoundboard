package org.neidhardt.dynamicsoundboard.views.edittext

import android.content.Context
import android.text.method.KeyListener
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R


class ActionbarEditText(context: Context, attrs: AttributeSet) :
		CustomEditText(context, attrs),
		TextView.OnEditorActionListener,
		EditTextBackEvent.EditTextImeBackListener
{
	override var input: EditTextBackEvent? = null
	override var onTextEditedListener: CustomEditText.OnTextEditedListener? = null

	private var divider: View? = null
	private var editTextKeyListener: KeyListener? = null
	private var initialText: String? = null

	override fun inflateLayout(context: Context)
	{
		LayoutInflater.from(context).inflate(R.layout.view_actionbar_edittext, this, true)

		this.input = this.findViewById(R.id.edittext) as EditTextBackEvent
		this.divider = this.findViewById(R.id.v_divider)

		this.disableEditText()
		this.input?.setOnClickListener {
			this.enableEditText()
			this.initialText = this.input?.text.toString()
		}
	}

	override fun onEditorAction(textView: TextView, actionId: Int, keyEvent: KeyEvent): Boolean
	{
		if (actionId == EditorInfo.IME_ACTION_DONE)
		{
			this.onTextEditedListener?.onTextEdited(this.input?.text?.toString() ?: "")
			this.disableEditText()
		}
		return false
	}

	override fun onImeBack(ctrl: EditTextBackEvent, text: String)
	{
		if (this.initialText != null)
			this.input!!.setText(this.initialText)
		this.disableEditText()
	}

	private fun disableEditText()
	{
		this.divider?.visibility = View.INVISIBLE

		this.editTextKeyListener = this.input!!.keyListener
		this.input!!.isCursorVisible = false
		this.input!!.keyListener = null
	}

	private fun enableEditText() {
		this.divider?.visibility = View.VISIBLE

		this.input!!.keyListener = this.editTextKeyListener
		this.input!!.isCursorVisible = true
		val lManager = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		lManager.showSoftInput(this.input, 0)
	}

}
