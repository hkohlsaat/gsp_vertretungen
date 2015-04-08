package org.aweture.wonk.substitutions;

import android.graphics.Point;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import static android.view.View.GONE;

public class AppearanceAnimation extends Animation {
	
	private View view;
	private Point oldPosition;
	private Point newPosition;
	private boolean hasOldPosition;
	private boolean hasNewPosition;
	
	
	public AppearanceAnimation(View view) {
		this.view = view;
		
		oldPosition = new Point();
		newPosition = new Point();
	}
	
	/**
	 * This method should get called before layout happens,
	 * in order to gather information about the pre-layout-values.
	 */
	public void queryOldPosition() {
		if (hasOldPosition = isVisible(view)) {
			
			int oldX = (int) view.getX();
			int oldY = (int) view.getY();
			oldPosition.set(oldX, oldY);
		}
	}
	
	
	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		
		if (hasNewPosition = isVisible(view)) {
			
			int newX = (int) view.getX();
			int newY = (int) view.getY();
			newPosition.set(newX, newY);
		}
	}
	
	boolean isVisible(View view) {
		return view.getVisibility() != GONE;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		if (animateFromPlaceToPlace()) {
			float timeCountDown = 1 - interpolatedTime;
			
			float substractNewX = - newPosition.x * (timeCountDown);
			float substractNewY = - newPosition.y * (timeCountDown);
			float addOldX = oldPosition.x * (timeCountDown);
			float addOldY = oldPosition.y * (timeCountDown);
			
			float shiftX = substractNewX + addOldX;
			float shiftY = substractNewY + addOldY;
			
			t.getMatrix().setTranslate(shiftX, shiftY);
		} else if (animateIn()) {
			t.setAlpha(interpolatedTime);
		} else if (animateOut()) {
			t.setAlpha(1 - interpolatedTime);
		}
	}
	
	private boolean animateFromPlaceToPlace() {
		return hasOldPosition && hasNewPosition;
	}
	private boolean animateIn() {
		return !hasOldPosition && hasNewPosition;
	}
	private boolean animateOut() {
		return hasOldPosition && !hasNewPosition;
	}
}
