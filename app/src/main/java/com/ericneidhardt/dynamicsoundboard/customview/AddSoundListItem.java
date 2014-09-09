package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by Eric Neidhardt on 09.09.2014.
 */
public class AddSoundListItem extends LinearLayout implements View.OnClickListener
{
	private TextView soundPath;
	private CustomEditText soundName;

	public AddSoundListItem(Context context)
	{
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_add_sound_list_item, this, true);
		this.soundPath = (TextView)this.findViewById(R.id.tv_path);
		this.soundName = (CustomEditText)this.findViewById(R.id.et_name_file);

		this.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		// this.soundName.getEditText().requestFocus();
		// InputMethodManager imm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		// imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

		//this.soundName.getEditText().setCursorVisible(true);
		//InputMethodManager lManager = (InputMethodManager)this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		//lManager.showSoftInput(this.soundName.getEditText(), InputMethodManager.SHOW_IMPLICIT);
	}

	public void setPath(String path)
	{
		this.soundPath.setText(path);
	}

	public void setSoundName(String name)
	{
		this.soundName.setText(name);
	}

}
