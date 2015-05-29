package org.neidhardt.dynamicsoundboard.dialog.fileexplorer;

import android.animation.Animator;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.views.BaseDialog;

import java.io.File;
import java.util.List;

/**
 * File created by eric.neidhardt on 12.11.2014.
 */
public abstract class FileExplorerDialog extends BaseDialog
{
	protected DirectoryAdapter adapter;

	protected abstract boolean canSelectDirectory();

	protected abstract boolean canSelectFile();

	protected class DirectoryAdapter extends RecyclerView.Adapter<DirectoryEntry>
	{
		protected File parent;
		protected File selectedFile;
		private List<File> fileList;
		private DirectoryEntry selectedEntry = null;

		public DirectoryAdapter()
		{
			this.setParent(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
			this.notifyDataSetChanged();
		}

		public void setParent(File parent)
		{
			this.parent = parent;
			this.fileList = FileUtils.getFilesInDirectory(this.parent);
			if (this.parent.getParentFile() != null)
				this.fileList.add(0, this.parent.getParentFile());
		}

		public void refreshDirectory()
		{
			this.fileList = FileUtils.getFilesInDirectory(this.parent);
			if (this.parent.getParentFile() != null)
				this.fileList.add(0, this.parent.getParentFile());
		}

		@Override
		public DirectoryEntry onCreateViewHolder(ViewGroup parent, int i)
		{
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_directory_item, parent, false);
			return new DirectoryEntry(view);
		}

		@Override
		public void onBindViewHolder(DirectoryEntry directoryEntry, int position)
		{
			File file = this.fileList.get(position);
			directoryEntry.bindData(file);
		}

		@Override
		public int getItemCount()
		{
			return this.fileList.size();
		}
	}

	protected class DirectoryEntry
			extends
				RecyclerView.ViewHolder
			implements
				View.OnClickListener,
				View.OnLongClickListener,
				Animator.AnimatorListener
	{
		private ImageView fileType;
		private ImageView selectionIndicator;
		private TextView fileName;


		public DirectoryEntry(View itemView)
		{
			super(itemView);
			this.fileName = (TextView) itemView.findViewById(R.id.tv_label);
			this.fileType = (ImageView) itemView.findViewById(R.id.iv_file_type);
			this.selectionIndicator = (ImageView) itemView.findViewById(R.id.iv_selected);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		public void bindData(File file)
		{
			if (file.equals(adapter.parent.getParentFile()))
				this.bindParentDirectory();
			else
			{
				this.fileName.setText(file.getName());
				if (file.isDirectory())
					this.bindDirectory(file);
				else
					this.bindFile(file);

				boolean isEntrySelected = file.equals(adapter.selectedFile);
				this.setSelection(isEntrySelected);
				if (isEntrySelected)
					adapter.selectedEntry = this;
			}
		}

		private void setSelection(boolean selected)
		{
			this.selectionIndicator.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
			this.fileType.setSelected(selected);
		}

		private void bindFile(File file)
		{
			if (FileUtils.isAudioFile(file))
				this.fileType.setImageResource(R.drawable.selector_ic_file_sound);
			else
				this.fileType.setImageResource(R.drawable.selector_ic_file);
		}

		private void bindDirectory(File file)
		{
			if (FileUtils.containsAudioFiles(file))
				this.fileType.setImageResource(R.drawable.selector_ic_folder_sound);
			else
				this.fileType.setImageResource(R.drawable.selector_ic_folder);
		}

		private void bindParentDirectory()
		{
			this.fileName.setText("..");
			this.fileType.setImageResource(R.drawable.selector_ic_parent_directory);
			this.selectionIndicator.setVisibility(View.GONE);
		}

		@Override
		public void onClick(View v)
		{
			File file = adapter.fileList.get(this.getLayoutPosition());
			if (!file.isDirectory())
				return;
			adapter.setParent(file);
			adapter.notifyDataSetChanged();
		}

		@Override
		public boolean onLongClick(View v)
		{
			File file = adapter.fileList.get(this.getLayoutPosition());
			if (file.equals(adapter.parent.getParentFile()))
				return false;

			if (file.isDirectory() && !canSelectDirectory())
				return false;

			if (!file.isDirectory() && !canSelectFile())
				return false;

			this.selectEntry(file);

			return true;
		}

		private void selectEntry(File file)
		{
			adapter.selectedFile = file;
			if (adapter.selectedEntry != null)
				adapter.selectedEntry.setSelection(false);

			adapter.selectedEntry = this;

			this.setSelection(true);
			this.animateSelectorSlideIn();
			this.animateFileLogoRotate();
		}

		private void animateFileLogoRotate()
		{
			this.fileType.animate()
					.rotationYBy(360)
					.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime)).setListener(this)
					.start();
		}

		@Override
		public void onAnimationStart(Animator animation) {}

		@Override
		public void onAnimationEnd(Animator animation)
		{
			this.fileType.setRotationY(0);
		}

		@Override
		public void onAnimationCancel(Animator animation)
		{
			this.fileType.setRotationY(0);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {}

		private void animateSelectorSlideIn()
		{
			int distance = this.selectionIndicator.getWidth();
			this.selectionIndicator.setTranslationX(distance); // move selector to the right to be out of the screen

			this.selectionIndicator.animate().
					translationX(0).
					setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime)).
					setInterpolator(new DecelerateInterpolator()).
					start();
		}
	}

}
