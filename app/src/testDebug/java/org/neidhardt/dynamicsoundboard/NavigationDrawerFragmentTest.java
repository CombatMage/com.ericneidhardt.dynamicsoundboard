package org.neidhardt.dynamicsoundboard;

import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

/**
 * Created by eric.neidhardt on 02.04.2015.
 */
public class NavigationDrawerFragmentTest extends AbstractBaseActivityTest
{
	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		assertSame(this.navigationDrawerFragment, this.navigationDrawerFragment.getNavigationDrawerFragment());
	}

	@Test
	public void testSetLayoutName() throws Exception
	{
		TextView textView = this.getCurrentSoundLayoutName();
		assertThat(textView.getText().toString(), equalTo(activity.getResources().getString(R.string.sound_layout_default)));

		String testString = "test";
		this.navigationDrawerFragment.setLayoutName(testString);
		assertThat(textView.getText().toString(), equalTo(testString));
	}

	protected TextView getCurrentSoundLayoutName()
	{
		return (TextView) this.activity.findViewById(R.id.tv_current_sound_layout_name);
	}
}