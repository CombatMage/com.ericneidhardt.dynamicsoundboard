package org.neidhardt.dynamicsoundboard.soundcontrol

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.fileexplorer.AddNewSoundFromDirectoryDialog
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OnOpenSoundDialogEventListener
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundRenameEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundSettingsEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.views.SoundPresenter
import org.neidhardt.dynamicsoundboard.soundcontrol.views.createSoundPresenter
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.RenameSoundFileDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundmanagement.views.ConfirmDeleteSoundsDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.views.SoundSettingsDialog
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.ConfirmDeleteSoundSheetDialog
import org.neidhardt.dynamicsoundboard.views.SnackbarPresenter
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.AddPauseFloatingActionButton
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
private val KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment.fragmentTag"

fun getNewInstance(soundSheet: SoundSheet): SoundSheetFragment
{
	val fragment = SoundSheetFragment()
	val args = Bundle()
	args.putString(KEY_FRAGMENT_TAG, soundSheet.fragmentTag)
	fragment.arguments = args
	return fragment
}

class SoundSheetFragment :
		BaseFragment(),
		SnackbarPresenter,
		OnOpenSoundDialogEventListener,
		OnSoundsChangedEventListener
{
	private val LOG_TAG = javaClass.name

	var fragmentTag: String = javaClass.name

	private val eventBus = EventBus.getDefault()
	private val soundsDataStorage: SoundsDataStorage = SoundboardApplication.getSoundsDataStorage()
	private val soundsDataAccess: SoundsDataAccess = SoundboardApplication.getSoundsDataAccess()

	private var floatingActionButton: AddPauseFloatingActionButton? = null
	private var soundPresenter: SoundPresenter? = null
	private var mainLayout: CoordinatorLayout? = null

	override val coordinatorLayout: CoordinatorLayout
		get() = this.mainLayout as CoordinatorLayout

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.retainInstance = true
		this.setHasOptionsMenu(true)

		val args = this.arguments
		var fragmentTag: String? = args.getString(KEY_FRAGMENT_TAG)
				?: throw NullPointerException(LOG_TAG + ": cannot create fragment, given fragmentTag is null")
		this.fragmentTag = fragmentTag as String
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		if (container == null)
			return null

		val fragmentView = inflater.inflate(R.layout.fragment_soundsheet, container, false)

		this.floatingActionButton = fragmentView.findViewById(R.id.fab) as AddPauseFloatingActionButton?
		this.mainLayout = fragmentView.findViewById(R.id.coordinator_layout) as CoordinatorLayout

		val soundLayout = fragmentView.findViewById(R.id.rv_sounds) as RecyclerView

		this.soundPresenter = createSoundPresenter(
				fragmentTag = this.fragmentTag,
				eventBus = this.eventBus,
				recyclerView = soundLayout,
				soundsDataAccess = this.soundsDataAccess,
				soundsDataStorage = this.soundsDataStorage)

		soundLayout.apply {
			this.adapter = soundPresenter?.adapter
			this.layoutManager = LinearLayoutManager(this.context)
			// TODO
			//this.itemAnimator = SlideInLeftAnimator().apply { this.supportsChangeAnimations = false }
			this.addItemDecoration(DividerItemDecoration(this.context))
		}

		return fragmentView
	}

	override fun onStart()
	{
		super.onStart()
		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this)
	}

	override fun onResume()
	{
		super.onResume()

		this.baseActivity.apply {
			this.setSoundSheetActionsEnable(true)
			this.findViewById(R.id.action_add_sound)?.setOnClickListener({ AddNewSoundDialog(this.supportFragmentManager, fragmentTag) })
			this.findViewById(R.id.action_add_sound_dir)?.setOnClickListener({ AddNewSoundFromDirectoryDialog.showInstance(this.supportFragmentManager, fragmentTag) })
		}

		this.soundPresenter?.onAttachedToWindow()
		this.attachScrollViewToFab()

		this.soundPresenter?.setProgressUpdateTimer(true)
	}

	override fun onPause()
	{
		super.onPause()
		this.soundPresenter!!.onDetachedFromWindow()
		this.soundPresenter?.setProgressUpdateTimer(false)
	}

	override fun onStop()
	{
		super.onStop()
		this.eventBus.unregister(this)
	}

	private fun attachScrollViewToFab()
	{
		this.floatingActionButton?.show(true)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == IntentRequest.GET_AUDIO_FILE)
			{
				val soundUri = data!!.data
				val soundLabel = FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(this.activity, soundUri))
				val playerData = MediaPlayerData.getNewMediaPlayerData(this.fragmentTag, soundUri, soundLabel)

				this.soundsDataStorage.createSoundAndAddToManager(playerData)
				return
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		super.onOptionsItemSelected(item)
		when (item.itemId)
		{
			R.id.action_clear_sounds_in_sheet ->
			{
				ConfirmDeleteSoundsDialog.showInstance(this.fragmentManager, this.fragmentTag)
				return true
			}
			R.id.action_delete_sheet ->
			{
				ConfirmDeleteSoundSheetDialog.showInstance(this.fragmentManager, this.fragmentTag)
				return true
			}
			else -> return false
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: OpenSoundRenameEvent)
	{
		RenameSoundFileDialog(this.fragmentManager, event.data)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: OpenSoundSettingsEvent)
	{
		SoundSettingsDialog.showInstance(this.fragmentManager, event.data)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundsRemovedEvent)
	{
		if (this.soundPresenter?.values?.size == 0)
			this.floatingActionButton?.show(true)
	}

	override fun onEvent(event: SoundMovedEvent) {}

	override fun onEvent(event: SoundAddedEvent) {}

	override fun onEvent(event: SoundChangedEvent) {}
}