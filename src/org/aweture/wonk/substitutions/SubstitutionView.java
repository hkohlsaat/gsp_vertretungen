package org.aweture.wonk.substitutions;

import org.aweture.wonk.models.Substitution;

import android.content.Context;
import android.view.View;
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
public class SubstitutionView extends AbstractSubstitutionView implements OnPreDrawListener {
	
	private static final int BACKGROUND_COLOR_RGB = 0xcfd8dc;
	private static final int ANIMATION_DURATION = 200;

	public SubstitutionView(Context context) {
		super(context);
		
		ViewHolder vh = getViewHolder();
		vh.setAnimationDuration(ANIMATION_DURATION);
	}

	@Override
	public void changeExpansionState(boolean animate) {
		ViewHolder vh = getViewHolder();
		vh.queryOldPositions();

		super.changeExpansionState(animate);
	}

	@Override
	public boolean onPreDraw() {
		ViewTreeObserver viewTreeObserver = getViewTreeObserver();
		viewTreeObserver.removeOnPreDrawListener(this);

		ViewHolder vh = getViewHolder();
		vh.startAnimations();
		
		clearAnimation();
		startAnimation(new BackgroundColorAnimation());
		return true;
	}
	
	private class BackgroundColorAnimation extends Animation {
		
		public BackgroundColorAnimation() {
			setDuration(ANIMATION_DURATION);
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			interpolatedTime = isExpanded() ? interpolatedTime : (1 - interpolatedTime);
			int backgroundAlpha = (int) (0xff * interpolatedTime) << 24;
			int backgroundColor = backgroundAlpha ^ BACKGROUND_COLOR_RGB;
			SubstitutionView.this.setBackgroundColor(backgroundColor);
		}
	}
}