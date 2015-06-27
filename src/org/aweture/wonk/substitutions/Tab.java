package org.aweture.wonk.substitutions;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class Tab implements OnClickListener {
	
	private static final int MAX_ALPHA = 0xff;
	private static final int MIN_ALPHA = 0x99;
	private static final int RGB_COLOR = 0xffffff;
	
	private TextView view;
	
	private OnTabClickListener clickListener;

	public Tab(Context context) {
		view = new TextView(context);
		view.setGravity(Gravity.CENTER);
		view.setTypeface(null, Typeface.BOLD);
		view.setAllCaps(true);
		view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		view.setTextColor((MIN_ALPHA << 24) + RGB_COLOR);
		view.setOnClickListener(this);
	}
	
	public View getView() {
		return view;
	}
	
	public void setText(CharSequence text) {
		view.setText(text);
	}
	
	public void setVolume(float volume) {
		int alpha = (int) ((MIN_ALPHA * (1 - volume)) + (MAX_ALPHA * volume));
		int newColor = (alpha << 24) + RGB_COLOR;
		view.setTextColor(newColor);
	}
	
	public void setOnTabClickListener(OnTabClickListener listener) {
		clickListener = listener;
	}

	@Override
	public void onClick(View v) {
		if (clickListener != null) {
			clickListener.onTabClicked(this);
		}
	}
	
	public interface OnTabClickListener {
		public void onTabClicked(Tab tab);
	}

}
