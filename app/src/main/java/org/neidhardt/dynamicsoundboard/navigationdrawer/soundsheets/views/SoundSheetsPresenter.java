package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views;

import android.util.SparseArray;
import android.view.View;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListPresenter;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.events.SoundSheetsRemovedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundDataModel;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.OpenSoundSheetEvent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 26.05.2015.
 */
public class SoundSheetsPresenter
		extends
			NavigationDrawerListPresenter<SoundSheets>
		implements
			SoundSheetsAdapter.OnItemClickListener
{
	private static final String TAG = SoundSheetsPresenter.class.getName();
	private SoundSheetsDataModel soundSheetsDataModel;
	private SoundDataModel soundDataModel;

	@Override
	protected boolean isEventBusSubscriber()
	{
		return false;
	}

	@Override
	public void onDeleteSelected(SparseArray<View> selectedItems)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onDeleteSelected failed, supplied view is null");

		SoundSheetsAdapter adapter = this.getView().getAdapter();

		List<SoundSheet> soundSheetsToRemove = new ArrayList<>(selectedItems.size());
		for(int i = 0; i < selectedItems.size(); i++)
		{
			int index = selectedItems.keyAt(i);
			soundSheetsToRemove.add(adapter.getValues().get(index));
		}

		for (SoundSheet soundSheet: soundSheetsToRemove)
		{
			this.getEventBus().post(new SoundSheetsRemovedEvent(soundSheet));
			if (soundSheet.getIsSelected())
			{
				List<SoundSheet> remainingSoundSheets = adapter.getValues();
				if (remainingSoundSheets.size() > 0)
					this.soundSheetsDataModel.setSelectedItem(0);
			}
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(View view, SoundSheet data, int position)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onItemClick failed, supplied view is null");

		if (super.isInSelectionMode)
			super.onItemSelected(view, position);
		else
		{
			this.soundSheetsDataModel.setSelectedItem(position);
			this.getEventBus().post(new OpenSoundSheetEvent(data));
		}
	}

	void setSoundDataModel(SoundDataModel soundDataModel)
	{
		this.soundDataModel = soundDataModel;
	}

	void setSoundSheetsDataModel(SoundSheetsDataModel soundSheetsDataModel)
	{
		this.soundSheetsDataModel = soundSheetsDataModel;
	}

	SoundSheetsDataModel getSoundSheetsDataModel() {
		return soundSheetsDataModel;
	}

	SoundDataModel getSoundDataModel() {
		return soundDataModel;
	}
}
