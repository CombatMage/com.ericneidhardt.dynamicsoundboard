package org.neidhardt.dynamicsoundboard.soundlayouts;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import org.junit.Test;
import org.neidhardt.dynamicsoundboard.NavigationDrawerFragmentTest;
import org.neidhardt.dynamicsoundboard.R;
import org.robolectric.Robolectric;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

/**
 * Created by eric.neidhardt on 03.04.2015.
 */
public class SoundLayoutsListTest extends NavigationDrawerFragmentTest
{

	// TODO debug layout count is 0
	@Test
	public void testOnDeleteSelected() throws Exception
	{
		SoundLayoutsList soundLayoutsList = (SoundLayoutsList) this.activity.findViewById(R.id.layout_select_sound_layout);
		assertNotNull(soundLayoutsList);

		final RecyclerView listView = (RecyclerView) soundLayoutsList.findViewById(R.id.rv_sound_layouts_list);
		assertNotNull(listView);

		int childCount = soundLayoutsList.getChildCount();
		SparseArray<View> children = new SparseArray<>(childCount);
		for (int i = 0; i < childCount; i++)
			children.put(i, listView.getChildAt(i));

		soundLayoutsList.onDeleteSelected(children);

		assertThat(SoundLayoutsManager.getInstance().getSoundLayouts().size(), equalTo(1));
		assertTrue(SoundLayoutsManager.getInstance().getActiveSoundLayout().isDefaultLayout());

		soundLayoutsList.post(new Runnable() // wait for nex rendering
		{
			@Override
			public void run()
			{
				assertThat(listView.getChildCount(), equalTo(1)); // only the default item is left
			}
		});
	}
}