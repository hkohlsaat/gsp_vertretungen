package org.aweture.wonk.substitutions;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.substitutions.Presentation.PresentationMode;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
	private static final int PADDING_HORIZONTAL_DIP = 16;
	private static final int PADDING_VERTICAL_DIP = 12;
	
	private static final ExpansionCoordinator expansionCoordinator = new ExpansionCoordinator();
	
	private final ViewHolder viewHolder;
	
	private final int horizontalPadding;
	private final int verticalPadding;
	private final int middleGapWidth;

	private final TextView[] views;
	private PresentationMode presentationMode;
	private Presentation presentation;
	
	public AbstractSubstitutionView(Context context) {
		super(context);

		final Resources resources = getResources();
		final float textSize = resources.getDimension(R.dimen.text_size);
		
		views = Presentation.createNewTextViews(getContext(), textSize);
		for (int i = 0; i < views.length; i++) {
			LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			attachViewToParent(views[i], i, layoutParams);
		}
		viewHolder = new ViewHolder();
		
		DisplayMetrics dm = resources.getDisplayMetrics();
		horizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING_HORIZONTAL_DIP, dm);
		verticalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING_VERTICAL_DIP, dm);
		middleGapWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIDDLE_GAP_WIDTH_DIP, dm);
		
		setOnClickListener(this);
	}
	
	ViewHolder getViewHolder() {
		return viewHolder;
	}
	
	@Override
	public void changeExpansionState(boolean animate) {
		applyVisibilityProperties();
	}
	
	@Override
	public View getView() {
		return this;
	}
	
	@Override
	public void onClick(View v) {
		expansionCoordinator.clicked(this);
	}
	
	public void setSubstitution(Substitution substitution, PresentationMode presentationMode) {
		if (this.presentationMode != presentationMode) {
			this.presentationMode = presentationMode;
			presentation = Presentation.applyPresentation(substitution, views, presentationMode);
		} else {
			presentation.applyPresenation(substitution);
		}
		
		applyVisibilityProperties();
	}
	
	private void applyVisibilityProperties() {
		if (isExpanded()) {
			presentation.setExpandedStateVisibilities();
		} else {
			presentation.setCollapsedStateVisibilities();
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
		int width = MeasureSpec.getSize(widthMeasureSpec);
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
		final int expandedChildCount = presentation.expandedStateChildCount();
		for (int i = 0; i < expandedChildCount; i++) {
			measureChild(presentation.getExpandedStateChild(i), widthMeasureSpec, heightMeasureSpec);
		}
		
		int maxLeftChildWidth = 0;
		int maxRightChildWidth = 0;
		final int visibleChildCount = presentation.expandedStateChildCount();
		for (int i = 0; i < visibleChildCount; i += 2) {
			final int leftMeasuredWidth = presentation.getExpandedStateChild(i).getMeasuredWidth();
			maxLeftChildWidth = Math.max(maxLeftChildWidth, leftMeasuredWidth);
		}
		for (int i = 1; i < visibleChildCount; i += 2) {
			final int rightMeasuredWidth = presentation.getExpandedStateChild(i).getMeasuredWidth();
			maxRightChildWidth = Math.max(maxRightChildWidth, rightMeasuredWidth);
		}
		
		final int horizontalSpace = (horizontalPadding * 2) + middleGapWidth;
		final int widthPrediction = horizontalSpace + maxLeftChildWidth + maxRightChildWidth;
		final int widthRequirement = MeasureSpec.getSize(widthMeasureSpec);
		if (widthRequirement < widthPrediction) {
			final int maxRightWidth = widthRequirement - horizontalSpace - maxLeftChildWidth;
			final int rightWidthSpec = MeasureSpec.makeMeasureSpec(maxRightWidth, MeasureSpec.AT_MOST);
			for (int i = 1; i < expandedChildCount; i += 2) {
				measureChild(presentation.getExpandedStateChild(i), rightWidthSpec, heightMeasureSpec);
			}
		}
	}
	
	private int computeOwnHeightExpanded() {
		int rowHeightSums = 0;
		final int padding = verticalPadding * 2;
		final int expandedChildCount = presentation.expandedStateChildCount();
		for (int i = 1; i < expandedChildCount; i += 2) {
			final int leftHeight = presentation.getExpandedStateChild(i - 1).getMeasuredHeight();
			final int rightHeight = presentation.getExpandedStateChild(i).getMeasuredHeight();
			rowHeightSums += Math.max(leftHeight, rightHeight) + padding;
		} if (expandedChildCount % 2 == 1) {
			final int leftHeight = presentation.getExpandedStateChild(expandedChildCount - 1).getMeasuredHeight();
			rowHeightSums += leftHeight + padding;
		}
		return rowHeightSums;
	}
	
	private int computeChildrenMeasuredStateExpanded() {
		int mode = 0;
		final int expandedChildCount = presentation.expandedStateChildCount();
		for (int i = 0; i < expandedChildCount; i++) {
			mode = mode | presentation.getExpandedStateChild(i).getMeasuredState();
		}
		return mode;
	}
	
	private void measureChildrenCollapsed(int widthMeasureSpec, int heightMeasureSpec) {
		measureChild(presentation.getCollapsedStateChild(0), widthMeasureSpec, heightMeasureSpec);
		measureChild(presentation.getCollapsedStateChild(1), widthMeasureSpec, heightMeasureSpec);
		if (presentation.hasRightViewInCollapsedState()) {
			measureChild(presentation.getCollapsedStateChild(2), widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	private int computeOwnHeightCollapsed() {
		final int leftHeight = presentation.getCollapsedStateChild(0).getMeasuredHeight();
		final int middleHeight = presentation.getCollapsedStateChild(1).getMeasuredHeight();
		final int padding = verticalPadding * 2;
		int height = Math.max(leftHeight, middleHeight) + padding;
		if (presentation.hasRightViewInCollapsedState()) {
			final int rightHeight = presentation.getCollapsedStateChild(2).getMeasuredHeight();
			height = Math.max(height, rightHeight);
		}
		return height;
	}
	
	private int computeChildrenMeasuredStateCollapsed() {
		int mode = presentation.getCollapsedStateChild(0).getMeasuredState();
		mode = mode | presentation.getCollapsedStateChild(1).getMeasuredState();
		if (presentation.hasRightViewInCollapsedState()) {
			mode = mode | presentation.getCollapsedStateChild(2).getMeasuredState();
		}
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
		int leftColumnWidth = 0;
		final int expandedChildCount = presentation.expandedStateChildCount();
		for (int i = 0; i < expandedChildCount; i += 2) {
			final int width = presentation.getExpandedStateChild(i).getMeasuredWidth();
			leftColumnWidth = Math.max(leftColumnWidth, width);
		}
		leftColumnWidth += horizontalPadding;
		
		int top = 0;
		int leftBottom = 0;
		int rightBottom = 0;
		
		for (int i = 1; i < expandedChildCount; i += 2) {
			leftBottom = layoutLeftTextView(presentation.getExpandedStateChild(i - 1), leftColumnWidth, top);
			rightBottom = layoutRightTextView(presentation.getExpandedStateChild(i), leftColumnWidth, top);
			top = Math.max(leftBottom, rightBottom);
		} if (expandedChildCount % 2 == 1) {
			layoutLeftTextView(presentation.getExpandedStateChild(expandedChildCount - 1), leftColumnWidth, top);
		}
	}
	
	private void onLayoutCollapsed() {
		final int leftWidth = presentation.getCollapsedStateChild(0).getMeasuredWidth() + horizontalPadding;
		final int top = 0;
		layoutLeftTextView(presentation.getCollapsedStateChild(0), leftWidth, top);
		layoutRightTextView(presentation.getCollapsedStateChild(1), leftWidth, top);
		
		if (presentation.hasRightViewInCollapsedState()) {
			final int width = getMeasuredWidth();
			final int left = width - presentation.getCollapsedStateChild(2).getMeasuredWidth() - horizontalPadding;
			final int right = width - horizontalPadding;
			final int bottom = presentation.getCollapsedStateChild(2).getMeasuredHeight() + verticalPadding;
			presentation.getCollapsedStateChild(2).layout(left, verticalPadding, right, bottom);
		}
	}
	
	private int layoutLeftTextView(View view, final int right, int top) {
		top += verticalPadding;
		final int left = right - view.getMeasuredWidth();
		final int height = view.getMeasuredHeight();
		final int bottom = top + height;
		view.layout(left, top, right, bottom);
		return bottom + verticalPadding;
	}
	
	private int layoutRightTextView(View view, final int leftWidth, int top) {
		final int left = leftWidth + middleGapWidth;
		final int right = left + view.getMeasuredWidth();
		final int height = view.getMeasuredHeight();
		top += verticalPadding;
		final int bottom = top + height;
		view.layout(left, top, right, bottom);
		return bottom + verticalPadding;
	}
	
	class ViewHolder {
		public AppearanceAnimation[] appearanceAnimations;
		
		private ViewHolder() {
			appearanceAnimations = new AppearanceAnimation[views.length];
			for (int i = 0; i < views.length; i++) {
				appearanceAnimations[i] = new AppearanceAnimation(views[i]);
			}
		}
		
		public void setAnimationDuration(long durationMillis) {
			for (int i = 0; i < appearanceAnimations.length; i++) {
				appearanceAnimations[i].setDuration(durationMillis);
			}
		}
		
		public void queryOldPositions() {
			for (int i = 0; i < appearanceAnimations.length; i++) {
				appearanceAnimations[i].queryOldPosition();
			}
		}
		
		public void startAnimations() {
			for (int i = 0; i < views.length; i++) {
				views[i].startAnimation(appearanceAnimations[i]);
			}
		}
	}
}
