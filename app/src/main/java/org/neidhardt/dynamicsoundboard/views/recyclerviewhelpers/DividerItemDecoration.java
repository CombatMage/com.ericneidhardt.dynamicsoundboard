package org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;

public class DividerItemDecoration extends RecyclerView.ItemDecoration
{
	private int colorBackground;
	private int colorDivider;
	private int heightDivider;

	public DividerItemDecoration()
	{
		this.colorBackground = DynamicSoundboardApplication.getSoundboardContext().getResources().getColor(R.color.background);
		this.colorDivider = DynamicSoundboardApplication.getSoundboardContext().getResources().getColor(R.color.divider);
		this.heightDivider = DynamicSoundboardApplication.getSoundboardContext().getResources().getDimensionPixelSize(R.dimen.stroke);
	}

	@Override
	public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state)
	{
		final int childCount = parent.getChildCount();
		if (childCount == 0)
			return;

		for (int i = 0; i < childCount; i++)
		{
			if (i < childCount - 1) // do not draw divider after last item
			{
				View child = parent.getChildAt(i);
				final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
				final int left = parent.getPaddingLeft();
				final int right = parent.getWidth() - parent.getPaddingRight();
				final int top = child.getBottom() + params.bottomMargin;
				final int bottom = top + this.heightDivider;

				this.drawDividerBackground(canvas, left, top, right, bottom);
				this.drawDivider(canvas, left, top, right, bottom);
			}
		}
	}

	private void drawDividerBackground(Canvas canvas, int left, int top, int right, int bottom)
	{
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);paint.setColor(this.colorBackground);
		canvas.drawRect(left, top, right, bottom, paint);
	}

	private void drawDivider(Canvas canvas, int left, int top, int right, int bottom)
	{
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);paint.setColor(this.colorDivider);
		canvas.drawRect(left, top, right, bottom, paint);
	}

	@Override
	public void getItemOffsets(Rect outRect, View childView, RecyclerView parent, RecyclerView.State state)
	{
		int topOffset = 0;
		int bottomOffset = this.heightDivider;
		int rightOffset =  0;
		int leftOffset =  0;
		outRect.set(leftOffset, topOffset, rightOffset, bottomOffset);
	}
}

