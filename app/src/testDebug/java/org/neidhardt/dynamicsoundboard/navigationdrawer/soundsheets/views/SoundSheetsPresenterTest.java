package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * File created by eric.neidhardt on 10.06.2015.
 */
public class SoundSheetsPresenterTest extends BaseTest
{
	private SoundSheetsPresenter presenter;

	@Mock private SoundSheetsDataAccess mockSoundSheetsDataAccess;
	@Mock private SoundSheetsDataStorage mockSoundSheetsDataStorage;
	@Mock private SoundsDataAccess mockSoundsDataAccess;
	@Mock private SoundsDataStorage mockSoundsDataStorage;
	@Mock private SoundSheetsAdapter mockAdapter;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		MockitoAnnotations.initMocks(this);

		this.presenter = spy(new SoundSheetsPresenter(this.mockSoundSheetsDataAccess, this.mockSoundSheetsDataStorage, this.mockSoundsDataAccess, this.mockSoundsDataStorage));
		this.presenter.setAdapter(this.mockAdapter);
	}

	@Test
	public void testOnItemClick() throws Exception
	{
		this.presenter.setIsInSelectionMode(true);
		SoundSheet data = mock(SoundSheet.class);
		when(data.isSelectedForDeletion()).thenReturn(false);

		this.presenter.onItemClick(null, data, 0);

		verify(data, times(1)).setIsSelectedForDeletion(true);
	}

	@Test
	public void testDeleteSelectedItems() throws Exception
	{
		SoundSheet soundSheet = mock(SoundSheet.class);
		when(soundSheet.getFragmentTag()).thenReturn("testTag");
		when(soundSheet.isSelectedForDeletion()).thenReturn(true);

		when(this.mockAdapter.getValues()).thenReturn(Collections.singletonList(soundSheet));

		List<EnhancedMediaPlayer> soundsInSoundSheet = new ArrayList<>();
		when(this.mockSoundsDataAccess.getSoundsInFragment("testTag")).thenReturn(soundsInSoundSheet);

		this.presenter.deleteSelectedItems();

		verify(this.mockSoundSheetsDataStorage, times(1)).removeSoundSheet(soundSheet);
		verify(this.mockAdapter, times(1)).notifyDataSetChanged();
		verify(this.mockSoundsDataStorage, times(1)).removeSounds(soundsInSoundSheet);
	}
}