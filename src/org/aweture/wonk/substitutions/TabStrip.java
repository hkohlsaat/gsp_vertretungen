package org.aweture.wonk.substitutions;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.appcompat.R;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

public class TabStrip extends LinearLayout implements OnPageChangeListener, Tab.OnTabClickListener {
	
	private int position;
	private float positionOffset;

	private Rect indicator;
	private Paint indicatorPaint;
	
	private ViewPager viewPager;
	private List<Tab> tabs;

	public TabStrip(Context context) {
		super(context);
		init(context, null);
	}
	public TabStrip(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	public TabStrip(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TabStrip(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs) {
		tabs = new ArrayList<Tab>();
		setOrientation(HORIZONTAL);
		
		TypedArray a = context.obtainStyledAttributes(attrs, new int[]{R.attr.colorPrimaryDark, R.attr.colorPrimary});
		final int indicatorColor = a.getColor(0, 0x88303030);
		final int backgroundColor = a.getColor(1, 0xff0000ff);
		setBackgroundColor(backgroundColor);
		a.recycle();
		
		final int unit = TypedValue.COMPLEX_UNIT_DIP;
		final int value = 4;
		final DisplayMetrics metrics = getResources().getDisplayMetrics();
		final int indicatorHeight = (int) TypedValue.applyDimension(unit, value, metrics);
		indicator = new Rect(0, 0, 0, indicatorHeight);
		
		indicatorPaint = new Paint();
		indicatorPaint.setColor(indicatorColor);
	}
	
	
	public void setViewPager(ViewPager pager) {
		viewPager = pager;
		viewPager.setOnPageChangeListener(this);
		PagerAdapter adapter = viewPager.getAdapter();
		setupFromAdapterInformation(adapter);
	}

	private void setupFromAdapterInformation(PagerAdapter pagerAdapter) {
		removeAllViews();
		tabs.clear();
		
		final int pages = pagerAdapter.getCount();
		for (int i = 0; i < pages; i++) {
			Tab tab = addTab(pagerAdapter.getPageTitle(i));
			tabs.add(tab);
		}
		
		if (tabs.size() > 0) {
			Tab tab = tabs.get(0);
			tab.setVolume(1);
		}
	}
	
	private Tab addTab(CharSequence tabText) {
		Tab tab = new Tab(getContext(), tabText);
		tab.setOnTabClickListener(this);
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1);
		addView(tab.getView(), params);
		
		return tab;
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}
	
	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		this.position = position;
		this.positionOffset = positionOffset;
		if (getChildCount() > 0) {
			updateTabs();
			invalidate();
		}
	}
	
	@Override
	public void onPageSelected(int arg0) {
	}
	
	private void updateTabs() {
		// update left tab
		Tab tab = tabs.get(position);
		tab.setVolume(1 - positionOffset);
		
		// update right tab
		int rightPosition = position + 1;
		if (tabs.size() > rightPosition) {
			tab = tabs.get(rightPosition);
			tab.setVolume(positionOffset);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (getChildCount() > position) {
			
			setupInticator();
			if (positionOffset != 0) {
				shiftIndicator();
			}
			
			canvas.drawRect(indicator, indicatorPaint);
		}
	}
	
	private void setupInticator() {
		View view = tabs.get(position).getView();
		indicator.top = getHeight() - indicator.height();
		indicator.right = view.getRight();
		indicator.bottom = getHeight();
		indicator.left = view.getLeft();
	}
	
	private void shiftIndicator() {
		indicator.left += indicator.width() * positionOffset;
		
		View view = tabs.get(position + 1).getView();
		indicator.right += view.getWidth() * positionOffset;
	}
	

	@Override
	public void onTabClicked(Tab tab) {
		int index = tabs.indexOf(tab);
		viewPager.setCurrentItem(index);
	}
}
