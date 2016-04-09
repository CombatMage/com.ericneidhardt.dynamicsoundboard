package org.neidhardt.dynamicsoundboard.introduction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment

/**
 * File created by eric.neidhardt on 27.10.2015.
 */

class IntroductionFragment : BaseFragment()
{
	companion object
	{
		val TAG = IntroductionFragment::class.qualifiedName
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		if (container == null)
			return null

		val fragmentView = inflater.inflate(R.layout.fragment_introduction, container, false)

		return fragmentView
	}
}