package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets

import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerList
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration


class SoundSheets : NavigationDrawerList
{
	private val soundsDataAccess = SoundboardApplication.getSoundsDataAccess()
	private val soundsDataStorage = SoundboardApplication.getSoundsDataStorage()

	private val soundSheetsDataAccess = SoundboardApplication.getSoundSheetsDataAccess()
	private val soundSheetsDataStorage = SoundboardApplication.getSoundSheetsDataStorage()

	var presenter: SoundSheetsPresenter = SoundSheetsPresenter(
			EventBus.getDefault(), this.soundSheetsDataAccess, this.soundSheetsDataStorage, this.soundsDataAccess, this.soundsDataStorage)
	var adapter: SoundSheetsAdapter  = SoundSheetsAdapter(this.presenter)

	@SuppressWarnings("unused") constructor(context: Context) : super(context)
	{
		this.init(context)
	}

	@SuppressWarnings("unused") constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
	{
		this.init(context)
	}

	@SuppressWarnings("unused") constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
	{
		this.init(context)
	}

	private fun init(context: Context)
	{
		this.presenter.adapter = this.adapter

		LayoutInflater.from(context).inflate(R.layout.view_sound_sheets, this, true)

		val soundSheets = this.findViewById(R.id.rv_sound_sheets) as RecyclerView
		if (!this.isInEditMode)
		{
			soundSheets.itemAnimator = DefaultItemAnimator()
			soundSheets.layoutManager = LinearLayoutManager(this.context)
			soundSheets.addItemDecoration(DividerItemDecoration(this.context))
		}
		soundSheets.adapter = this.adapter
	}

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		this.presenter.onAttachedToWindow()
	}

	override fun onDetachedFromWindow()
	{
		this.presenter.onDetachedFromWindow()
		super.onDetachedFromWindow()
	}

	override fun onFinishInflate()
	{
		super.onFinishInflate()
		this.presenter.view = this
	}

	override val itemCount: Int
		get() = presenter.values.size

	override val actionModeTitle: Int
		get() = R.string.cab_title_delete_sound_sheets

}

