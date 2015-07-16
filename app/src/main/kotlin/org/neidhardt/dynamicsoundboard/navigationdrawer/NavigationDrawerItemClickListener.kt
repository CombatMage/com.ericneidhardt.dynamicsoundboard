package org.neidhardt.dynamicsoundboard.navigationdrawer

import org.neidhardt.dynamicsoundboard.dao.SoundSheet

/**
 * File created by eric.neidhardt on 16.07.2015.
 */
public interface NavigationDrawerItemClickListener<T>
{
	fun onItemClick(data: T)
}