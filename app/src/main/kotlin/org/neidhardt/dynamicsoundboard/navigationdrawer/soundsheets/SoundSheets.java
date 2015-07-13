package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerList;
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage;
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration;


public class SoundSheets
		extends
			NavigationDrawerList
{
	private SoundSheetsPresenter presenter;
	private SoundSheetsAdapter adapter;

	private  SoundsDataAccess soundsDataAccess = DynamicSoundboardApplication.getApplicationComponent().getSoundsDataAccess();
	private  SoundsDataStorage soundsDataStorage = DynamicSoundboardApplication.getApplicationComponent().getSoundsDataStorage();

	private  SoundSheetsDataAccess soundSheetsDataAccess = DynamicSoundboardApplication.getApplicationComponent().getSoundSheetsDataAccess();
	private  SoundSheetsDataStorage soundSheetsDataStorage = DynamicSoundboardApplication.getApplicationComponent().getSoundSheetsDataStorage();

	@SuppressWarnings("unused")
	public SoundSheets(Context context)
	{
		super(context);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public SoundSheets(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public SoundSheets(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.init(context);
	}

	private void init(Context context)
	{
		this.presenter = new SoundSheetsPresenter(this.soundSheetsDataAccess, this.soundSheetsDataStorage, this.soundsDataAccess, this.soundsDataStorage);
		this.adapter = new SoundSheetsAdapter(this.presenter);

		this.presenter.setAdapter(this.adapter);

		LayoutInflater.from(context).inflate(R.layout.view_sound_sheets, this, true);

		RecyclerView soundSheets = (RecyclerView) this.findViewById(R.id.rv_sound_sheets);
		if (!this.isInEditMode())
		{
			soundSheets.setItemAnimator(new DefaultItemAnimator());
			soundSheets.setLayoutManager(new LinearLayoutManager(context));
			soundSheets.addItemDecoration(new DividerItemDecoration());
		}
		soundSheets.setAdapter(this.adapter);
	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		this.adapter.onAttachedToWindow();
		this.presenter.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		this.adapter.onDetachedFromWindow();
		this.presenter.onDetachedFromWindow();
		super.onDetachedFromWindow();
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		this.presenter.setView(this);
	}

	public SoundSheetsAdapter getAdapter()
	{
		return this.adapter;
	}

	@Override
	protected int getActionModeTitle()
	{
		return R.string.cab_title_delete_sound_sheets;
	}

	@Override
	protected int getItemCount()
	{
		return this.adapter.getItemCount();
	}

	@Override
	public NavigationDrawerListPresenter getPresenter()
	{
		return this.presenter;
	}
}

