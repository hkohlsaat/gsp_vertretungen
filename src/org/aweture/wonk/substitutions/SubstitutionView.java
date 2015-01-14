package org.aweture.wonk.substitutions;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Subjects;
import org.aweture.wonk.models.Subjects.Subject;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.models.Teachers;
import org.aweture.wonk.models.Teachers.Teacher;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.TextView;

public class SubstitutionView extends FrameLayout implements Expandable, OnClickListener, OnPreDrawListener{
	
	private static final String LOG_TAG = SubstitutionView.class.getSimpleName();
	private static final int XML_RESOURCE = R.layout.view_substitution;
	private static final int COLLAPSED_BACKGROUND = 0x00dddddd;
	private static final int EXPANDED_BACKGROUND = 0xffdddddd;
	private static final int ANIMATION_DURATION = 150;
	
	/** Default width of the gap in dp */
	private static final int STD_GAP_WIDTH = 8;
	
	private Rect screenSize = new Rect();
	
	private int gapWidth;
	private int marginLeft;
	
	private TextView periodTextView;
	private TextView subjectTextView;
	private TextView instdOfTextView;
	private TextView instdTeacherTextView;
	private TextView kindTextView;
	private TextView substTeacherTextView;
	private TextView infoTextView;
	private TextView textTextView;
	
	private Teachers teachers;
	private Subjects subjects;
	private Substitution substitution;
	
	private ExpansionCoordinator expansionCoordinator;
	
	private BackgroundColorUpdater backgroundColorUpdater;
	private ValueAnimator expandingBackgroundColorAnimation;
	private ValueAnimator collapsingBackgroundColorAnimation;
	private ExpansionAnimation expansionAnimation;

	public SubstitutionView(Context context) {
		super(context);
		
		teachers = new Teachers(context);
		subjects = new Subjects(context);
		
		LayoutInflater.from(context).inflate(XML_RESOURCE, this, true);
		
		
		periodTextView = (TextView) findViewById(R.id.periodTextView);
		subjectTextView = (TextView) findViewById(R.id.subjectTextView);
		instdOfTextView = (TextView) findViewById(R.id.instdOfTextView);
		instdTeacherTextView = (TextView) findViewById(R.id.instdTeacherTextView);
		kindTextView = (TextView) findViewById(R.id.kindTextView);
		substTeacherTextView = (TextView) findViewById(R.id.substTeacherTextView);
		textTextView = (TextView) findViewById(R.id.textTextView);
		infoTextView = (TextView) findViewById(R.id.infoTextView);
		
		// Set an Animation object each to be associated with one TextView only.
		periodTextView.setTag(new RightTimeRightPlaceAnimation(periodTextView));
		subjectTextView.setTag(new RightTimeRightPlaceAnimation(subjectTextView));
		instdOfTextView.setTag(new RightTimeRightPlaceAnimation(instdOfTextView));
		instdTeacherTextView.setTag(new RightTimeRightPlaceAnimation(instdTeacherTextView));
		kindTextView.setTag(new RightTimeRightPlaceAnimation(kindTextView));
		substTeacherTextView.setTag(new RightTimeRightPlaceAnimation(substTeacherTextView));
		textTextView.setTag(new RightTimeRightPlaceAnimation(textTextView));
		infoTextView.setTag(new RightTimeRightPlaceAnimation(infoTextView));
		
		DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
		gapWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, STD_GAP_WIDTH, dm);
		marginLeft = context.getResources().getDimensionPixelSize(R.dimen.class_substitution_item_margin);
		
		setOnClickListener(this);
		
		backgroundColorUpdater = new BackgroundColorUpdater();
		
		expandingBackgroundColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), COLLAPSED_BACKGROUND, EXPANDED_BACKGROUND);
		expandingBackgroundColorAnimation.addUpdateListener(backgroundColorUpdater);
		expandingBackgroundColorAnimation.setDuration(ANIMATION_DURATION);
		
		collapsingBackgroundColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), EXPANDED_BACKGROUND, COLLAPSED_BACKGROUND);
		collapsingBackgroundColorAnimation.addUpdateListener(backgroundColorUpdater);
		collapsingBackgroundColorAnimation.setDuration(ANIMATION_DURATION);
		expansionAnimation = new ExpansionAnimation();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		getWindowVisibleDisplayFrame(screenSize);
		int width = screenSize.width();
		int height = 0;
		int mode = 0;
		
		measureChild(periodTextView, widthMeasureSpec, heightMeasureSpec);
		measureChild(kindTextView, widthMeasureSpec, heightMeasureSpec);
		mode = periodTextView.getMeasuredState() | kindTextView.getMeasuredState();
		if (expansionCoordinator.isExpanded(this)) {
			measureChild(subjectTextView, widthMeasureSpec, heightMeasureSpec);
			measureChild(instdOfTextView, widthMeasureSpec, heightMeasureSpec);
			measureChild(instdTeacherTextView, widthMeasureSpec, heightMeasureSpec);
			measureChild(substTeacherTextView, widthMeasureSpec, heightMeasureSpec);
			if (infoTextView.getVisibility() != GONE) {
				measureChild(infoTextView, widthMeasureSpec, heightMeasureSpec);
				measureChild(textTextView, widthMeasureSpec, heightMeasureSpec);
				mode = mode | infoTextView.getMeasuredState() | textTextView.getMeasuredState();
			}
			
			int bottom1 = periodTextView.getMeasuredHeight();
			int bottom2 = subjectTextView.getMeasuredHeight();
			height += Math.max(bottom1, bottom2);
			
			bottom1 = instdOfTextView.getMeasuredHeight();
			bottom2 = instdTeacherTextView.getMeasuredHeight();
			height += Math.max(bottom1, bottom2);
			
			bottom1 = kindTextView.getMeasuredHeight();
			bottom2 = substTeacherTextView.getMeasuredHeight();
			height += Math.max(bottom1, bottom2);
			
			if (infoTextView.getVisibility() != GONE) {
				bottom1 = infoTextView.getMeasuredHeight();
				bottom2 = textTextView.getMeasuredHeight();
				height += Math.max(bottom1, bottom2);
			}
			
			mode = mode | subjectTextView.getMeasuredState() | instdOfTextView.getMeasuredState()
					| instdTeacherTextView.getMeasuredState() | substTeacherTextView.getMeasuredState();
		} else {
			int bottom1 = periodTextView.getMeasuredHeight();
			int bottom2 = kindTextView.getMeasuredHeight();
			height = Math.max(bottom1, bottom2);
		}
		
		setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, mode),
				resolveSizeAndState(height, heightMeasureSpec, mode << MEASURED_HEIGHT_STATE_SHIFT));
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int leftWidth = periodTextView.getMeasuredWidth();
		if (expansionCoordinator.isExpanded(this)) {
			leftWidth = Math.max(leftWidth, instdOfTextView.getMeasuredWidth());
			leftWidth = Math.max(leftWidth, kindTextView.getMeasuredWidth());
			if (infoTextView.getVisibility() != GONE) {
				leftWidth = Math.max(leftWidth, infoTextView.getMeasuredWidth());
			}
			leftWidth += marginLeft;
			
			int bottom1 = layoutLeftTextView(periodTextView, leftWidth, 0);
			int bottom2 = layoutRightTextView(subjectTextView, leftWidth, 0);
			int nextTop = Math.max(bottom1, bottom2);
			
			bottom1 = layoutLeftTextView(instdOfTextView, leftWidth, nextTop);
			bottom2 = layoutRightTextView(instdTeacherTextView, leftWidth, nextTop);
			nextTop = Math.max(bottom1, bottom2);
			
			bottom1 = layoutLeftTextView(kindTextView, leftWidth, nextTop);
			bottom2 = layoutRightTextView(substTeacherTextView, leftWidth, nextTop);
			nextTop = Math.max(bottom1, bottom2);
			
			if (infoTextView.getVisibility() != GONE) {
				layoutLeftTextView(infoTextView, leftWidth, nextTop);
				layoutRightTextView(textTextView, leftWidth, nextTop);
			}
		} else {
			leftWidth += marginLeft;
			layoutLeftTextView(periodTextView, leftWidth, 0);
			layoutRightTextView(kindTextView, leftWidth, 0);
		}
	}
	
	private int layoutLeftTextView(TextView view, int leftWidth, int top) {
		int left = leftWidth - view.getMeasuredWidth();
		int right = leftWidth;
		int bottom = top + view.getMeasuredHeight();
		view.layout(left, top, right, bottom);
		return bottom;
	}
	
	private int layoutRightTextView(TextView view, int leftWidth, int top) {
		int left = leftWidth + gapWidth;
		int right = left + view.getMeasuredWidth();
		int bottom = top + view.getMeasuredHeight();
		view.layout(left, top, right, bottom);
		return bottom;
	}
	

	public void setExpansionCoordinator(ExpansionCoordinator expansionCoordinator) {
		this.expansionCoordinator = expansionCoordinator;
	}
	
	public void setSubstitution(Substitution substitution) {
		this.substitution = substitution;
		applySubstitutionData();
		applyVisibilityProperties();
	}
	
	private void applySubstitutionData() {
		String period = substitution.getPeriodNumber() + ". Stunde";
		String instdSubjectShort = substitution.getInstdSubject();
		String instdTeacherShort = substitution.getInstdTeacher();
		String kind = substitution.getKind();
		String substTeacherShort = substitution.getSubstTeacher();
		String text = substitution.getText();
		
		Teacher teacher = teachers.getTeacher(instdTeacherShort);
		String instdTeacher = teacher.getAccusative();
		teacher = teachers.getTeacher(substTeacherShort);
		String substTeacher = teacher.getName();
		
		Subject subject = subjects.getSubject(instdSubjectShort);
		String instdSubject = subject.getName();
		
		periodTextView.setText(period);
		subjectTextView.setText(instdSubject);
		instdTeacherTextView.setText(instdTeacher);
		kindTextView.setText(kind);
		substTeacherTextView.setText(substTeacher);
		textTextView.setText(text);
	}
	
	private void applyVisibilityProperties() {
		if (expansionCoordinator.isExpanded(this)) {
			subjectTextView.setVisibility(VISIBLE);
			instdOfTextView.setVisibility(VISIBLE);
			instdTeacherTextView.setVisibility(VISIBLE);
			substTeacherTextView.setVisibility(VISIBLE);
			textTextView.setVisibility(VISIBLE);
			if (textTextView.getText().toString().isEmpty()) {
				infoTextView.setVisibility(GONE);
				textTextView.setVisibility(GONE);
			} else {
				infoTextView.setVisibility(VISIBLE);
				textTextView.setVisibility(VISIBLE);
			}
		} else {
			subjectTextView.setVisibility(GONE);
			instdOfTextView.setVisibility(GONE);
			instdTeacherTextView.setVisibility(GONE);
			substTeacherTextView.setVisibility(GONE);
			textTextView.setVisibility(GONE);
			infoTextView.setVisibility(GONE);
		}
	}

	@Override
	public void onClick(View v) {
		if (expansionCoordinator != null) {
			expansionCoordinator.clicked(this);
		} else {
			String className = ExpansionCoordinator.class.getSimpleName();
			Log.w(LOG_TAG, "Click happened but no "
					+ className + "set! Ignoring click!");
		}
	}

	@Override
	public void expand() {
		setupExpansionChange();
	}

	@Override
	public void collapse() {
		setupExpansionChange();
	}
	
	private void setupExpansionChange() {
		expansionAnimation.setOldHeight(getHeight());
		
		getAnimation(periodTextView).queryOldPosition();
		getAnimation(subjectTextView).queryOldPosition();
		getAnimation(instdOfTextView).queryOldPosition();
		getAnimation(instdTeacherTextView).queryOldPosition();
		getAnimation(kindTextView).queryOldPosition();
		getAnimation(substTeacherTextView).queryOldPosition();
		getAnimation(textTextView).queryOldPosition();
		getAnimation(infoTextView).queryOldPosition();
		
		applyVisibilityProperties();
		if (isAttachedToWindow()) {
			getViewTreeObserver().addOnPreDrawListener(this);
		}
	}

	@Override
	public boolean onPreDraw() {
		getViewTreeObserver().removeOnPreDrawListener(this);
		
		if (expansionCoordinator.isExpanded(this)) {
			expandingBackgroundColorAnimation.start();
		} else {
			collapsingBackgroundColorAnimation.start();
		}
		
		startAnimation(expansionAnimation);
		
		periodTextView.startAnimation(getAnimation(periodTextView));
		subjectTextView.startAnimation(getAnimation(subjectTextView));
		instdOfTextView.startAnimation(getAnimation(instdOfTextView));
		instdTeacherTextView.startAnimation(getAnimation(instdTeacherTextView));
		kindTextView.startAnimation(getAnimation(kindTextView));
		substTeacherTextView.startAnimation(getAnimation(substTeacherTextView));
		textTextView.startAnimation(getAnimation(textTextView));
		infoTextView.startAnimation(getAnimation(infoTextView));
		return false;
	}
	
	private RightTimeRightPlaceAnimation getAnimation(View view) {
		return (RightTimeRightPlaceAnimation) view.getTag();
	}
	
	private class BackgroundColorUpdater implements AnimatorUpdateListener {
	    @Override
	    public void onAnimationUpdate(ValueAnimator animator) {
	        setBackgroundColor((Integer) animator.getAnimatedValue());
	    }
	}
	
	private class ExpansionAnimation extends Animation {
		
		private int oldHeight, newHeight;
		
		public ExpansionAnimation() {
			setDuration(ANIMATION_DURATION);
		}

		public void setOldHeight(int oldHeight) {
			this.oldHeight = oldHeight;
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			getLayoutParams().height = computeHeight(interpolatedTime);
			requestLayout();
		}
		
		private int computeHeight(float interpolatedTime) {
			if (interpolatedTime != 1) {
				return (int) (oldHeight * (1 - interpolatedTime) + (newHeight * interpolatedTime));
			} else {
				return LayoutParams.WRAP_CONTENT;
			}
		}
		
		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			newHeight = height;
		}
	}
	
	/**
	 * This animation cares for {@link View}s <ul><li>coming to the screen</li>
	 * <li>leaving the screen</li><li>staying on screen and changing position.</li></ul>
	 * 
	 * <p>"Coming" means View.GONE before and View.VISIBLE after layout. The view
	 * will get faded in.
	 * "Leaving" means View.VISIBLE before and View.GONE after layout. The view
	 * will get faded out.
	 * "Changing position" means View.VISIBLE before and VIEW.VISIBLE after layout
	 * having different x and y coordinates. The view will get moved from the old to
	 * the new position.</p>
	 * 
	 * <p>This class is intended to be used with the {@link OnPreDrawListener}. Before
	 * layout happens the method {@link #queryOldPosition()}
	 * should get called to gather information about the pre-layout-values. After layout
	 * but before the draw (in {@link OnPreDrawListener#onPreDraw()}) this animation
	 * should get started to show a smooth transition between old and new state. It will
	 * gather the then current information itself at the start of the animation.</p>
	 * 
	 * @author Hannes Kohlsaat
	 */
	private class RightTimeRightPlaceAnimation extends Animation {
		
		/** {@link View} from which new states information will be gatered in {@link #initialize(int, int, int, int)}. */
		private View view;
		
		/** Old position of the view, if it has one.
		 * @see #hasOldPosition */
		private Point oldPosition;
		
		/** New position of the view, if it has one.
		 * @see #hasNewPosition */
		private Point newPosition;
		
		/** Whether {@link #view} has an old position. The information is
		 * particularly interesting in comparison to {@link #hasNewPosition} */
		private boolean hasOldPosition;
		
		/** Whether {@link #view} has a new position. The information is
		 * particularly interesting in comparison to {@link #hasOldPosition} */
		private boolean hasNewPosition;
		
		/**
		 * Constructor
		 * @param view (of type {@link View} should get supplied to get the current state at animation begin.
		 */
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
			hasOldPosition = view.getVisibility() != GONE;
			
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
			hasNewPosition = view.getVisibility() != GONE;
			
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