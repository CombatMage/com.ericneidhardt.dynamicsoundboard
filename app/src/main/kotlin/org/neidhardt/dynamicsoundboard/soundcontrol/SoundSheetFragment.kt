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
import org.neidhardt.android_utils.recyclerview_utils.decoration.DividerItemDecoration
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseFragment
import org.neidhardt.dynamicsoundboard.manager.RxSoundManager
import org.neidhardt.dynamicsoundboard.manager.findByFragmentTag
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerFactory
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEventListener
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OnOpenSoundDialogEventListener
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundRenameEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundSettingsEvent
import org.neidhardt.dynamicsoundboard.soundcontrol.views.PendingDeletionHandler
import org.neidhardt.dynamicsoundboard.soundcontrol.views.SoundPresenter
import org.neidhardt.dynamicsoundboard.soundcontrol.views.createSoundPresenter
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.ConfirmDeleteSoundsDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.RenameSoundFileDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.SoundSettingsDialog
import org.neidhardt.dynamicsoundboard.dialog.soundsheetmanagement.ConfirmDeleteSoundSheetDialog
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.AddPauseFloatingActionButtonView
import org.neidhardt.eventbus_utils.registerIfRequired
import org.neidhardt.ui_utils.helper.SnackbarPresenter
import org.neidhardt.ui_utils.helper.SnackbarView
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
private val KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment.fragmentTag"
private val KEY_STATE_RECYCLER_VIEW = KEY_FRAGMENT_TAG + "_recycler_view_state"

fun getNewInstance(soundSheet: NewSoundSheet): SoundSheetFragment {
	val fragment = SoundSheetFragment()
	val args = Bundle()
	args.putString(KEY_FRAGMENT_TAG, soundSheet.fragmentTag)
	fragment.arguments = args
	return fragment
}

