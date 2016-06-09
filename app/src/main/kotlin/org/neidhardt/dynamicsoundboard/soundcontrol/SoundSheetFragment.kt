package org.neidhardt.dynamicsoundboard.soundcontrol

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_soundsheet.*
import kotlinx.android.synthetic.main.layout_fab.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.fileexplorer.AddNewSoundFromDirectoryDialog
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEventListener
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.misc.registerIfRequired
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OnOpenSoundDialogEventListener
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundRenameEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundSettingsEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.views.SoundPresenter
import org.neidhardt.dynamicsoundboard.soundcontrol.views.createSoundPresenter
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.ConfirmDeleteSoundsDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.RenameSoundFileDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.SoundSettingsDialog
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.ConfirmDeleteSoundSheetDialog
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.AddPauseFloatingActionButton
import org.neidhardt.ui_utils.helper.SnackbarPresenter
import org.neidhardt.ui_utils.recyclerview.decoration.DividerItemDecoration

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
		OnSoundsChangedEventListener,
		MediaPlayerFailedEventListener
{
	private val LOG_TAG = javaClass.name

	var fragmentTag: String = javaClass.name

	private val eventBus = EventBus.getDefault()
	private val soundsDataStorage: SoundsDataStorage = SoundboardApplication.soundsDataStorage
	private val soundsDataAccess: SoundsDataAccess = SoundboardApplication.soundsDataAccess

	private var soundPresenter: SoundPresenter? = null
	private var snackbar: Snackbar? = null

	private val floatingActionButton: AddPauseFloatingActionButton? by lazy { this.fb_layout_fab }
	override val coordinatorLayout: CoordinatorLayout by lazy { this.cl_fragment_sound_sheet }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.retainInstance = true
		this.setHasOptionsMenu(true)

		val args = this.arguments
		val fragmentTag: String? = args.getString(KEY_FRAGMENT_TAG)
				?: throw NullPointerException(LOG_TAG + ": cannot create fragment, given fragmentTag is null")
		this.fragmentTag = fragmentTag as String
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		if (container == null)
			return null
		return inflater.inflate(R.layout.fragment_soundsheet, container, false)
	}

	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		this.rv_fragment_sound_sheet_sounds.let { soundList ->
			this.soundPresenter = createSoundPresenter(
					fragmentTag = this.fragmentTag,
					eventBus = this.eventBus,
					recyclerView = soundList,
					snackbarPresenter = this,
					soundsDataAccess = this.soundsDataAccess,
					soundsDataStorage = this.soundsDataStorage)

			soundList.apply {
				this.adapter = soundPresenter?.adapter
				this.layoutManager = LinearLayoutManager(this.context)
				this.addItemDecoration(DividerItemDecoration(this.context, R.color.background, R.color.divider))
			}
		}
	}

	override fun onStart()
	{
		super.onStart()
		this.eventBus.registerIfRequired(this)
	}

	override fun onResume()
	{
		super.onResume()

		this.baseActivity.apply {
			this.areSoundSheetActionsEnable = true
			this.actionAddSound?.setOnClickListener{ AddNewSoundDialog(this.supportFragmentManager, fragmentTag) }
			this.actionAddSoundDir?.setOnClickListener{ AddNewSoundFromDirectoryDialog.showInstance(this.supportFragmentManager, fragmentTag) }
		}

		this.soundPresenter?.onAttachedToWindow()
		this.attachScrollViewToFab()

		this.soundPresenter?.setProgressUpdateTimer(true)
	}

	override fun onPause()
	{
		super.onPause()
		this.soundPresenter?.onDetachedFromWindow()
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
		RenameSoundFileDialog.show(this.fragmentManager, event.data)
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

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerFailedEvent)
	{
		this.snackbar?.dismiss()
		val message = "${this.coordinatorLayout.context.resources.getString(R.string.sound_control_error_during_playback)}: ${event.player.mediaPlayerData.label}"
		this.snackbar = this.makeSnackbar(message, Snackbar.LENGTH_INDEFINITE, null).apply { this.show() }
	}

	override fun onEvent(event: SoundMovedEvent) {}
	override fun onEvent(event: SoundAddedEvent) {}
	override fun onEvent(event: SoundChangedEvent) {}
}