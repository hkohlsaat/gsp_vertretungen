package org.aweture.wonk.substitutions;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabStrip extends LinearLayout implements OnPageChangeListener {
	
	private static final short MIN_ALPHA = 0x99;
	
	private int position;
	private float positionOffset;
	
	private Rect indicator;
	private Paint indicatorPaint;
	
	private ViewPager viewPager;

	public TabStrip(Context context) {
		this(context, null, 0, 0);
	}
	public TabStrip(Context context, AttributeSet attrs) {
		this(context, attrs, 0, 0);
	}
	public TabStrip(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}
	public TabStrip(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		setOrientation(HORIZONTAL);
		
		int[] textSizeAttr = new int[]{android.R.attr.colorPrimary};
		TypedArray a = context.obtainStyledAttributes(attrs, textSizeAttr);
		int backgroundColor = a.getColor(0, 0xffffffff);
		a.recycle();
		setBackgroundColor(backgroundColor);
		
		int indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
		indicator = new Rect(0, 0, 0, indicatorHeight);
		
		indicatorPaint = new Paint();
		indicatorPaint.setColor(0xfFffffff);
	}
	
	public void setViewPager(ViewPager viewPager) {
		this.viewPager = viewPager;
		viewPager.setOnPageChangeListener(this);
	}
	
	public void setTabsFromPagerAdapter(PagerAdapter pagerAdapter) {
		removeAllViews();
		
		int pages = pagerAdapter.getCount();
		for (int i = 0; i < pages; i++) {
			addTab(pagerAdapter.getPageTitle(i), i);
		}
	}
	
	public int addTab(CharSequence tabText, int tabNr) {
		TextView textView = new TextView(getContext());
		textView.setText(tabText);
		textView.setGravity(Gravity.CENTER);
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		textView.setAllCaps(true);
		textView.setTextColor((MIN_ALPHA << 24) + 0xffffff);
		textView.setOnClickListener(new OnTabClickListener(tabNr));
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1);
		addView(textView, params);
		
		return tabNr;
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}
	
	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		this.position = position;
		this.positionOffset = positionOffset;
		if (getChildCount() > 0) {
			updateTabTexts();
			invalidate();
		}
	}
	
	@Override
	public void onPageSelected(int arg0) {
	}
	
	private void updateTabTexts() {
		TextView leftText = (TextView) getChildAt(position);
		int argb = leftText.getTextColors().getDefaultColor();
		int rgb = argb & 0xffffff;
		int alpha = (int) (MIN_ALPHA + ((0xff - MIN_ALPHA) * (1 - positionOffset)));
		leftText.setTextColor((alpha << 24) + rgb);
		
		int rightPosition = position + 1;
		if (rightPosition < getChildCount()) {
			TextView rightText = (TextView) getChildAt(rightPosition);
			argb = rightText.getTextColors().getDefaultColor();
			rgb = argb & 0xffffff;
			alpha = (int) (MIN_ALPHA + ((0xff - MIN_ALPHA) * positionOffset));
			rightText.setTextColor((alpha << 24) + rgb);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (getChildCount() > 0) {
			
			TextView leftTextView = (TextView) getChildAt(position);
			
			indicator.left = leftTextView.getLeft();
			indicator.right = leftTextView.getRight();
			int heigt = indicator.height();
			indicator.bottom = getHeight() - 1;
			indicator.top = indicator.bottom - heigt;
			
			if (positionOffset != 0) {
				float partOfRight = (float) Math.pow(positionOffset, 2);
				int rightLength = (int) (getChildAt(position + 1).getWidth() * partOfRight);
				indicator.right += rightLength;
				
				float partOfLeft = (float) Math.pow(1 - positionOffset, 2);
				int leftLenght = (int) (leftTextView.getWidth() * partOfLeft);
				indicator.left += leftTextView.getWidth() - leftLenght;
			}
			canvas.drawRect(indicator, indicatorPaint);
		}
	}
	
	private class OnTabClickListener implements OnClickListener {
		private final int tabNr;
		
		public OnTabClickListener(int tabNr) {
			this.tabNr = tabNr;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(tabNr);
			}
		}
	}
}
