package org.neidhardt.dynamicsoundboard.soundsheetfragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import kotlinx.android.synthetic.main.fragment_soundsheet.*
import kotlinx.android.synthetic.main.fragment_soundsheet.view.*
import kotlinx.android.synthetic.main.layout_fab.*
import kotlinx.android.synthetic.main.layout_fab.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.android_utils.recyclerview_utils.decoration.DividerItemDecoration
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseFragment
import org.neidhardt.dynamicsoundboard.dialog.GenericConfirmDialogs
import org.neidhardt.dynamicsoundboard.dialog.GenericRenameDialogs
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.AddNewSoundFromDirectoryDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.SoundSettingsDialog
import org.neidhardt.dynamicsoundboard.manager.findByFragmentTag
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.events.*
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.misc.registerIfRequired
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.dynamicsoundboard.soundsheetfragment.viewhelper.ItemTouchCallback
import org.neidhardt.dynamicsoundboard.soundsheetfragment.viewhelper.PendingDeletionHandler
import org.neidhardt.dynamicsoundboard.soundsheetfragment.viewhelper.SoundAdapter
import org.neidhardt.dynamicsoundboard.views.sound_control.PlayButton
import org.neidhardt.dynamicsoundboard.views.sound_control.ToggleLoopButton
import org.neidhardt.dynamicsoundboard.views.sound_control.TogglePlaylistButton

/**
 * Created by eric.neidhardt@gmail.com on 08.05.2017.
 */
