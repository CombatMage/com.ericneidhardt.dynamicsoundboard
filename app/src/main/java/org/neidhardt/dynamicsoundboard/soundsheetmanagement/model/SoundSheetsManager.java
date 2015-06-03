package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.events.SoundSheetsRemovedEvent;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.soundmanagement.MusicService;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.*;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.LoadSoundSheetsTask;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.StoreSoundSheetTask;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.StoreSoundSheetsTask;
import roboguice.util.SafeAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 02.06.2015.
 */
public class SoundSheetsManager
		implements
			SoundSheetsDataModel,
			SoundSheetsDataStorage,
			OnSoundSheetRenamedEventListener,
			OnOpenSoundSheetEventListener,
			OnSoundSheetsLoadedEventListener
{
	public static final String TAG = SoundSheetsManager.class.getName();

	private static final String DB_SOUND_SHEETS_DEFAULT = "org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment.db_sound_sheets";
	private static final String DB_SOUND_SHEETS = "db_sound_sheets";

	private List<SoundSheet> soundSheets;
	private DaoSession daoSession;

	private EventBus eventBus;

	public SoundSheetsManager()
	{
		this.eventBus = EventBus.getDefault();
	}

	@Override
	public void registerOnEventBus()
	{
		if (this.eventBus.isRegistered(this))
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

	/**
	 * Called by greenRobot eventBus when SoundSheets have been loaded form file.
	 * This sheets needed to be added to the database.
	 * @param event delivered SoundSheetsFromFileLoadedEvent
	 */
	@SuppressWarnings("unused")
	public void onEvent(SoundSheetsFromFileLoadedEvent event)
	{
		// clear SoundSheets before adding new values
		// this removes all sounds in SoundSheets, but no in playlist
		this.deleteAllSoundSheets();
		this.soundSheets.addAll(event.getSoundSheetList());

		this.eventBus.post(new SoundSheetsChangedEvent());

		for (SoundSheet soundSheet : soundSheets)
		{
			SafeAsyncTask task = new StoreSoundSheetTask(this.daoSession, soundSheet);
			task.execute();
		}
	}

	private void deleteAllSoundSheets()
	{
		SoundActivity activity = (SoundActivity) this.getActivity();
		activity.removeSoundFragment(this.soundSheets);
		activity.setSoundSheetActionsEnable(false);

		ServiceManagerFragment fragment = this.getServiceManagerFragment();
		MusicService service = fragment.getSoundService();
		for (SoundSheet soundSheet : this.soundSheets)
		{
			List<EnhancedMediaPlayer> soundsInSoundSheet = fragment.getSounds().get(soundSheet.getFragmentTag());
			service.removeSounds(soundsInSoundSheet);
		}
		this.soundSheets.clear();
		this.daoSession.getSoundSheetDao().deleteAll();
	}

}
