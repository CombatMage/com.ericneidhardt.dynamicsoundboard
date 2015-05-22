package org.neidhardt.dynamicsoundboard.misc.longtermtask;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.longtermtask.events.LongTermTaskStateChangedEvent;
import roboguice.util.SafeAsyncTask;

/**
 * Created by eric.neidhardt on 24.03.2015.
 */

public abstract class LongTermTask<T> extends SafeAsyncTask<T>
{
	private static int taskCounter;

	@Override
	protected void onPreExecute() throws Exception
	{
		super.onPreExecute();
		taskCounter++;
		EventBus.getDefault().postSticky(new LongTermTaskStateChangedEvent(true, taskCounter));
	}

	@Override
	protected void onSuccess(T t) throws Exception
	{
		super.onSuccess(t);
		taskCounter--;
		EventBus.getDefault().postSticky(new LongTermTaskStateChangedEvent(false, taskCounter));
	}

	@Override
	protected void onException(Exception e) throws RuntimeException
	{
		super.onException(e);
		Logger.e(getTag(), e.getMessage());
		taskCounter--;
		EventBus.getDefault().postSticky(new LongTermTaskStateChangedEvent(false, taskCounter));
		throw new RuntimeException(e);
	}

	protected abstract String getTag();

}
