package org.neidhardt.dynamicsoundboard.dao;

import org.greenrobot.greendao.annotation.*;
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutManager;

@Entity
public class SoundLayout {

	@Id
	public Long id;

	@NotNull
	@Index(unique = true)
	public String databaseId;

	@NotNull
	public String label;

	@NotNull
	public boolean isSelected;

	@Transient
	public boolean isSelectedForDeletion;

    @Generated(hash = 1712563922)
    public SoundLayout() {
    }

    public SoundLayout(Long id) {
        this.id = id;
    }

    @Generated(hash = 953469533)
	public SoundLayout(Long id, @NotNull String databaseId, @NotNull String label,
									boolean isSelected) {
					this.id = id;
					this.databaseId = databaseId;
					this.label = label;
					this.isSelected = isSelected;
	}

	public boolean isDefaultLayout()
	{
		return this.databaseId.equals(SoundLayoutManager.DB_DEFAULT);
	}

	public Long getId() {
					return this.id;
	}

	public void setId(Long id) {
					this.id = id;
	}

	public String getDatabaseId() {
					return this.databaseId;
	}

	public void setDatabaseId(String databaseId) {
					this.databaseId = databaseId;
	}

	public String getLabel() {
					return this.label;
	}

	public void setLabel(String label) {
					this.label = label;
	}

	public boolean getIsSelected() {
					return this.isSelected;
	}

	public void setIsSelected(boolean isSelected) {
					this.isSelected = isSelected;
	}
}
