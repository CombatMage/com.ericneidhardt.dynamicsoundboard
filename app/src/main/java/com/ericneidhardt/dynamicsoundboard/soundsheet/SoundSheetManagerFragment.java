package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

import java.util.List;
import java.util.Random;

/**
 * Created by Eric Neidhardt on 29.08.2014.
 */
public class SoundSheetManagerFragment extends Fragment implements View.OnClickListener, SoundSheetAdapter.OnItemClickedListener
{
	public static final String TAG = SoundSheetManagerFragment.class.getSimpleName();

	private static final String DB_SOUND_SHEETS = "com.ericneidhardt.dynamicsoundboard.db_sound_sheets";

	private SoundSheetAdapter soundSheetAdapter;
	private DaoSession daoSession;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		this.soundSheetAdapter = new SoundSheetAdapter();
		this.daoSession = Util.setupDatabase(this.getActivity(), DB_SOUND_SHEETS);

		LoadSoundSheetsTask task = new LoadSoundSheetsTask();
		task.execute();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		RecyclerView recyclerView = (RecyclerView) this.getActivity().findViewById(R.id.rv_navigation);
		recyclerView.setAdapter(this.soundSheetAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		recyclerView.setItemAnimator(new DefaultItemAnimator());

		this.getActivity().findViewById(R.id.action_new_sound_sheet).setOnClickListener(this);

		this.soundSheetAdapter.setOnItemClickedListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		StoreSoundSheetsTask task = new StoreSoundSheetsTask(this.soundSheetAdapter.getValues());
		task.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_clear_sound_sheets:
				Toast.makeText(this.getActivity(), "action_clear_sound_sheets", Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.action_new_sound_sheet:
				this.openDialogAddNewSoundLayout();
				break;
			default:
				Logger.e(TAG, "unknown item clicked " + view);
		}
	}

	@Override
	public void onItemClicked(View view, SoundSheet data, int position)
	{
		if (this.getActivity() != null)
		{
			BaseActivity activity = (BaseActivity)this.getActivity();
			activity.toggleNavigationDrawer();
			activity.openSoundFragment(data);
		}
	}

	private void openDialogAddNewSoundLayout()
	{
		final View dialogView = LayoutInflater.from(this.getActivity()).inflate(R.layout.dialog_add_new_sound_layout, null);
		((EditText)dialogView.findViewById(R.id.et_input)).setText("test" + this.soundSheetAdapter.getItemCount());

		AlertDialog.Builder inputNameDialog = new AlertDialog.Builder(this.getActivity());
		inputNameDialog.setView(dialogView);

		final AlertDialog dialog = inputNameDialog.create();
		dialogView.findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.dismiss();
			}
		});
		dialogView.findViewById(R.id.b_ok).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String label = ((EditText)dialogView.findViewById(R.id.et_input)).getText().toString();
				String tag = Integer.toString((label + DynamicSoundboardApplication.getRandomNumber()).hashCode());
				soundSheetAdapter.add(new SoundSheet(null, tag, label));
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private class LoadSoundSheetsTask extends SafeAsyncTask<List<SoundSheet>>
	{
		@Override
		public List<SoundSheet> call() throws Exception
		{
			return daoSession.getSoundSheetDao().queryBuilder().list();
		}

		@Override
		protected void onSuccess(List<SoundSheet> soundSheets) throws Exception
		{
			super.onSuccess(soundSheets);
			soundSheetAdapter.addAll(soundSheets);
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG, e.getMessage());
		}
	}

	private class StoreSoundSheetsTask extends SafeAsyncTask<Void>
	{
		private List<SoundSheet> soundSheets;

		private StoreSoundSheetsTask(List<SoundSheet> soundSheets)
		{
			this.soundSheets = soundSheets;
		}

		@Override
		public Void call() throws Exception
		{
			daoSession.getSoundSheetDao().insertOrReplaceInTx(soundSheets);
			return null;
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG, e.getMessage());
		}
	}
}
