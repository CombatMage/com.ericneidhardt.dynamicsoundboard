package org.neidhardt.dynamicsoundboard.model;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */

public class SoundLayout {

	public String databaseId;

	public String label;

	public boolean isSelected;

	public boolean isSelectedForDeletion;

	@Nullable
	public List<SoundSheet> soundSheets;

	@Nullable
	public List<MediaPlayerData> playList;
}
