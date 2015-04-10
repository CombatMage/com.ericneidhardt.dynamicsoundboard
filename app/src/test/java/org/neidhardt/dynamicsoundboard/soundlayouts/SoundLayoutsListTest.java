package org.neidhardt.dynamicsoundboard.soundlayouts;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import org.junit.Test;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.NavigationDrawerFragmentTest;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

/**
 * Created by eric.neidhardt on 03.04.2015.
 */
public class SoundLayoutsListTest extends NavigationDrawerFragmentTest
{
	private static final int NR_TEST_ITEMS = 3;

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		for (long i = 0; i < NR_TEST_ITEMS; i++)
			SoundLayoutsManager.getInstance().addSoundLayout(this.getRandomSoundLayout());
	}

	private SoundLayout getRandomSoundLayout()
	{
		SoundLayout testLayout = new SoundLayout();
		testLayout.setLabel("test");
		testLayout.setDatabaseId(Integer.toString(DynamicSoundboardApplication.getRandomNumber()));
		testLayout.setIsSelected(false);
		return testLayout;
	}

	@Test
	public void testOnDeleteSelected() throws Exception
	{
		SoundLayoutsList soundLayoutsList = (SoundLayoutsList) this.activity.findViewById(R.id.layout_select_sound_layout);
		assertNotNull(soundLayoutsList);

		RecyclerView listView = (RecyclerView) soundLayoutsList.findViewById(R.id.rv_sound_layouts_list);
		assertNotNull(listView);
		this.triggerRecyclerViewRelayout(listView);

		assertThat(listView.getAdapter().getItemCount(), equalTo(NR_TEST_ITEMS + 1)); // test items + default item

		int childCount = listView.getChildCount();
		assertThat(childCount, equalTo(NR_TEST_ITEMS + 1));

		SparseArray<View> children = new SparseArray<>(childCount);
		for (int i = 0; i < childCount; i++)
			children.put(i, listView.getChildAt(i));

		soundLayoutsList.onDeleteSelected(children);

		assertThat(SoundLayoutsManager.getInstance().getSoundLayouts().size(), equalTo(1));
		assertTrue(SoundLayoutsManager.getInstance().getActiveSoundLayout().isDefaultLayout());

		triggerRecyclerViewRelayout(listView);
		assertThat(listView.getChildCount(), equalTo(1)); // only the default item is left
	}

	/**
	 * Robolectric does no trigger relayout of RecyclerView. This must be done manually.
	 * * @param recyclerView view to update
	 */
	private void triggerRecyclerViewRelayout(RecyclerView recyclerView)
	{
		recyclerView.getAdapter().notifyDataSetChanged();
		recyclerView.measure(0, 0);
		recyclerView.layout(0, 0, 100, 10000);
	}
}