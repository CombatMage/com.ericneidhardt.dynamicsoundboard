package org.neidhardt.dynamicsoundboard.navigationdrawer.events

/**
* Created by ericn on 04.03.2016.
*/
data class ItemSelectedForDeletion(val selectedItemCount: Int, val itemCount: Int)

interface ItemSelectedForDeletionListener
{
	fun onEvent(event: ItemSelectedForDeletion)
}