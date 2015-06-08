package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

import de.greenrobot.event.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * File created by eric.neidhardt on 04.06.2015.
 */
public class SoundSheetsManagerTest extends BaseTest
{
	private SoundSheetsManager manager;

	@Mock private EventBus mockEventBus;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		MockitoAnnotations.initMocks(this);

		this.manager = spy(new SoundSheetsManager());
		this.manager.eventBus = this.mockEventBus;
	}

	@Test
	public void testRegisterOnEventBus() throws Exception
	{
		when(this.mockEventBus.isRegistered(this.manager)).thenReturn(false);
		this.manager.registerOnEventBus();
		verify(this.mockEventBus, times(1)).registerSticky(this.manager, 1);
	}

	@Test
	public void testUnregisterOnEventBus() throws Exception
	{
		this.manager.unregisterOnEventBus();
		verify(this.mockEventBus, times(1)).unregister(this.manager);
	}

	@Test
	public void testGetSoundSheets() throws Exception
	{
		List<SoundSheet> soundSheetList = new ArrayList<>();
		this.manager.soundSheets = soundSheetList;

		assertThat(this.manager.getSoundSheets(), equalTo(soundSheetList));
	}
}