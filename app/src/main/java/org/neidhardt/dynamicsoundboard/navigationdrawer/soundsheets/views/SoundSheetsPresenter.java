package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views;

import android.util.SparseArray;
import android.view.View;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundDataModel;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.OpenSoundSheetEvent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
public class SoundSheetsPresenter
		extends
			NavigationDrawerListPresenter<SoundSheets>
		implements
			SoundSheetsAdapter.OnItemClickListener
{
	private static final String TAG = SoundSheetsPresenter.class.getName();

	private SoundSheetsDataAccess soundSheetsDataAccess;
	private SoundSheetsDataStorage soundSheetsDataStorage;
	private SoundDataModel soundDataModel;
	private SoundSheetsAdapter adapter;

	@Override
	protected boolean isEventBusSubscriber()
	{
		return false;
	}

	@Override
	public void onDeleteSelected()
	{
		List<SoundSheet> soundSheetsToRemove = new ArrayList<>();
		List<SoundSheet> existingSoundSheets = this.adapter.getValues();

		for(SoundSheet soundSheet : existingSoundSheets)
		{
			if (soundSheet.isSelectedForDeletion())
				soundSheetsToRemove.add(soundSheet);
		}

		for (SoundSheet soundSheet: soundSheetsToRemove)
		{
			this.soundSheetsDataStorage.removeSoundSheet(soundSheet);
			if (soundSheet.getIsSelected())
			{
				List<SoundSheet> remainingSoundSheets = adapter.getValues();
				if (remainingSoundSheets.size() > 0)
					this.soundSheetsDataAccess.setSelectedItem(0);
			}
		}
		this.adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(View view, SoundSheet data, int position)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onItemClick failed, supplied view is null");

		if (this.isInSelectionMode())
		{
			super.onItemSelectedForDeletion();
			data.setIsSelectedForDeletion(!data.isSelectedForDeletion());
		}
		else
		{
			this.soundSheetsDataAccess.setSelectedItem(position);
			this.getEventBus().post(new OpenSoundSheetEvent(data));
		}
		this.adapter.notifyItemChanged(position);
	}

	void setSoundDataModel(SoundDataModel soundDataModel)
	{
		this.soundDataModel = soundDataModel;
	}

	void setSoundSheetsDataAccess(SoundSheetsDataAccess soundSheetsDataAccess)
	{
		this.soundSheetsDataAccess = soundSheetsDataAccess;
	}

	public void setSoundSheetsDataStorage(SoundSheetsDataStorage soundSheetsDataStorage)
	{
		this.soundSheetsDataStorage = soundSheetsDataStorage;
	}

	SoundSheetsDataAccess getSoundSheetsDataAccess()
	{
		return soundSheetsDataAccess;
	}

	SoundDataModel getSoundDataModel()
	{
		return soundDataModel;
	}

	public void setAdapter(SoundSheetsAdapter adapter)
	{
		this.adapter = adapter;
	}
}
