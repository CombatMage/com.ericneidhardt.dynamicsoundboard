package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.ericneidhardt.dynamicsoundboard.R;

public class DividerItemDecoration extends RecyclerView.ItemDecoration
{

	private static final int[] ATTRS = new int[]{
			android.R.attr.listDivider
	};
	public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
	public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

	private Drawable divider;
	private Drawable shadow;
	private int orientation;
	private int offsetFirstItem;
	private int offsetLastItem;

	public DividerItemDecoration(Context context, int orientation, Drawable customDivider)
	{
		if (customDivider == null)
		{
			final TypedArray a = context.obtainStyledAttributes(ATTRS);
			this.divider = a.getDrawable(0);
			a.recycle();
		}
		else
			this.divider = customDivider;

		this.shadow = context.getResources().getDrawable(R.drawable.ic_shadow_horizontal);

		this.offsetFirstItem = context.getResources().getDimensionPixelSize(R.dimen.margin_very_small);
		this.offsetLastItem = context.getResources().getDimensionPixelSize(R.dimen.height_shadow);
		this.setOrientation(orientation);
	}

	public void setOrientation(int orientation)
	{
		if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST)
			throw new IllegalArgumentException("invalid orientation");
		this.orientation = orientation;
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent) {
		if (orientation == VERTICAL_LIST)
			drawForVerticalList(c, parent);
		else
			drawForHorizontalList(c, parent);
	}

	public void drawForVerticalList(Canvas c, RecyclerView parent)
	{
		final int left = parent.getPaddingLeft();
		final int right = parent.getWidth() - parent.getPaddingRight();

		final int childCount = parent.getChildCount();
		if (childCount == 0)
			return;

		for (int i = 0; i < childCount; i++)
		{
			if (i == 0)
				this.drawBackgroundFirstItem(c, parent);

			final View child = parent.getChildAt(i);
			if (i < childCount - 1) // do not draw divider after last item
			{
				final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
						.getLayoutParams();
				final int top = child.getBottom() + params.bottomMargin;
				final int bottom = top + divider.getIntrinsicHeight();
				divider.setBounds(left, top, right, bottom);
				divider.draw(c);
			}
			//else
			//	this.drawShadowAfterLastItem(c, parent);
		}
	}

	private void drawShadowAfterLastItem(Canvas canvas, RecyclerView parent)
	{
		final int left = parent.getPaddingLeft();
		final int right = parent.getWidth() - parent.getPaddingRight();
		final int top = parent.getBottom();
		final int bottom = top + this.offsetLastItem;

		this.shadow.setBounds(left, top, right, bottom);
		this.shadow.draw(canvas);
	}

	private void drawBackgroundFirstItem(Canvas canvas, RecyclerView parent)
	{
		final int left = parent.getPaddingLeft();
		final int right = parent.getWidth() - parent.getPaddingRight();
		final int top = parent.getTop();
		final int bottom = top + this.offsetFirstItem;

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(parent.getContext().getResources().getColor(R.color.background));
		canvas.drawRect(left, top, right, bottom, paint);
	}

	public void drawForHorizontalList(Canvas c, RecyclerView parent)
	{
		final int top = parent.getPaddingTop();
		final int bottom = parent.getHeight() - parent.getPaddingBottom();

		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++)
		{
			final View child = parent.getChildAt(i);
			final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
					.getLayoutParams();
			final int left = child.getRight() + params.rightMargin;
			final int right = left + divider.getIntrinsicHeight();
			divider.setBounds(left, top, right, bottom);
			divider.draw(c);
		}
	}

	@Override
	public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent)
	{
		int bottomOffset = orientation == VERTICAL_LIST ? this.divider.getIntrinsicHeight() : 0;
		int topOffset = orientation == VERTICAL_LIST && itemPosition == 0 ? this.offsetFirstItem : 0;

		int rightOffset = orientation != VERTICAL_LIST ? this.divider.getIntrinsicHeight() : 0;
		int leftOffset = orientation != VERTICAL_LIST && itemPosition == 0 ? this.offsetFirstItem : 0;

		outRect.set(leftOffset, topOffset, rightOffset, bottomOffset);
	}
}