class SoundSheetFragment :
		BaseFragment(),
		OnOpenSoundDialogEventListener,
		MediaPlayerFailedEventListener {

	private val LOG_TAG = javaClass.name

	override var fragmentTag: String = javaClass.name
	private val soundSheet: NewSoundSheet get() =
			this.soundSheetManager.soundSheets.findByFragmentTag(this.fragmentTag) ?: throw IllegalStateException("no match for fragmentTag found")

	private var subscriptions = CompositeSubscription()
	private val eventBus = EventBus.getDefault()
	private val soundSheetManager = SoundboardApplication.newSoundSheetManager
	private val soundManager = SoundboardApplication.newSoundManager
	private val playlistManager = SoundboardApplication.newPlaylistManager

	private var soundPresenter: SoundPresenter? = null

	private val snackbarPresenter = SnackbarPresenter()

	private val floatingActionButton: AddPauseFloatingActionButtonView? by lazy { this.fb_layout_fab }
	private val coordinatorLayout: CoordinatorLayout by lazy { this.cl_fragment_sound_sheet }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.retainInstance = true
		this.setHasOptionsMenu(true)

		val args = this.arguments
		val fragmentTag: String? = args.getString(KEY_FRAGMENT_TAG)
				?: throw NullPointerException(LOG_TAG + ": cannot create fragment, given fragmentTag is null")


		this.fragmentTag = fragmentTag as String
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (container == null)
			return null
		return inflater.inflate(R.layout.fragment_soundsheet, container, false)
	}

	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		this.snackbarPresenter.init(this.coordinatorLayout)

		val soundSheet = this.soundSheetManager.soundSheets.findByFragmentTag(this.fragmentTag)
				?: throw IllegalStateException("no SoundSheet for fragmentTag was found")

		this.rv_fragment_sound_sheet_sounds.let { soundList ->
			this.soundPresenter = createSoundPresenter(
					soundSheet = soundSheet,
					eventBus = this.eventBus,
					recyclerView = soundList,
					onItemDeletionRequested = { handler,time -> this.showSnackbarForRestore(handler, time) },
					soundManager = this.soundManager,
					playlistManager = this.playlistManager)

			soundList.apply {
				this.adapter = soundPresenter?.adapter
				this.layoutManager = LinearLayoutManager(this.context)
				this.addItemDecoration(DividerItemDecoration(this.context, R.color.background, R.color.divider))
			}
		}
	}

	private fun showSnackbarForRestore(deletionHandler: PendingDeletionHandler, timeTillDeletion: Int) {
		this.coordinatorLayout.context.resources.let { res ->
			val snackbarAction = SnackbarView.SnackbarAction(
					R.string.sound_control_deletion_pending_undo,
					{ deletionHandler.restoreDeletedItems() })

			val count = deletionHandler.countPendingDeletions
			val message = if (count == 1)
				res.getString(R.string.sound_control_deletion_pending_single)
			else
				res.getString(R.string.sound_control_deletion_pending).replace("{%s0}", count.toString())

			this.snackbarPresenter.showSnackbar(message, timeTillDeletion, snackbarAction)
		}
	}

	override fun onStart() {
		super.onStart()
		this.eventBus.registerIfRequired(this)
		this.subscriptions = CompositeSubscription()
		// if sounds where removed, the view becomes unscrollable and therefore the fab can not be reached
		this.subscriptions.add(RxSoundManager.changesSoundList(this.soundManager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { sounds -> if (sounds.isEmpty()) this.floatingActionButton?.visibility = View.VISIBLE })
	}

	override fun onResume() {
		super.onResume()

		this.baseActivity.let { it.toolbarVM.isSoundSheetActionsEnable = true }

		this.soundPresenter?.onAttachedToWindow()
		this.floatingActionButton?.show(true)
	}

	override fun onRestoreState(savedInstanceState: Bundle) {
		super.onRestoreState(savedInstanceState)
		this.rv_fragment_sound_sheet_sounds?.layoutManager?.onRestoreInstanceState(
				savedInstanceState.getParcelable(KEY_STATE_RECYCLER_VIEW))
	}

	override fun onSaveState(outState: Bundle) {
		super.onSaveState(outState)
		this.rv_fragment_sound_sheet_sounds?.layoutManager?.let { layoutManager ->
			outState.putParcelable(KEY_STATE_RECYCLER_VIEW, layoutManager.onSaveInstanceState())
		}
	}

	override fun onPause() {
		super.onPause()
		this.snackbarPresenter.stop()
		this.soundPresenter?.onDetachedFromWindow()
	}

	override fun onStop() {
		super.onStop()
		this.subscriptions.unsubscribe()
		this.eventBus.unregister(this)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == IntentRequest.GET_AUDIO_FILE) {
				val soundUri = data!!.data
				val soundLabel = FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(this.activity, soundUri))
				val playerData = MediaPlayerFactory.getNewMediaPlayerData(this.fragmentTag, soundUri, soundLabel)
				this.soundManager.add(this.soundSheet, playerData)
				return
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		super.onOptionsItemSelected(item)
		when (item.itemId) {
			R.id.action_clear_sounds_in_sheet -> {
				ConfirmDeleteSoundsDialog.showInstance(this.fragmentManager, this.fragmentTag)
				return true
			}
			R.id.action_delete_sheet -> {
				ConfirmDeleteSoundSheetDialog.showInstance(this.fragmentManager, this.fragmentTag)
				return true
			}
			else -> return false
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: OpenSoundRenameEvent) {
		RenameSoundFileDialog.show(this.fragmentManager, event.data)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: OpenSoundSettingsEvent) {
		SoundSettingsDialog.showInstance(this.fragmentManager, event.data)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerFailedEvent) {
		this.coordinatorLayout.context.resources.let { res ->
			val message = "${res.getString(R.string.sound_control_error_during_playback)}: " +
					event.player.mediaPlayerData.label
			this.snackbarPresenter.showSnackbar(message, Snackbar.LENGTH_INDEFINITE, null)
		}
	}

}