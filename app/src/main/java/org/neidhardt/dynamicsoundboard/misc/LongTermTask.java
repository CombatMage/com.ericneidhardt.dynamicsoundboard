package org.neidhardt.dynamicsoundboard.misc;

import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.events.LongTermTaskStartedEvent;
import org.neidhardt.dynamicsoundboard.events.LongTermTaskStoppedEvent;
import org.neidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;

import java.util.List;

/**
 * Created by eric.neidhardt on 24.03.2015.
 */

public abstract class LongTermTask<T> extends SafeAsyncTask<List<T>>
{
	private LongTermTaskStartedEvent event;

	@Override
	protected void onPreExecute() throws Exception
	{
		super.onPreExecute();
		this.event = new LongTermTaskStartedEvent();
		EventBus.getDefault().postSticky(this.event);
	}

	@Override
	protected void onSuccess(List<T> ts) throws Exception
	{
		super.onSuccess(ts);
		EventBus.getDefault().removeStickyEvent(this.event);
		EventBus.getDefault().postSticky(new LongTermTaskStoppedEvent());
	}

	@Override
	protected void onException(Exception e) throws RuntimeException
	{
		super.onException(e);
		Logger.e(getTag(), e.getMessage());
		EventBus.getDefault().removeStickyEvent(this.event);
		EventBus.getDefault().postSticky(new LongTermTaskStoppedEvent());
		throw new RuntimeException(e);
	}

	protected abstract String getTag();
}
