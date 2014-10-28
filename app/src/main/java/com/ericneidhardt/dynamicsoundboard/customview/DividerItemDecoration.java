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
	public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

	private Drawable divider;
	private int offsetFirstItem;
	public DividerItemDecoration(Context context, Drawable customDivider)
	{
		if (customDivider == null)
		{
			final TypedArray a = context.obtainStyledAttributes(ATTRS);
			this.divider = a.getDrawable(0);
			a.recycle();
		}
		else
			this.divider = customDivider;

		this.offsetFirstItem = context.getResources().getDimensionPixelSize(R.dimen.margin_very_small);
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent)
	{
		drawForVerticalList(c, parent);
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
			if (i == 0)
				this.drawBackgroundFirstItem(c, parent);
		}
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

	@Override
	public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent)
	{
		int bottomOffset = this.divider.getIntrinsicHeight();
		int topOffset = itemPosition == 0 ? this.offsetFirstItem : 0;
		int rightOffset =  0;
		int leftOffset =  0;

		outRect.set(leftOffset, topOffset, rightOffset, bottomOffset);
	}
}

