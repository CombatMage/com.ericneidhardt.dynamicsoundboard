package org.neidhardt.dynamicsoundboard.views;

import android.app.DialogFragment;
import android.os.Bundle;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil;

import javax.inject.Inject;

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
public abstract class BaseDialog extends DialogFragment
{
	@Inject protected SoundSheetsDataAccess soundSheetsDataAccess;
	@Inject protected SoundSheetsDataStorage soundSheetsDataStorage;
	@Inject protected SoundSheetsDataUtil soundSheetsDataUtil;

	@Inject protected SoundsDataStorage soundsDataStorage;
	@Inject protected SoundsDataAccess soundsDataAccess;

	private DialogBaseLayout mainView;

	public DialogBaseLayout getMainView()
	{
		return mainView;
	}

	public void setMainView(DialogBaseLayout mainView)
	{
		this.mainView = mainView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		DynamicSoundboardApplication.getApplicationComponent().inject(this);
	}

	public SoundActivity getSoundActivity()
	{
		return (SoundActivity) this.getActivity();
	}
}
