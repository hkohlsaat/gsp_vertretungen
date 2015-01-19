package org.aweture.wonk.substitutions;

import org.aweture.wonk.models.Substitution;

import android.content.Context;
import android.graphics.Point;
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
	
	
	private ExpansionAnimation expansionAnimation;
	
	private OnPreDrawListener onPickOffNewHeightPreDrawListener;
	private OnPreDrawListener onStartAnimationsPreDrawListener;

	public SubstitutionView(Context context, ExpansionCoordinator expansionCoordinator) {
		super(context);
		
		integrateAnimationsIntoChildViews();
		
		expansionAnimation = new ExpansionAnimation();
		
		onPickOffNewHeightPreDrawListener = new OnPickOffNewHeightPreDrawListener();
		onStartAnimationsPreDrawListener = new OnStartAnimationsPreDrawListener();
	}
	
	private void integrateAnimationsIntoChildViews() {
		// Set an Animation object each to be associated with one TextView only.
		ViewHolder vh = getViewHolder();
		vh.period.setTag(new RightTimeRightPlaceAnimation(vh.period));
		vh.course.setTag(new RightTimeRightPlaceAnimation(vh.course));
		vh.subject.setTag(new RightTimeRightPlaceAnimation(vh.subject));
		vh.instdOf.setTag(new RightTimeRightPlaceAnimation(vh.instdOf));
		vh.instdTeacher.setTag(new RightTimeRightPlaceAnimation(vh.instdTeacher));
		vh.kind.setTag(new RightTimeRightPlaceAnimation(vh.kind));
		vh.substTeacher.setTag(new RightTimeRightPlaceAnimation(vh.substTeacher));
		vh.text.setTag(new RightTimeRightPlaceAnimation(vh.text));
		vh.info.setTag(new RightTimeRightPlaceAnimation(vh.info));
	}

	@Override
	public void changeExpansionState(boolean animate) {
		expansionAnimation.oldHeight = getHeight();
		
		ViewHolder vh = getViewHolder();
		getAnimation(vh.period).queryOldPosition();
		getAnimation(vh.course).queryOldPosition();
		getAnimation(vh.subject).queryOldPosition();
		getAnimation(vh.instdOf).queryOldPosition();
		getAnimation(vh.instdTeacher).queryOldPosition();
		getAnimation(vh.kind).queryOldPosition();
		getAnimation(vh.substTeacher).queryOldPosition();
		getAnimation(vh.text).queryOldPosition();
		getAnimation(vh.info).queryOldPosition();

		super.changeExpansionState(animate);
		
		if (animate) {
			getViewTreeObserver().addOnPreDrawListener(onPickOffNewHeightPreDrawListener);
		} else {
			setBackgroundColor(BACKGROUND_COLOR_RGB);
		}
	}
	
	private RightTimeRightPlaceAnimation getAnimation(View view) {
		return (RightTimeRightPlaceAnimation) view.getTag();
	}
	
	private class OnPickOffNewHeightPreDrawListener implements OnPreDrawListener {

		@Override
		public boolean onPreDraw() {
			ViewTreeObserver viewTreeObserver = getViewTreeObserver();
			viewTreeObserver.removeOnPreDrawListener(this);
			viewTreeObserver.addOnPreDrawListener(onStartAnimationsPreDrawListener);
			
			expansionAnimation.newHeight = getHeight();
			
			ViewGroup.LayoutParams layoutParams = getLayoutParams();
			layoutParams.height = expansionAnimation.oldHeight;
			
			requestLayout();
			
			return false;
		}
	}
	
	private class OnStartAnimationsPreDrawListener implements OnPreDrawListener {
		
		@Override
		public boolean onPreDraw() {
			getViewTreeObserver().removeOnPreDrawListener(this);
			
			startAnimation(expansionAnimation);
			
			ViewHolder vh = getViewHolder();
			vh.period.startAnimation(getAnimation(vh.period));
			vh.course.startAnimation(getAnimation(vh.course));
			vh.subject.startAnimation(getAnimation(vh.subject));
			vh.instdOf.startAnimation(getAnimation(vh.instdOf));
			vh.instdTeacher.startAnimation(getAnimation(vh.instdTeacher));
			vh.kind.startAnimation(getAnimation(vh.kind));
			vh.substTeacher.startAnimation(getAnimation(vh.substTeacher));
			vh.text.startAnimation(getAnimation(vh.text));
			vh.info.startAnimation(getAnimation(vh.info));
			
			return true;
		}
	}
	
	private class ExpansionAnimation extends Animation {
		
		private int oldHeight, newHeight;
		
		public ExpansionAnimation() {
			setDuration(ANIMATION_DURATION);
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
	
	/**
	 * This animation cares for {@link View}s
	 * <ul>
	 * 		<li>coming to the screen</li>
	 * 		<li>leaving the screen</li>
	 * 		<li>staying on screen and changing position.</li>
	 * </ul>
	 * 
	 * 
	 * @author Hannes Kohlsaat
	 */
	private class RightTimeRightPlaceAnimation extends Animation {
		
		private View view;
		private Point oldPosition;
		private Point newPosition;
		private boolean hasOldPosition;
		private boolean hasNewPosition;
		
		
		public RightTimeRightPlaceAnimation(View view) {
			this.view = view;
			oldPosition = new Point();
			newPosition = new Point();
			setDuration(ANIMATION_DURATION);
		}
		
		/**
		 * This method should get called before layout happens to gather information about the pre-layout-values.
		 */
		public void queryOldPosition() {
			// Was is the view currently there?
			hasOldPosition = isVisible(view);
			
			// If it is there get the position.
			if (hasOldPosition) {
				int oldX = (int) view.getX();
				int oldY = (int) view.getY();
				oldPosition.set(oldX, oldY);
			}
		}
		
		
		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			// Is the View still there?
			hasNewPosition = isVisible(view);
			
			// If it is there get the new position.
			if (hasNewPosition) {
				int newX = (int) view.getX();
				int newY = (int) view.getY();
				newPosition.set(newX, newY);
			}
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			if (hasOldPosition && hasNewPosition) {
				// Move the view from the old to the new place.
				float currentX = - newPosition.x * (1 - interpolatedTime) + (oldPosition.x * (1 - interpolatedTime));
				float currentY = - newPosition.y * (1 - interpolatedTime) + (oldPosition.y * (1 - interpolatedTime));
				t.getMatrix().setTranslate(currentX, currentY);
			} else if (!hasOldPosition && hasNewPosition) {
				// Fade in.
				t.setAlpha(interpolatedTime);
			} else if (hasOldPosition && !hasNewPosition) {
				// Fade out.
				t.setAlpha(1 - interpolatedTime);
			}
		}
	}
}