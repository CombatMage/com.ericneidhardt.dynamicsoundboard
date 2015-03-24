package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

import java.util.List;

/**
 * Created by eric.neidhardt on 24.03.2015.
 */

public abstract class LoadTask<T> extends SafeAsyncTask<List<T>>
{
	private static final String TAG = LoadTask.class.getName();

	@Override
	protected void onSuccess(List<T> ts) throws Exception {
		super.onSuccess(ts);
		Logger.d(TAG, "onSuccess: with " + ts.size() + " sounds loaded");
	}

	@Override
	protected void onException(Exception e) throws RuntimeException {
		super.onException(e);
		Logger.e(TAG, e.getMessage());
		throw new RuntimeException(e);
	}
}
