package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage;

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
	@Mock private SoundSheetsAdapter mockAdapter;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		MockitoAnnotations.initMocks(this);

		this.presenter = spy(new SoundSheetsPresenter(this.mockSoundSheetsDataAccess, this.mockSoundSheetsDataStorage, this.mockSoundsDataAccess));
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
}