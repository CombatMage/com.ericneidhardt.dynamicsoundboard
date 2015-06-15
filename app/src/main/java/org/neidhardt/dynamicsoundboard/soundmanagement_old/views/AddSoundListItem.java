package org.neidhardt.dynamicsoundboard.soundmanagement_old.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.views.edittext.CustomEditText;

public class AddSoundListItem extends LinearLayout implements TextWatcher
{
	private TextView soundPath;
	private CustomEditText soundName;
	private boolean wasSoundNameAltered = false;

	public AddSoundListItem(Context context)
	{
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_add_sound_list_item, this, true);
		this.soundPath = (TextView)this.findViewById(R.id.tv_path);
		this.soundName = (CustomEditText)this.findViewById(R.id.et_name_file);
		this.soundName.addTextChangedListener(this);
	}

	public void setPath(String path)
	{
		this.soundPath.setText(path);
	}

	public void setSoundName(String name)
	{
		this.soundName.setText(name);
		this.wasSoundNameAltered = false;
	}

	public String getSoundName()
	{
		return this.soundName.getText().toString();
	}

	public boolean wasSoundNameAltered()
	{
		return this.wasSoundNameAltered;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

	@Override
	public void afterTextChanged(Editable s) {
		this.wasSoundNameAltered = true;
	}
}