class SoundSheetFragment : BaseFragment(),
		SoundSheetFragmentContract.View,
		MediaPlayerEventListener,
		MediaPlayerFailedEventListener {

	companion object {
		internal val KEY_FRAGMENT_TAG = "SoundSheetFragment.KEY_FRAGMENT_TAG"
		fun getNewInstance(soundSheet: SoundSheet): SoundSheetFragment {
			val fragment = SoundSheetFragment()
			val args = Bundle()
			args.putString(KEY_FRAGMENT_TAG, soundSheet.fragmentTag)
			fragment.arguments = args
			return fragment
		}
	}

	private val soundSheet: SoundSheet get() =
		this.soundSheetManager.soundSheets.findByFragmentTag(this.fragmentTag)
				?: throw IllegalStateException("no match for fragmentTag found")

	private val KEY_STATE_RECYCLER_VIEW get() = "${this.fragmentTag}_recycler_view_state"

	private val eventBus = EventBus.getDefault()

	private val preferences = SoundboardApplication.preferenceRepository
	private val soundSheetManager = SoundboardApplication.soundSheetManager
	private val soundManager = SoundboardApplication.soundManager
	private val playlistManager = SoundboardApplication.playlistManager

	private val adapterSounds = SoundAdapter()
	private val deletionHandler by lazy {
		PendingDeletionHandler(
				soundSheet = this.soundSheet,
				adapter = this.adapterSounds,
				manager = this.soundManager,
				onItemDeletionRequested = { _, _ ->
					this.presenter.onUserDeletesSound()
				}
		)
	}

	private lateinit var presenter: SoundSheetFragmentContract.Presenter
	private lateinit var model: SoundSheetFragmentContract.Model

	private lateinit var recyclerView: RecyclerView
	private lateinit var snackbar: Snackbar

	override var displayedSounds: List<MediaPlayerController>
		get() = this.adapterSounds.values
		set(value) {
			this.adapterSounds.values = value
			this.adapterSounds.notifyDataSetChanged()
		}

	override var currentPlaylist: List<MediaPlayerController>
		get() = this.adapterSounds.currentPlaylist
		set(value) {
			this.adapterSounds.currentPlaylist = value
			this.adapterSounds.notifyDataSetChanged()
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		this.retainInstance = true
		this.setHasOptionsMenu(true)

		this.fragmentTag = this.arguments.getString(KEY_FRAGMENT_TAG)
				?: throw NullPointerException(fragmentTag + ": cannot create fragment, given fragmentTag is null")

		this.model = SoundSheetFragmentModel(
				this.soundSheet,
				this.soundManager,
				this.playlistManager
		)

		this.presenter = SoundSheetFragmentPresenter(
				this,
				this.model
		)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		if (container == null) return null

		val view = inflater.inflate(R.layout.fragment_soundsheet, container, false)

		this.recyclerView = view.rv_fragment_sound_sheet_sounds.apply {
			this.adapter = adapterSounds
			this.layoutManager = LinearLayoutManager(this.context.applicationContext)
			this.itemAnimator = DefaultItemAnimator()
			this.addItemDecoration(DividerItemDecoration(
					this.context.applicationContext, R.color.background, R.color.divider))
		}
		val itemTouchHelper = ItemTouchHelper(
				ItemTouchCallback(
						context = this.recyclerView.context,
						oneSwipeToDelete = this.preferences.isOneSwipeToDeleteEnabled,
						deletionHandler = this.deletionHandler,
						adapter = this.adapterSounds,
						soundSheet = this.soundSheet,
						soundManager = this.soundManager
				)
		)
		itemTouchHelper.attachToRecyclerView(this.recyclerView)

		val recyclerViewDetaches = RxView.detaches(this.recyclerView)
		this.adapterSounds.startsReorder
				.takeUntil(recyclerViewDetaches)
				.subscribe { viewHolder -> itemTouchHelper.startDrag(viewHolder) }

		this.adapterSounds.startsSwipe
				.takeUntil(recyclerViewDetaches)
				.subscribe { viewHolder -> itemTouchHelper.startSwipe(viewHolder) }

		this.adapterSounds.clicksSettings
				.takeUntil(recyclerViewDetaches)
				.subscribe { viewHolder ->
					viewHolder.player?.let { player ->
						SoundSettingsDialog.showInstance(
								this.fragmentManager, player.mediaPlayerData)
					}
				}

		this.adapterSounds.clicksName
				.takeUntil(recyclerViewDetaches)
				.subscribe { viewHolder ->
					viewHolder.player?.let { player ->
						GenericRenameDialogs.showRenameSoundDialog(
								this.fragmentManager, playerData = player.mediaPlayerData)
					}
				}

		this.adapterSounds.clicksPlay
				.takeUntil(recyclerViewDetaches)
				.subscribe { viewHolder ->
					viewHolder.name.clearFocus()
					viewHolder.player?.let { player ->
						when {
							player.isFadingOut -> viewHolder.playButton.state = PlayButton.State.PLAY
							player.isPlayingSound -> viewHolder.playButton.state = PlayButton.State.FADE
							else -> viewHolder.playButton.state = PlayButton.State.PAUSE
						}
						this.presenter.onUserClicksPlay(player)
					}
				}

		this.adapterSounds.clicksStop
				.takeUntil(recyclerViewDetaches)
				.subscribe { viewHolder ->
					viewHolder.player?.let { player ->
						this.presenter.onUserClicksStop(player)
					}
				}

		this.adapterSounds.clicksTogglePlaylist
				.takeUntil(recyclerViewDetaches)
				.subscribe { viewHolder ->
					val addToPlaylist = viewHolder
							.inPlaylistButton.state == TogglePlaylistButton.State.NOT_IN_PLAYLIST
					viewHolder.inPlaylistButton.state = if (addToPlaylist)
						TogglePlaylistButton.State.IN_PLAYLIST
					else
						TogglePlaylistButton.State.NOT_IN_PLAYLIST

					viewHolder.player?.let { player ->
						this.presenter.onUserTogglePlaylist(player)
					}
				}

		this.adapterSounds.clicksLoopEnabled
				.takeUntil(recyclerViewDetaches)
				.subscribe { viewHolder ->
					val enable = viewHolder
							.isLoopEnabledButton.state == ToggleLoopButton.State.LOOP_DISABLE

					viewHolder.isLoopEnabledButton.state = if (enable)
						ToggleLoopButton.State.LOOP_ENABLE
					else
						ToggleLoopButton.State.LOOP_DISABLE

					viewHolder.player?.let { player ->
						this.presenter.onUserTogglePlayerLooping(player)
					}
				}

		this.adapterSounds.seeksToPosition
				.takeUntil(recyclerViewDetaches)
				.subscribe { (viewHolder, data) ->
					viewHolder.player?.let { player ->
						this.presenter.onUserSeeksToPlaybackPosition(player, data)
					}
				}

		val fab = view.fb_layout_fab
		if (fab != null) {
			RxView.clicks(fab)
					.takeUntil(RxView.detaches(this.recyclerView))
					.subscribe {
						this.presenter.onUserClicksFab()
					}
		}

		return view
	}

	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		this.presenter.viewCreated()
	}

	override fun onResume() {
		super.onResume()
		this.eventBus.registerIfRequired(this)
	}

	override fun onPause() {
		super.onPause()
		this.eventBus.unregister(this)
	}

	override fun onRestoreState(savedInstanceState: Bundle) {
		super.onRestoreState(savedInstanceState)
		this.rv_fragment_sound_sheet_sounds?.layoutManager?.let { layoutManager ->
			savedInstanceState.putParcelable(
					KEY_STATE_RECYCLER_VIEW,
					layoutManager.onSaveInstanceState()
			)
		}
	}

	override fun onSaveState(outState: Bundle) {
		super.onSaveState(outState)
		this.rv_fragment_sound_sheet_sounds?.layoutManager?.onRestoreInstanceState(
				outState.getParcelable(KEY_STATE_RECYCLER_VIEW)
		)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == IntentRequest.GET_AUDIO_FILE) {
				val soundUri = data!!.data
				val soundLabel = FileUtils.stripFileTypeFromName(
						FileUtils.getFileNameFromUri(this.activity, soundUri))
				this.presenter.onUserAddsNewPlayer(
						soundUri, soundLabel, this.soundSheet)
			}
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		super.onOptionsItemSelected(item)
		return when (item.itemId) {
			R.id.action_clear_sounds_in_sheet -> {
				GenericConfirmDialogs.showConfirmDeleteSoundsDialog(this.fragmentManager, this.soundSheet)
				true
			}
			R.id.action_delete_sheet -> {
				GenericConfirmDialogs.showConfirmDeleteSoundSheetDialog(this.fragmentManager, this.soundSheet)
				true
			}
			else -> false
		}
	}

	override fun showAddButton() {
		this.fb_layout_fab?.visibility = View.VISIBLE
	}

	override fun updateSound(player: MediaPlayerController) {
		this.adapterSounds.notifyItemChanged(player)
	}

	override fun openDialogForNewSound() {
		if (this.preferences.useBuildInBrowserForFiles) {
			AddNewSoundFromDirectoryDialog.showInstance(this.fragmentManager, this.fragmentTag)
		} else {
			val intent = Intent(Intent.ACTION_GET_CONTENT)
			intent.type = FileUtils.MIME_AUDIO
			this.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE)
		}
	}

	override fun showSnackbarForPlayerError(player: MediaPlayerController) {
		val resources = this.context?.resources ?: return
		val message = "${resources.getString(R.string.sound_control_error_during_playback)}: " +
				player.mediaPlayerData.label

		this.snackbar.dismiss()
		this.snackbar = Snackbar.make(
				this.cl_fragment_sound_sheet, message, Snackbar.LENGTH_INDEFINITE)
		this.snackbar.show()
	}

	override fun showSnackbarForRestoreSound() {
		val resources = this.context?.resources ?: return
		val timeTillDeletion = this.deletionHandler.DELETION_TIMEOUT

		val count = deletionHandler.countPendingDeletions
		val message = if (count == 1)
			resources.getString(R.string.sound_control_deletion_pending_single)
		else
			resources.getString(R.string.sound_control_deletion_pending).replace("{%s0}",
					count.toString())

		this.snackbar.dismiss()
		this.snackbar = Snackbar.make(
				this.cl_fragment_sound_sheet, message, timeTillDeletion).apply {
			this.setAction(
					R.string.sound_control_deletion_pending_undo,
					{ deletionHandler.restoreDeletedItems() }
			)
		}
		this.snackbar.show()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerStateChangedEvent) {
		this.presenter.onMediaPlayerStateChanges(event.player, event.isAlive)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerFailedEvent) {
		this.presenter.onMediaPlayerFailed(event.player)
	}

	override fun onEvent(event: MediaPlayerCompletedEvent) {}
}