package org.neidhardt.dynamicsoundboard.soundsheet;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import org.neidhardt.dynamicsoundboard.BaseActivity;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.customview.edittext.ActionbarEditText;

/**
 * Created by eric.neidhardt on 12.02.2015.
 */
public class TestSoundSheetManagerFragment extends ActivityInstrumentationTestCase2<BaseActivity>
{
	private static final String TAG = TestSoundSheetManagerFragment.class.getName();

	private Instrumentation instrumentation;
	private BaseActivity activity;
	private SoundSheetManagerFragment fragment;

	public TestSoundSheetManagerFragment()
	{
		super(BaseActivity.class);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.instrumentation = this.getInstrumentation();
		this.activity = this.getActivity();
		this.fragment = (SoundSheetManagerFragment)this.activity.getFragmentManager().findFragmentByTag(SoundSheetManagerFragment.TAG);
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
