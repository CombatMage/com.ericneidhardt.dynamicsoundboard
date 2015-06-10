package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.events.SoundSheetsRemovedEvent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.*;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.LoadSoundSheetsTask;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.StoreSoundSheetsTask;
import roboguice.util.SafeAsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * File created by eric.neidhardt on 02.06.2015.
 */
public class SoundSheetsManager
		implements
			SoundSheetsDataAccess,
			SoundSheetsDataStorage,
			SoundSheetsDataUtil,
			OnSoundSheetRenamedEventListener,
			OnOpenSoundSheetEventListener,
			OnSoundSheetsLoadedEventListener,
			OnSoundSheetsFromFileLoadedEventListener
{
	public static final String TAG = SoundSheetsManager.class.getName();

	private static final String DB_SOUND_SHEETS_DEFAULT = "org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment.db_sound_sheets";
	private static final String DB_SOUND_SHEETS = "db_sound_sheets";

	private DaoSession daoSession;
	List<SoundSheet> soundSheets;

	EventBus eventBus;

	public SoundSheetsManager()
	{
		this.eventBus = EventBus.getDefault();
	}

	@Override
	public void registerOnEventBus()
	{
		if (!this.eventBus.isRegistered(this))
			this.eventBus.registerSticky(this, 1);
	}

	@Override
	public void unregisterOnEventBus()
	{
		this.eventBus.unregister(this);
	}

	@Override
	public void init()
	{
		this.soundSheets = new ArrayList<>();
		this.daoSession = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), this.getDatabaseName());

		SafeAsyncTask task = new LoadSoundSheetsTask(this.daoSession);
		task.execute();
	}

	private String getDatabaseName()
	{
		String baseName = SoundLayoutsManager.getInstance().getActiveSoundLayout().getDatabaseId();
		if (baseName.equals(SoundLayoutsManager.DB_DEFAULT))
			return DB_SOUND_SHEETS_DEFAULT;
		return baseName + DB_SOUND_SHEETS;
	}

	@Override
	public List<SoundSheet> getSoundSheets()
	{
		return this.soundSheets;
	}

	@Override
	public String addOrUpdateSoundSheet(SoundSheet soundSheet)
	{
		SoundSheet existingSoundSheet = null;
		int index = this.soundSheets.indexOf(soundSheet);
		if (index == -1)
			this.soundSheets.add(soundSheet);
		else
		{
			existingSoundSheet = this.soundSheets.get(index);
			existingSoundSheet.setFragmentTag(soundSheet.getFragmentTag());
			existingSoundSheet.setLabel(soundSheet.getLabel());
		}
		this.eventBus.post(new SoundSheetsChangedEvent());
		if (existingSoundSheet == null)
			this.eventBus.post(new OpenSoundSheetEvent(soundSheet));
		else
			this.eventBus.post(new OpenSoundSheetEvent(existingSoundSheet));

		if (existingSoundSheet != null)
			return existingSoundSheet.getFragmentTag();
		else
			return soundSheet.getFragmentTag();
	}

	@Override
	public SoundSheet getSoundSheetForFragmentTag(String fragmentTag)
	{
		for (SoundSheet soundSheet : this.soundSheets)
		{
			if (soundSheet.getFragmentTag().equals(fragmentTag))
				return soundSheet;
		}
		return null;
	}

	@Override
	public void setSelectedItem(int position)
	{
		int size = this.soundSheets.size();
		for (int i = 0; i < size; i++)
		{
			boolean isSelected = i == position;
			this.soundSheets.get(i).setIsSelected(isSelected);
		}
	}

	@Override
	public SoundSheet getSelectedItem()
	{
		for (SoundSheet soundSheet : this.soundSheets)
		{
			if (soundSheet.getIsSelected())
				return soundSheet;
		}
		return null;
	}

	@Override
	public void writeCacheBack()
	{
		this.daoSession.getSoundSheetDao().deleteAll();
		SafeAsyncTask task = new StoreSoundSheetsTask(this.daoSession, this.soundSheets);
		task.execute();
	}

	@Override
	public String getSuggestedName()
	{
		return DynamicSoundboardApplication.getSoundboardContext().getResources().getString(R.string.suggested_sound_sheet_name) + this.soundSheets.size();
	}

	@Override
	public SoundSheet getNewSoundSheet(String label)
	{
		String tag = Integer.toString((label + DynamicSoundboardApplication.getRandomNumber()).hashCode());
		return new SoundSheet(null, tag, label, false);
	}

	@Override
	public void removeSoundSheet(SoundSheet soundSheet)
	{
		this.soundSheets.remove(soundSheet);
		this.eventBus.post(new SoundSheetsRemovedEvent(soundSheet));
	}

	@Override
	public void removeAllSoundSheets()
	{
		this.soundSheets.clear();
		this.eventBus.post(new SoundSheetsChangedEvent());
	}

	@Override
	public void onEvent(SoundSheetRenamedEvent event)
	{
		String renamedFragmentTag = event.getFragmentTag();
		String newLabel = event.getNewLabel();

		SoundSheet correspondingSoundSheetData = this.getSoundSheetForFragmentTag(renamedFragmentTag);
		if (correspondingSoundSheetData == null)
			throw new NullPointerException("sound sheet label was edited, but no sound sheet is selected");

		correspondingSoundSheetData.setLabel(newLabel);
	}

	@Override
	public void onEvent(OpenSoundSheetEvent event)
	{
		int indexOfSelectedItem = this.soundSheets.indexOf(event.getSoundSheetToOpen());
		this.setSelectedItem(indexOfSelectedItem);
		EventBus.getDefault().post(new SoundSheetsChangedEvent());
	}

	@Override
	public void onEventMainThread(SoundSheetsLoadedEvent event)
	{
		this.eventBus.removeStickyEvent(event);
		this.soundSheets.addAll(event.getLoadedSoundSheets());

		this.findSelectionAndDeselectOthers();

		this.eventBus.post(new SoundSheetsChangedEvent());
	}

	private void findSelectionAndDeselectOthers()
	{
		SoundSheet selected = null;
		if (this.soundSheets != null)
		{
			for (SoundSheet soundSheet : this.soundSheets)
			{
				if (soundSheet.getIsSelected() && selected == null)
					selected = soundSheet;
				else
					soundSheet.setIsSelected(false);
			}
		}
	}

	/**
	 * Called by LoadSoundSheetsTask when loading of soundsheets has been finished.
	 * @param event delivered SoundSheetsLoadedEvent
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(SoundSheetsRemovedEvent event)
	{
		this.soundSheets.remove(event.getRemovedSoundSheet());
	}

	@Override
	public void onEvent(SoundSheetsFromFileLoadedEvent event)
	{
		// clear SoundSheets before adding new values
		// this removes all sounds in SoundSheets, but no in playlist
		this.soundSheets.clear();
		this.daoSession.getSoundSheetDao().deleteAll();

		this.soundSheets.addAll(event.getNewSoundSheetList());
		this.eventBus.post(new SoundSheetsChangedEvent());
	}

}
