package org.neidhardt.dynamicsoundboard.persistance.model;

import java.util.List;

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */

public class NewSoundLayout {

	public String databaseId;

	public String label;

	public boolean isSelected;

	public boolean isSelectedForDeletion;

	public List<NewSoundSheet> soundSheets;

	public List<NewMediaPlayerData> playList;
}
