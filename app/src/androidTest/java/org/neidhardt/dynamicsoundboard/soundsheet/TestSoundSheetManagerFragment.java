package org.neidhardt.dynamicsoundboard.soundsheetmanagement;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.views.edittext.ActionbarEditText;

/**
 * Created by eric.neidhardt on 12.02.2015.
 */
public class TestSoundSheetManagerFragment extends ActivityInstrumentationTestCase2<SoundActivity>
{
	private static final String TAG = TestSoundSheetManagerFragment.class.getName();

	private Instrumentation instrumentation;
	private SoundActivity activity;
	private SoundSheetsManagerFragment fragment;

	public TestSoundSheetManagerFragment()
	{
		super(SoundActivity.class);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.instrumentation = this.getInstrumentation();
		this.activity = this.getActivity();
		this.fragment = (SoundSheetsManagerFragment)this.activity.getFragmentManager().findFragmentByTag(SoundSheetsManagerFragment.TAG);
	}

	public void testOnTextEdited() throws Exception
	{
		// isolation test
		Log.d(TAG, "isolation test");
		String textChanged = "test";
		this.fragment.onTextEdited(textChanged);

		// test in activity context
		Log.d(TAG, "test in activity context");
		final ActionbarEditText labelCurrentSoundSheet = (ActionbarEditText)this.activity.findViewById(R.id.et_set_label);
		assertNotNull(labelCurrentSoundSheet);
		labelCurrentSoundSheet.setText("test2");

		// test fragment
		this.fragment.onTextEdited("test3");
	}
}
