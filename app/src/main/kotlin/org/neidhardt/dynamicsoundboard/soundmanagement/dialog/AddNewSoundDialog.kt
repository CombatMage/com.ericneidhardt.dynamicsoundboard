package org.neidhardt.dynamicsoundboard.soundmanagement.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.FragmentManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.fileexplorer.FileResultHandler
import org.neidhardt.dynamicsoundboard.fileexplorer.GetNewSoundFromDirectoryDialog
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.Playlist
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.views.BaseDialog
import org.neidhardt.dynamicsoundboard.views.DialogBaseLayout
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration
import java.io.File
import java.util.*

/**
 * File created by eric.neidhardt on 30.06.2015.
 */
class AddNewSoundDialog : BaseDialog, FileResultHandler
{
	val TAG: String = javaClass.name
	private val KEY_SOUNDS_URI = "org.neidhardt.dynamicsoundboard.soundmanagement.dialog.AddNewSoundDialog.soundsToAdd"
	private val KEY_SOUNDS_LABEL = "org.neidhardt.dynamicsoundboard.soundmanagement.dialog.AddNewSoundDialog.soundsToAddLabels"


	private var presenter: AddNewSoundDialogPresenter? = null

	internal var callingFragmentTag = ""

	constructor() : super() {}

	constructor(manager: FragmentManager, callingFragmentTag: String) : super()
	{
		val args = Bundle()
		args.putString(KEY_CALLING_FRAGMENT_TAG, callingFragmentTag)
		this.arguments = args

		this.show(manager, TAG)
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		val args = this.arguments
		if (args != null)
			this.callingFragmentTag = args.getString(KEY_CALLING_FRAGMENT_TAG)
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(R.layout.dialog_add_new_sound, null)

		val dialog = AppCompatDialog(this.activity, R.style.DialogTheme)
		dialog.setContentView(view)
		dialog.setTitle(R.string.dialog_add_new_sound_title)

		val heightOfControls = this.getMeasureHeight(view) + this.resources.getDimensionPixelSize(R.dimen.margin_default)

		view.layoutParams.height = heightOfControls
		this.mainView = view as DialogBaseLayout

		this.presenter = AddNewSoundDialogPresenter(
				dialog = this,
				soundsDataStorage = this.soundsDataStorage,
				add = view.findViewById(R.id.b_ok) as Button,
				cancel = view.findViewById(R.id.b_cancel),
				addAnotherSound = view.findViewById(R.id.b_add_another_sound),
				addedSoundsLayout = view.findViewById(R.id.rv_dialog) as RecyclerView)

		return dialog
	}

	private fun getMeasureHeight(view: View) : Int
	{
		view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

		return view.measuredHeight
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		val presenter = this.presenter as AddNewSoundDialogPresenter

		if (savedInstanceState != null)
		{
			val labels = savedInstanceState.getStringArrayList(KEY_SOUNDS_LABEL)
			val uris = savedInstanceState.getStringArrayList(KEY_SOUNDS_URI)
			if (labels != null && uris != null)
			{
				val count = labels.size
				for (i in 0..count - 1)
				{
					val uri = Uri.parse(uris[i])
					val label = labels[i]

					this.updateHeightToContent()
					presenter.addNewSound(NewSoundData(uri, label))
				}
			}
		}
	}

	override fun onSaveInstanceState(@SuppressWarnings("NullableProblems") outState: Bundle)
	{
		super.onSaveInstanceState(outState)

		val presenter = this.presenter as AddNewSoundDialogPresenter

		val count = presenter.getValues().size
		val uris = ArrayList<String>(count)
		val labels = ArrayList<String>(count)

		for (i in 0..count - 1)
		{
			val data = presenter.getValues()[i]

			labels.add(i, data.label)
			uris.add(i, data.uri.toString())
		}
		outState.putStringArrayList(KEY_SOUNDS_URI, uris)
		outState.putStringArrayList(KEY_SOUNDS_LABEL, labels)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == IntentRequest.GET_AUDIO_FILE)
			{
				val soundUri = data!!.data
				val label = FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(this.activity, soundUri))
				presenter?.addNewSound(NewSoundData(soundUri, label))
				this.updateHeightToContent()
				return
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}

	// if no intent mechanism, but another file dialog is used, this method may be called to deliver results
	override fun onFileResultsAvailable(files: List<File>)
	{
		for (file in files)
		{
			val soundUri = Uri.parse(file.absolutePath)

			val label = FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(this.activity, soundUri))
			presenter?.addNewSound(NewSoundData(soundUri, label))
			this.updateHeightToContent()
		}
		this.updateHeightToContent()
	}

	private fun updateHeightToContent()
	{
		this.mainView!!.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
	}
}

