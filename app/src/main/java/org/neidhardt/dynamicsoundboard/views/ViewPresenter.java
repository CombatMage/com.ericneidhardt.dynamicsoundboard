package org.neidhardt.dynamicsoundboard.views;

/**
 * Created by eric.neidhardt on 22.05.2015.
 */
public interface ViewPresenter<T>
{

	/**
	 * Retrieve reference of controlled ui component from presenter.
	 * @return controlled ui object
	 */
	T getView();

	/**
	 * Pass reference to controlled ui component to presenter.
	 * @param view ui object to be controlled by presenter
	 */
	void setView(T view);

	/**
	 * Notifies the presenter, that corresponding view has been attached to main window. EventBus should be registered in
	 * this step.
	 */
	void onAttachedToWindow();

	/**
	 * Notifies the presenter, that corresponding view has been detached from main window. EventBus should be unregistered in this step.
	 */
	void onDetachedFromWindow();

}
