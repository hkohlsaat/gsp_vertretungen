package org.aweture.wonk.substitutions;

import org.aweture.wonk.models.Substitution;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * {@link View} presenting {@link Substitution}s animating 
 * expand and collapse.
 * 
 * @author Hannes Kohlsaat
 *
 */
public class SubstitutionView extends AbstractSubstitutionView {
	
	private static final int BACKGROUND_COLOR_RGB = 0xdddddd;
	private static final int ANIMATION_DURATION = 150;
	
	private final ExpansionAnimation expansionAnimation;
	
	private final OnPreDrawListener onPickOffNewHeightPreDrawListener;
	private final OnPreDrawListener onStartAnimationsPreDrawListener;

	public SubstitutionView(Context context) {
		super(context);
		
		ViewHolder vh = getViewHolder();
		vh.setAnimationDuration(ANIMATION_DURATION);
		
		expansionAnimation = new ExpansionAnimation();
		
		onPickOffNewHeightPreDrawListener = new OnPickOffNewHeightPreDrawListener();
		onStartAnimationsPreDrawListener = new OnStartAnimationsPreDrawListener();
	}

	@Override
	public void changeExpansionState(boolean animate) {
		clearAnimation();
		
		if (animate) {
			expansionAnimation.queryOldHeight();
			
			ViewHolder vh = getViewHolder();
			vh.queryOldPositions();
			
			getViewTreeObserver().addOnPreDrawListener(onPickOffNewHeightPreDrawListener);
		} else {
			setBackgroundColor(BACKGROUND_COLOR_RGB);
		}

		super.changeExpansionState(animate);
		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		layoutParams.height = LayoutParams.WRAP_CONTENT;
	}
	
	private class OnPickOffNewHeightPreDrawListener implements OnPreDrawListener {

		@Override
		public boolean onPreDraw() {
			ViewTreeObserver viewTreeObserver = getViewTreeObserver();
			viewTreeObserver.removeOnPreDrawListener(this);
			viewTreeObserver.addOnPreDrawListener(onStartAnimationsPreDrawListener);

			expansionAnimation.queryNewHeight();
			
			// Because since changeExpansionState() all views have the new layout,
			// set the old height again and lay it out, so the animation can display
			// a smooth transition from the old to the new height.
			ViewGroup.LayoutParams layoutParams = getLayoutParams();
			layoutParams.height = expansionAnimation.oldHeight;
			requestLayout();
			
			// Return false because the current layout should not be drawn.
			// The next draw will pass. See OnStartAnimationsPreDrawListener.
			return false;
		}
	}
	
	private class OnStartAnimationsPreDrawListener implements OnPreDrawListener {
		
		@Override
		public boolean onPreDraw() {
			getViewTreeObserver().removeOnPreDrawListener(this);
			
			startAnimation(expansionAnimation);
			
			ViewHolder vh = getViewHolder();
			vh.startAnimations();
			
			return true;
		}
	}
	
	
	private class ExpansionAnimation extends Animation {
		
		private int oldHeight, newHeight;
		
		public ExpansionAnimation() {
			setDuration(ANIMATION_DURATION);
		}

		private void queryOldHeight() {
			oldHeight = getHeight();
		}
		private void queryNewHeight() {
			newHeight = getHeight();
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			applyNewHeight(interpolatedTime);
			setNewBackgroundColor(interpolatedTime);
			requestLayout();
		}
		
		private void applyNewHeight(float interpolatedTime) {
			ViewGroup.LayoutParams layoutParams = getLayoutParams();
			layoutParams.height = computeHeight(interpolatedTime);
		}
		
		private int computeHeight(float interpolatedTime) {
			if (interpolatedTime != 1) {
				return (int) (oldHeight * (1 - interpolatedTime) + (newHeight * interpolatedTime));
			} else {
				return LayoutParams.WRAP_CONTENT;
			}
		}
		
		private void setNewBackgroundColor(float interpolatedTime) {
			if (oldHeight > newHeight) {
				interpolatedTime = 1 - interpolatedTime;
			}
			int backgroundAlpha = (int) (0xff * interpolatedTime) << 24;
			int backgroundColor = backgroundAlpha ^ BACKGROUND_COLOR_RGB;
			SubstitutionView.this.setBackgroundColor(backgroundColor);
		}
	}
}