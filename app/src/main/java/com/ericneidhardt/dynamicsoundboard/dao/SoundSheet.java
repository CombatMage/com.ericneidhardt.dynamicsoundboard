package com.ericneidhardt.dynamicsoundboard.dao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table SOUND_SHEET.
 */
public class SoundSheet {

    private Long id;
    private String fragmentTag;
    private String label;
    private Boolean isSelected;

    public SoundSheet() {
    }

    public SoundSheet(Long id) {
        this.id = id;
    }

    public SoundSheet(Long id, String fragmentTag, String label, Boolean isSelected) {
        this.id = id;
        this.fragmentTag = fragmentTag;
        this.label = label;
        this.isSelected = isSelected;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFragmentTag() {
        return fragmentTag;
    }

    public void setFragmentTag(String fragmentTag) {
        this.fragmentTag = fragmentTag;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

}