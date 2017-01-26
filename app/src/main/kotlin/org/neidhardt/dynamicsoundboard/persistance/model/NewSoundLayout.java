package org.neidhardt.dynamicsoundboard.persistance.model;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */

public class NewSoundLayout {

	public String databaseId;

	public String label;

	public boolean isSelected;

	public boolean isSelectedForDeletion;

	@Nullable
	public List<NewSoundSheet> soundSheets;

	@Nullable
	public List<NewMediaPlayerData> playList;
}
