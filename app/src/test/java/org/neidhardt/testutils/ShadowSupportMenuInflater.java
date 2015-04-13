package org.neidhardt.testutils;

import android.support.v7.internal.view.SupportMenuInflater;
import android.view.Menu;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowMenuInflater;

/**
 * Created by eric.neidhardt on 02.04.2015.
 */

@Implements(SupportMenuInflater.class)
public class ShadowSupportMenuInflater extends ShadowMenuInflater {
	@Implementation
	public void inflate(int menuRes, Menu menu) {
		super.inflate(menuRes, menu);
	}
}
