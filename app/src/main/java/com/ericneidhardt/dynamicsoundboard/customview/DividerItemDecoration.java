package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.ericneidhardt.dynamicsoundboard.R;

public class DividerItemDecoration extends RecyclerView.ItemDecoration
{

	private static final int[] ATTRS = new int[]{
			android.R.attr.listDivider
	};

	private Drawable divider;
	private int heightDivider = 0;
	private int offsetFirstItem = 0;
	private int offsetLastItem = 0;
	private Integer backgroundColor;

	public DividerItemDecoration(Context context)
	{
		this.init(context);

		this.backgroundColor = null;
	}

	public DividerItemDecoration(Context context, int backgroundColor)
	{
		this.init(context);

		this.backgroundColor = backgroundColor;
	}

	private void init(Context context)
	{
		final TypedArray a = context.obtainStyledAttributes(ATTRS);
		this.divider = a.getDrawable(0);
		a.recycle();

		this.heightDivider = this.divider.getIntrinsicHeight();

		this.offsetFirstItem = context.getResources().getDimensionPixelSize(R.dimen.margin_very_small);
		this.offsetLastItem = context.getResources().getDimensionPixelSize(R.dimen.margin_very_small);
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
		if (this.backgroundColor == null)
			paint.setColor(parent.getContext().getResources().getColor(R.color.background));
		else
			paint.setColor(this.backgroundColor);
		canvas.drawRect(left, top, right, bottom, paint);
	}

	@Override
	public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent)
	{
		boolean isFirstItem = itemPosition == 0;
		boolean isLastItem = itemPosition == parent.getAdapter().getItemCount() - 1;

		int bottomOffset = isLastItem ?  + this.heightDivider + this.offsetLastItem : this.heightDivider;
		int topOffset = isFirstItem ? this.offsetFirstItem : 0;
		int rightOffset =  0;
		int leftOffset =  0;

		outRect.set(leftOffset, topOffset, rightOffset, bottomOffset);
	}
}