private class AddNewSoundDialogPresenter
(
		private val dialog: AddNewSoundDialog,
		private val add: Button,
		private val cancel: View,
		private val addAnotherSound: View,
		private val addedSoundsLayout: RecyclerView,

		private val soundsDataStorage: SoundsDataStorage
)
{
	private val soundsToAdd = ArrayList<NewSoundData>()

	val adapter = NewSoundAdapter(this)

	init
	{
		this.add.isEnabled = false

		this.cancel.setOnClickListener({ view -> this.dialog.dismiss() })
		this.add.setOnClickListener({ view -> this.addSoundsToSoundSheet()})
		this.addAnotherSound.setOnClickListener({ this.addAnotherSound() })

		this.addedSoundsLayout.apply {
			this.addItemDecoration(DividerItemDecoration(this.context))
			this.layoutManager = LinearLayoutManager(this.context)
			this.itemAnimator = DefaultItemAnimator()
		}
		this.addedSoundsLayout.adapter = adapter

		this.dialog.mainView!!.enableRecyclerViewDividers(false)
	}

	private fun addSoundsToSoundSheet()
	{
		if (soundsToAdd.size > 0)
		{
			this.returnResultsToCallingFragment()
			this.dialog.dismiss()
		}
	}

	private fun returnResultsToCallingFragment()
	{
		val count = this.soundsToAdd.size
		val playersData = ArrayList<MediaPlayerData>(count)
		val renamedPlayers = ArrayList<MediaPlayerData>()
		for (i in 0..count - 1)
		{
			val item = this.getValues()[i]

			val soundUri = item.uri
			val soundLabel = item.label
			val playerData = MediaPlayerData.getNewMediaPlayerData(this.dialog.callingFragmentTag, soundUri, soundLabel)
			playersData.add(playerData)

			if (item.wasSoundRenamed)
				renamedPlayers.add(playerData)
		}

		if (this.dialog.callingFragmentTag == Playlist.TAG)
		{
			for (playerData in playersData)
				this.soundsDataStorage.createPlaylistSoundAndAddToManager(playerData)
		} else {
			for (playerData in playersData)
				this.soundsDataStorage.createSoundAndAddToManager(playerData)
		}

		this.showRenameDialog(renamedPlayers) // show the rename dialog for all altered players
	}

	private fun showRenameDialog(renamedMediaPlayers: List<MediaPlayerData>)
	{
		for (data in renamedMediaPlayers)
			RenameSoundFileDialog(this.dialog.fragmentManager, data)
	}

	private fun addAnotherSound()
	{
		if (SoundboardPreferences.useSystemBrowserForFiles())
		{
			val intent = Intent(Intent.ACTION_GET_CONTENT)
			intent.type = FileUtils.MIME_AUDIO
			this.dialog.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE)
		}
		else
		{
			GetNewSoundFromDirectoryDialog(this.dialog.fragmentManager, this.dialog.TAG);
		}
	}

	internal fun addNewSound(data: NewSoundData)
	{
		this.add.isEnabled = true
		this.soundsToAdd.add(data)
		this.dialog.mainView!!.enableRecyclerViewDividers(true)
		this.adapter.notifyItemInserted(this.soundsToAdd.size - 1)
	}

	internal fun getValues() : List<NewSoundData>
	{
		return this.soundsToAdd
	}
}

private class NewSoundAdapter(private val presenter: AddNewSoundDialogPresenter) : RecyclerView.Adapter<NewSoundViewHolder>()
{
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewSoundViewHolder
	{
		val view = LayoutInflater.from(parent.context).inflate(R.layout.view_add_sound_list_item, parent, false)
		return NewSoundViewHolder(view)
	}

	override fun getItemCount(): Int
	{
		return this.presenter.getValues().size
	}

	override fun onBindViewHolder(holder: NewSoundViewHolder, position: Int)
	{
		holder.bindData(this.presenter.getValues()[position])
	}
}

private class NewSoundData(uri: Uri, label: String)
{
	var uri: Uri = uri
	var label: String = label
	var wasSoundRenamed = false
}

private class NewSoundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), TextWatcher
{
	private val soundPath = itemView.findViewById(R.id.tv_path) as TextView
	private val soundName = itemView.findViewById(R.id.et_name_file) as EditText

	private var data: NewSoundData? = null

	init
	{
		this.soundName.addTextChangedListener(this)
	}

	internal fun bindData(data: NewSoundData)
	{
		this.data = data
		this.soundPath.text = data.uri.toString()
		this.soundName.setText(data.label.toString())
	}

	override fun afterTextChanged(newLabel: Editable)
	{
		if (!newLabel.toString().equals(this.data?.label))
		{
			this.data?.label = newLabel.toString()
			this.data?.wasSoundRenamed = true
		}
	}

	override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

	override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
}