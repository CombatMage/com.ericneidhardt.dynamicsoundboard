package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by Eric Neidhardt on 09.09.2014.
 */
public class LoadSoundListItem extends LinearLayout
{
	private TextView soundPath;
	private CustomEditText soundName;

	public LoadSoundListItem(Context context)
	{
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_load_sound_list_item, this, true);
		this.soundPath = (TextView)this.findViewById(R.id.tv_path);
		this.soundName = (CustomEditText)this.findViewById(R.id.et_name_file);
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
