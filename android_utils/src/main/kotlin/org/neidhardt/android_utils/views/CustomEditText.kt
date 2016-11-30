package org.neidhardt.android_utils.views

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import org.neidhardt.android_utils.R

abstract class CustomEditText(context: Context, attrs: AttributeSet) :
		FrameLayout(context, attrs),
		TextView.OnEditorActionListener,
		EditTextBackEvent.EditTextImeBackListener,
		View.OnFocusChangeListener
{
	abstract var input: EditTextBackEvent
	protected abstract var mOnTextEditedListener: OnTextEditedListener?

	fun setOnTextEditedListener(listener: (String) -> Unit) {
		this.mOnTextEditedListener = object : OnTextEditedListener {
			override fun onTextEdited(text: String) {
				listener(text)
			}
		}
	}

	fun setOnTextEditedListener(listener: OnTextEditedListener?) {
		this.mOnTextEditedListener = listener
	}

	var text: String?
		get() = this.input.text?.toString()
		set(value) {
			this.input.text = if (value != null) SpannableStringBuilder(value) else null
		}

	val displayedText: String
		get() {
			val userInput = this.text!!.toString()
			if (!userInput.isEmpty())
				return userInput
			return this.hint!!.toString()
		}

	var hint: CharSequence?
		get() = this.input.hint
		set(value) {
			this.input.hint = value
		}

	init {
		this.inflateLayout(context)
		this.initTextListeners()
		this.setValuesAttributeSet(context, attrs)
	}

	abstract fun inflateLayout(context: Context)

	fun initTextListeners() {
		this.input.onFocusChangeListener = this
		this.input.onImeBackListener = this
		this.input.setOnEditorActionListener(this)
	}

	fun setValuesAttributeSet(context: Context, attrs: AttributeSet) {
		val array = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText, 0, 0)
		val size = array.getDimension(R.styleable.CustomEditText_text_size, 0f)
		if (array.hasValue(R.styleable.CustomEditText_text_size))
			this.input.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)

		val color = array.getColor(R.styleable.CustomEditText_text_color, 0)
		if (array.hasValue(R.styleable.CustomEditText_text_color))
			this.input.setTextColor(color)

		array.recycle()
	}

	override fun hasFocus(): Boolean = this.input.hasFocus()

	override fun clearFocus(): Unit = this.input.clearFocus()

	override fun onEditorAction(textView: TextView, actionId: Int, keyEvent: KeyEvent?): Boolean {
		if (actionId == EditorInfo.IME_ACTION_DONE)
			this.mOnTextEditedListener?.onTextEdited(this.input.text.toString())
		return false
	}

	override fun onImeBack(ctrl: EditTextBackEvent, text: String) {
		this.mOnTextEditedListener?.onTextEdited(this.input.text.toString())
	}

	override fun onFocusChange(v: View, hasFocus: Boolean) {
		if (!hasFocus)
			this.mOnTextEditedListener?.onTextEdited(this.input.text.toString())
			this.onFocusChangeListener?.onFocusChange(this, hasFocus)
	}

	override fun onSaveInstanceState(): Parcelable {
		val superState = super.onSaveInstanceState()
		return SavedCustomEditTextState(superState, this.input.text.toString())
	}

	override fun onRestoreInstanceState(state: Parcelable?) {
		val savedState = state as SavedCustomEditTextState?
		super.onRestoreInstanceState(savedState?.superState)

		if (savedState?.value != null)
			this.input.setText(savedState?.value)
	}

	@SuppressWarnings("unchecked")
	override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
		// As we save our own instance state, ensure our children don't save
		// and restore their state as well.
		super.dispatchFreezeSelfOnly(container)
	}

	@SuppressWarnings("unchecked")
	override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>?) {
		/** See comment in [.dispatchSaveInstanceState]  */
		super.dispatchThawSelfOnly(container)
	}

	class SavedCustomEditTextState : BaseSavedState {
		var value: String? = null
			private set

		constructor(superState: Parcelable, value: String) : super(superState) {
			this.value = value
		}

		private constructor(superState: Parcel) : super(superState) {
			this.value = superState.readString()
		}

		override fun writeToParcel(destination: Parcel, flags: Int) {
			super.writeToParcel(destination, flags)
			destination.writeString(this.value)
		}
		
		companion object {
			@Suppress("unused")
			val CREATOR: Parcelable.Creator<SavedCustomEditTextState> = object : Parcelable.Creator<SavedCustomEditTextState>
			{
				override fun createFromParcel(`in`: Parcel): SavedCustomEditTextState {
					return SavedCustomEditTextState(`in`)
				}

				override fun newArray(size: Int): Array<SavedCustomEditTextState?> {
					return arrayOfNulls(size)
				}
			}
		}
	}

	interface OnTextEditedListener {
		fun onTextEdited(text: String)
	}
}
