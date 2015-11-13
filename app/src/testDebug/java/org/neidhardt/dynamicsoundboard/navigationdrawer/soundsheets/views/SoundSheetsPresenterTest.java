package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views;

import de.greenrobot.event.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.SoundSheetsAdapter;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.SoundSheetsPresenter;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage;

import static org.mockito.Mockito.*;

/**
 * File created by eric.neidhardt on 10.06.2015.
 */
public class SoundSheetsPresenterTest extends BaseTest
{
	private SoundSheetsPresenter presenter;

	@Mock private EventBus eventBus;
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

		this.presenter = spy(new SoundSheetsPresenter(this.eventBus,
				this.mockSoundSheetsDataAccess,
				this.mockSoundSheetsDataStorage,
				this.mockSoundsDataAccess,
				this.mockSoundsDataStorage));
		this.presenter.setAdapter(this.mockAdapter);
	}

	@Test
	public void testOnItemClick() throws Exception
	{
		this.presenter.setInSelectionMode(true);
		SoundSheet data = mock(SoundSheet.class);
		when(data.getIsSelectedForDeletion()).thenReturn(false);

		this.presenter.onItemClick(data);

		verify(data, times(1)).setIsSelectedForDeletion(true);
	}

}