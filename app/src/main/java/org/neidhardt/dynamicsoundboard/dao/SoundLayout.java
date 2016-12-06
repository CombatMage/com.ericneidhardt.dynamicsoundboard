package org.neidhardt.dynamicsoundboard.dao;

import org.greenrobot.greendao.annotation.*;
import org.neidhardt.dynamicsoundboard.daohelper.DaoHelperKt;
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.NewSoundLayoutManager;

/**
 * Entity mapped to table "SOUND_LAYOUT".
 */
@Entity
public class SoundLayout {

    @Id
    private Long id;

    @NotNull
    private String label;

    @NotNull
    @Unique
    private String databaseId;
    private boolean isSelected;

    @Transient
    private boolean isSelectedForDeletion;

    @Generated(hash = 1712563922)
    public SoundLayout() {
    }

    public SoundLayout(Long id) {
        this.id = id;
    }

    @Generated(hash = 311490126)
    public SoundLayout(Long id, @NotNull String label, @NotNull String databaseId,
            boolean isSelected) {
        this.id = id;
        this.label = label;
        this.databaseId = databaseId;
        this.isSelected = isSelected;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setLabel(@NotNull String label) {
        this.label = label;
    }

    @NotNull
    public String getDatabaseId() {
        return databaseId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setDatabaseId(@NotNull String databaseId) {
        this.databaseId = databaseId;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isDefaultLayout()
    {
        return this.databaseId.equals(NewSoundLayoutManager.DB_DEFAULT);
    }

    public boolean isSelectedForDeletion() {
        return isSelectedForDeletion;
    }

    public void setIsSelectedForDeletion(boolean isSelectedForDeletion) {
        this.isSelectedForDeletion = isSelectedForDeletion;
    }
}
