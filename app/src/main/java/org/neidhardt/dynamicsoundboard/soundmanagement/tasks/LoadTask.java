package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.progressbar.LongTermTaskEvent;
import org.neidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

import java.util.List;

/**
 * Created by eric.neidhardt on 24.03.2015.
 */

public abstract class LoadTask<T> extends SafeAsyncTask<List<T>>
{
	private static final String TAG = LoadTask.class.getName();

	private LongTermTaskEvent event;

	@Override
	protected void onPreExecute() throws Exception
	{
		super.onPreExecute();
		this.event = new LongTermTaskEvent(true);
		EventBus.getDefault().postSticky(this.event);
	}

	@Override
	protected void onSuccess(List<T> ts) throws Exception
	{
		super.onSuccess(ts);
		Logger.d(TAG, "onSuccess: with " + ts.size() + " sounds loaded");
		EventBus.getDefault().removeStickyEvent(this.event);
		EventBus.getDefault().post(new LongTermTaskEvent(false));
	}

	@Override
	protected void onException(Exception e) throws RuntimeException
	{
		super.onException(e);
		Logger.e(TAG, e.getMessage());
		EventBus.getDefault().removeStickyEvent(this.event);
		EventBus.getDefault().post(new LongTermTaskEvent(false));
		throw new RuntimeException(e);
	}

}
