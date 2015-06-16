package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views;

import android.view.View;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.OpenSoundSheetEvent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage;

import javax.inject.Inject;
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
	private SoundSheetsDataAccess soundSheetsDataAccess;
	private SoundSheetsDataStorage soundSheetsDataStorage;
	private SoundsDataAccess soundsDataAccess;
	private SoundSheetsAdapter adapter;

	@Inject
	public SoundSheetsPresenter(SoundSheetsDataAccess soundSheetsDataAccess, SoundSheetsDataStorage soundSheetsDataStorage, SoundsDataAccess soundsDataAccess)
	{
		this.soundSheetsDataAccess = soundSheetsDataAccess;
		this.soundSheetsDataStorage = soundSheetsDataStorage;
		this.soundsDataAccess = soundsDataAccess;
	}

	@Override
	protected boolean isEventBusSubscriber()
	{
		return false;
	}

	@Override
	public void deleteSelectedItems()
	{
		List<SoundSheet> soundSheetsToRemove = this.getSoundSheetsSelectedForDeletion();
		for (SoundSheet soundSheet: soundSheetsToRemove)
		{
			this.soundSheetsDataStorage.removeSoundSheet(soundSheet);
			if (soundSheet.getIsSelected())
			{
				List<SoundSheet> remainingSoundSheets = this.adapter.getValues();
				if (remainingSoundSheets.size() > 0)
					this.soundSheetsDataAccess.setSelectedItem(0);
			}
		}
		this.adapter.notifyDataSetChanged();
		super.onSelectedItemsDeleted();
	}

	@Override
	public void onItemClick(View view, SoundSheet data, int position)
	{
		if (this.isInSelectionMode())
		{
			data.setIsSelectedForDeletion(!data.isSelectedForDeletion());
			super.onItemSelectedForDeletion();
		}
		else
		{
			this.soundSheetsDataAccess.setSelectedItem(position);
			this.getEventBus().post(new OpenSoundSheetEvent(data));
		}
		this.adapter.notifyItemChanged(position);
	}

	@Override
	protected int getNumberOfItemsSelectedForDeletion()
	{
		return this.getSoundSheetsSelectedForDeletion().size();
	}

	@Override
	protected void deselectAllItemsSelectedForDeletion()
	{
		List<SoundSheet> selectedSoundSheets = this.getSoundSheetsSelectedForDeletion();
		for (SoundSheet soundSheet : selectedSoundSheets)
		{
			soundSheet.setIsSelectedForDeletion(false);
			this.adapter.notifyItemChanged(soundSheet);
		}
	}

	private List<SoundSheet> getSoundSheetsSelectedForDeletion()
	{
		List<SoundSheet> selectedSoundSheets = new ArrayList<>();
		List<SoundSheet> existingSoundSheets = this.adapter.getValues();
		for(SoundSheet soundSheet : existingSoundSheets)
		{
			if (soundSheet.isSelectedForDeletion())
				selectedSoundSheets.add(soundSheet);
		}
		return selectedSoundSheets;
	}

	void setSoundSheetsDataAccess(SoundSheetsDataAccess soundSheetsDataAccess)
	{
		this.soundSheetsDataAccess = soundSheetsDataAccess;
	}

	public void setSoundSheetsDataStorage(SoundSheetsDataStorage soundSheetsDataStorage)
	{
		this.soundSheetsDataStorage = soundSheetsDataStorage;
	}

	public void setAdapter(SoundSheetsAdapter adapter)
	{
		this.adapter = adapter;
	}

	public List<SoundSheet> getSoundSheets()
	{
		return this.soundSheetsDataAccess.getSoundSheets();
	}

	public List<EnhancedMediaPlayer> getSoundsInFragment(String fragmentTag)
	{
		return this.soundsDataAccess.getSoundsInFragment(fragmentTag);
	}
}
