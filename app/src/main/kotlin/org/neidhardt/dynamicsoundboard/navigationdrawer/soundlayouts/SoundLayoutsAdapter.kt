package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutAddedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRenamedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.AddNewSoundLayoutDialog
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.SoundLayoutSettingsDialog
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsManager
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.ListAdapter

/**
 * File created by eric.neidhardt on 08.03.2015.
 */
public class SoundLayoutsAdapter
(
		private var onItemClickListener: NavigationDrawerItemClickListener<SoundLayout>
		private val eventBus: EventBus
) :
		RecyclerView.Adapter<SoundLayoutsAdapter.ViewHolder>(),
		ListAdapter<SoundLayout>,
		SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
		AddNewSoundLayoutDialog.OnSoundLayoutAddedEventListener
{
	public fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
		this.onItemClickListener = onItemClickListener
	}

	public fun getValues(): List<SoundLayout> {
		return SoundLayoutsManager.getInstance().getSoundLayouts()
	}

	override fun getItemCount(): Int {
		return this.getValues().size()
	}

	override fun getItemViewType(position: Int): Int {
		return R.layout.view_sound_layout_item
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val data = this.getValues().get(position)
		holder.bindData(data)
	}

	override fun notifyItemChanged(data: SoundLayout) {
		val index = this.getValues().indexOf(data)
		if (index == -1)
			this.notifyDataSetChanged()
		else
			this.notifyItemChanged(index)
	}

	override fun onEvent(event: SoundLayoutRenamedEvent) {
		val renamedLayout = event.getRenamedSoundLayout()
		this.notifyItemChanged(this.getValues().indexOf(renamedLayout))
	}

	override fun onEvent(event: SoundLayoutAddedEvent) {
		this.notifyDataSetChanged()
	}

	public inner class ViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
		private val label: TextView
		private val selectionIndicator: ImageView

		init {
			this.label = itemView.findViewById(R.id.tv_label) as TextView
			this.selectionIndicator = itemView.findViewById(R.id.iv_selected) as ImageView

			itemView.findViewById(R.id.b_settings).setOnClickListener(this)
			itemView.setOnClickListener(this)
		}

		public fun bindData(data: SoundLayout) {
			this.label.setText(data.getLabel())
			this.label.setSelected(data.getIsSelected())
			this.selectionIndicator.setVisibility(if (data.getIsSelected()) View.VISIBLE else View.INVISIBLE)

			this.label.setActivated(data.isSelectedForDeletion())
			this.itemView.setSelected(data.isSelectedForDeletion())
		}

		override fun onClick(view: View) {
			if (onItemClickListener == null)
				return

			val id = view.getId()
			val position = this.getLayoutPosition()
			val item = getValues().get(position)
			if (id == R.id.b_settings)
				onItemClickListener!!.onItemSettingsClicked(item)
			else if (onItemClickListener != null)
				onItemClickListener!!.onItemClick(view, item, position)
		}
	}

	public interface OnItemClickListener {
		public fun onItemClick(view: View, data: SoundLayout, position: Int)

		public fun onItemSettingsClicked(data: SoundLayout)
	}

}
