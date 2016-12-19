package org.neidhardt.dynamicsoundboard.persistance.model;

import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */

public class NewMediaPlayerData {
	public String playerId;
	public String fragmentTag;
	public String label;
	public String uri;
	public boolean isLoop;
	public boolean isInPlaylist;
	public Long timePosition;
	public Integer sortOrder;
	public boolean isSelectedForDeletion;
}
