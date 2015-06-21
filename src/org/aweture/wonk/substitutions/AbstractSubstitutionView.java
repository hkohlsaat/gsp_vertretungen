package org.aweture.wonk.substitutions;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.substitutions.SubstitutionPresentation.PresentationFor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * {@link View} presenting {@link Substitution}s by expanding
 * when clicked but not animated.
 * 
 * @author Hannes Kohlsaat
 *
 */
public class AbstractSubstitutionView extends ViewGroup implements Expandable, OnClickListener {
	
	private static final int MIDDLE_GAP_WIDTH_DIP = 8;
	private static final int PADDING_LEFT_DIP = 16;
	
	private static final ExpansionCoordinator expansionCoordinator = new ExpansionCoordinator();
	
	private final ViewHolder viewHolder;
	
	private final int paddingLeft;
	private final int middleGapWidth;
	private final int screenWidth;
	
	private Substitution substitution;
	private boolean studentRepresentation = true;
	private SubstitutionPresentation presentation;
	
	public AbstractSubstitutionView(Context context) {
		super(context);
		
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		layoutInflater.inflate(R.layout.view_substitution, this, true);
		
		viewHolder = new ViewHolder();
		
		Resources resources = getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		paddingLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING_LEFT_DIP, dm);
		middleGapWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIDDLE_GAP_WIDTH_DIP, dm);
		
		Rect rect = new Rect();
		getWindowVisibleDisplayFrame(rect);
		screenWidth = rect.width();
		
		setOnClickListener(this);
	}
	
	ViewHolder getViewHolder() {
		return viewHolder;
	}
	
	boolean isStudentRepresentation() {
		return studentRepresentation;
	}
	
	@Override
	public void changeExpansionState(boolean animate) {
		applyVisibilityProperties();
	}
	
	@Override
	public void onClick(View v) {
		expansionCoordinator.clicked(this);
	}
	
	public void setSubstitution(Substitution substitution, PresentationFor presentationMode) {
		this.substitution = substitution;
		presentation = new SubstitutionPresentation(this, substitution, presentationMode);
		applyVisibilityProperties();
	}
	
	private void applyVisibilityProperties() {
		if (isExpanded()) {
			presentation.setExpandedVisibilities();
		} else {
			presentation.setCollapsedVisibilities();
		}
	}
	
	private boolean isExpanded() {
		return expansionCoordinator.isExpanded(this);
	}
	
	@Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (expansionCoordinator.isExpanded(this)) {
			expansionCoordinator.removeAsExpanded(this);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = screenWidth;
		int height = 0;
		int state = 0;
		
		if (isExpanded()) {
			measureChildrenExpanded(widthMeasureSpec, heightMeasureSpec);
			height = computeOwnHeightExpanded();
			state = computeChildrenMeasuredStateExpanded();
		} else {
			measureChildrenCollapsed(widthMeasureSpec, heightMeasureSpec);
			height = computeOwnHeightCollapsed();
			state = computeChildrenMeasuredStateCollapsed();
		}
		
		int measuredWidth = resolveSizeAndState(width, widthMeasureSpec, state);
		int measuredHeight = resolveSizeAndState(height, heightMeasureSpec, state << MEASURED_HEIGHT_STATE_SHIFT);
		
		setMeasuredDimension(measuredWidth, measuredHeight);
	}
	
	private void measureChildrenExpanded(int widthMeasureSpec, int heightMeasureSpec) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	private int computeOwnHeightExpanded() {
		int leftHeight = presentation.getExpanded(0).getMeasuredHeight();
		int rightHeight = presentation.getExpanded(1).getMeasuredHeight();
		int ownHeight = Math.max(leftHeight, rightHeight);
		
		leftHeight = presentation.getExpanded(2).getMeasuredHeight();
		rightHeight = presentation.getExpanded(3).getMeasuredHeight();
		ownHeight += Math.max(leftHeight, rightHeight);
		
		leftHeight = presentation.getExpanded(4).getMeasuredHeight();
		rightHeight = presentation.getExpanded(5).getMeasuredHeight();
		ownHeight += Math.max(leftHeight, rightHeight);
		
		if (substitution.hasText()) {
			leftHeight = presentation.getExpanded(6).getMeasuredHeight();
			rightHeight = presentation.getExpanded(7).getMeasuredHeight();
			ownHeight += Math.max(leftHeight, rightHeight);
		}
		return ownHeight;
	}
	
	private int computeChildrenMeasuredStateExpanded() {
		int mode = 0;
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			mode = mode | getChildAt(i).getMeasuredState();
		}
		return mode;
	}
	
	private void measureChildrenCollapsed(int widthMeasureSpec, int heightMeasureSpec) {
		measureChild(presentation.getCollapsedLeft(), widthMeasureSpec, heightMeasureSpec);
		measureChild(presentation.getCollapsedMiddle(), widthMeasureSpec, heightMeasureSpec);
		measureChild(presentation.getCollapsedRight(), widthMeasureSpec, heightMeasureSpec);
	}
	
	private int computeOwnHeightCollapsed() {
		int leftHeight = presentation.getCollapsedLeft().getMeasuredHeight();
		int middleHeight = presentation.getCollapsedMiddle().getMeasuredHeight();
		int rightHeight = presentation.getCollapsedRight().getMeasuredHeight();
		int ownHeight = Math.max(leftHeight, rightHeight);
		ownHeight = Math.max(ownHeight, middleHeight);
		return ownHeight;
	}
	
	private int computeChildrenMeasuredStateCollapsed() {
		int mode = presentation.getCollapsedLeft().getMeasuredState();
		mode = mode | presentation.getCollapsedMiddle().getMeasuredState();
		mode = mode | presentation.getCollapsedRight().getMeasuredState();
		return mode;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (isExpanded()) {
			onLayoutExpanded();
		} else {
			onLayoutCollapsed();
		}
	}
	
	private void onLayoutExpanded() {
		int leftColumnWidth = presentation.getExpanded(0).getMeasuredWidth();
		leftColumnWidth = Math.max(leftColumnWidth, presentation.getExpanded(2).getMeasuredWidth());
		leftColumnWidth = Math.max(leftColumnWidth, presentation.getExpanded(4).getMeasuredWidth());
		if (substitution.hasText()) {
			leftColumnWidth = Math.max(leftColumnWidth, presentation.getExpanded(6).getMeasuredWidth());
		}
		leftColumnWidth += paddingLeft;
		
		int top = 0;
		int leftBottom = layoutLeftTextView(presentation.getExpanded(0), leftColumnWidth, top);
		int rightBottom = layoutRightTextView(presentation.getExpanded(1), leftColumnWidth, top);
		
		top = Math.max(leftBottom, rightBottom);
		leftBottom = layoutLeftTextView(presentation.getExpanded(2), leftColumnWidth, top);
		rightBottom = layoutRightTextView(presentation.getExpanded(3), leftColumnWidth, top);
		
		top = Math.max(leftBottom, rightBottom);
		leftBottom = layoutLeftTextView(presentation.getExpanded(4), leftColumnWidth, top);
		rightBottom = layoutRightTextView(presentation.getExpanded(5), leftColumnWidth, top);
		
		if (substitution.hasText()) {
			top = Math.max(leftBottom, rightBottom);
			layoutLeftTextView(presentation.getExpanded(6), leftColumnWidth, top);
			layoutRightTextView(presentation.getExpanded(7), leftColumnWidth, top);
		}
	}
	
	private void onLayoutCollapsed() {
		int leftWidth = presentation.getCollapsedLeft().getMeasuredWidth() + paddingLeft;
		int top = 0;
		layoutLeftTextView(presentation.getCollapsedLeft(), leftWidth, top);
		layoutRightTextView(presentation.getCollapsedMiddle(), leftWidth, top);
		
		// layout course info
		int left = getMeasuredWidth() - presentation.getCollapsedRight().getMeasuredWidth() - paddingLeft;
		int right = getMeasuredWidth() - paddingLeft;
		int bottom = presentation.getCollapsedRight().getMeasuredHeight();
		presentation.getCollapsedRight().layout(left, top, right, bottom);
	}
	
	private int layoutLeftTextView(TextView view, int leftColumnWidth, int top) {
		int left = leftColumnWidth - view.getMeasuredWidth();
		int right = leftColumnWidth;
		int bottom = top + view.getMeasuredHeight();
		view.layout(left, top, right, bottom);
		return bottom;
	}
	
	private int layoutRightTextView(TextView view, int leftWidth, int top) {
		int left = leftWidth + middleGapWidth;
		int right = left + view.getMeasuredWidth();
		int bottom = top + view.getMeasuredHeight();
		view.layout(left, top, right, bottom);
		return bottom;
	}
	
	class ViewHolder {
		public AppearanceAnimation[] appearanceAnimations;
		
		private ViewHolder() {
			int childCount = getChildCount();
			appearanceAnimations = new AppearanceAnimation[childCount];
			for (int i = 0; i < childCount; i++) {
				appearanceAnimations[i] = new AppearanceAnimation(getChildAt(i));
			}
		}
		
		public void setAnimationDuration(long durationMillis) {
			for (AppearanceAnimation aa : appearanceAnimations) {
				aa.setDuration(durationMillis);
			}
		}
		
		public void queryOldPositions() {
			for (AppearanceAnimation aa : appearanceAnimations) {
				aa.queryOldPosition();
			}
		}
		
		public void startAnimations() {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				getChildAt(i).startAnimation(appearanceAnimations[i]);
			}
		}
	}
}
