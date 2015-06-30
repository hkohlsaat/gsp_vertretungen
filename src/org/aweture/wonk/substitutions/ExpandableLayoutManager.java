package org.aweture.wonk.substitutions;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ExpandableLayoutManager extends LayoutManager {

	private int firstTopAddition = 0;
	private boolean datasetChanged = false;

	@Override
	public LayoutParams generateDefaultLayoutParams() {
		final int width = LayoutParams.MATCH_PARENT;
		final int height = LayoutParams.WRAP_CONTENT;
		return new LayoutParams(width, height);
	}
	
	@Override
	public void onItemsChanged(RecyclerView recyclerView) {
		datasetChanged = true;
	}
	
	@Override
	public void onLayoutChildren(Recycler recycler, State state) {
		if (getItemCount() == 0) {
			removeAndRecycleAllViews(recycler);
			return;
		}
		
		if (datasetChanged) {
			datasetChanged = false;
			removeAndRecycleAllViews(recycler);
		}
		
		int decoratedTop = 0;
		int firstPosition = 0;
		if (getChildCount() > 0) {
			final View firstItem = getChildAt(0);
			decoratedTop = getDecoratedTop(firstItem);
			firstPosition = getPosition(firstItem);
			if (firstPosition + 1 < getItemCount()) {
				measureChild(firstItem, 0, 0);
				final int top = getDecoratedTop(firstItem);
				final int height = getDecoratedMeasuredHeight(firstItem);
				final int bottom = top + height + getPaddingTop() - getHeight() + getPaddingBottom() + firstTopAddition;
				detachAndScrapAttachedViews(recycler);
				findGreaterPositionItemAndStartFill(firstPosition + 1, bottom, 0, recycler, state);
				firstTopAddition = 0;
				return;
			}
		}
		
		detachAndScrapAttachedViews(recycler);
		fill(firstPosition, decoratedTop, recycler, state);
	}
	
	private void fill(final int firstPosition, final int firstTop, Recycler recycler, State state) {
		final int recyclerViewContentHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		final int itemsInAdapter = getItemCount();
		for (int position = firstPosition, height = firstTop; height < recyclerViewContentHeight && position < itemsInAdapter; position++) {
			final View view = recycler.getViewForPosition(position);
			addView(view);
			
			measureChildWithMargins(view, 0, 0);
			final int measuredHeight = getDecoratedMeasuredHeight(view);
			final int measuredWidth = getDecoratedMeasuredWidth(view);
			
			final int recyclerViewPaddingLeft = getPaddingLeft();
			final int recyclerViewPadddingTop = getPaddingTop();
			
			final int left = recyclerViewPaddingLeft;
			final int top = recyclerViewPadddingTop + height;
			final int right = left + measuredWidth;
			final int bottom = top + measuredHeight;
			
			layoutDecorated(view, left, top, right, bottom);
			height += measuredHeight;
		}
	}
	
	@Override
	public boolean canScrollHorizontally() {
		return false;
	}
	
	@Override
	public boolean canScrollVertically() {
		return true;
	}
	
	@Override
	public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
		if (getChildCount() == 0) {
			return 0;
		}
		
		if (dy > 0) { // scroll down - movement up
			final View lastVisibleView = getChildAt(getChildCount() - 1);
			final int bottom = getDecoratedBottom(lastVisibleView) - (getHeight() - getPaddingBottom());
			
			if (bottom >= dy) {
				offsetChildrenVertical(-dy);
				return dy;
			}
			
			final int position = getPosition(lastVisibleView);
			if (position + 1 < getItemCount()) {
				detachAndScrapAttachedViews(recycler);
				return findGreaterPositionItemAndStartFill(position + 1, bottom - dy, dy, recycler, state);
			} else if (bottom > 0) {
				// that means position is the last ( == itemCount - 1)
				// that means 0 < bottom < dy
				final int top = getDecoratedTop(lastVisibleView) - bottom;
				if (position > 0) {
					detachAndScrapAttachedViews(recycler);
					findLesserPositionItemAndStartFill(position - 1, top, bottom, recycler, state);
					// return value of findLesserPosition...(...) is equal to bottom
				} else {
					detachAndScrapAttachedViews(recycler);
					fill(position, top, recycler, state);
				}
				return bottom;
			} else {
				return 0;
			}
		} else { // scroll up - movement down
			final View firstVisibleView = getChildAt(0);
			final int top = getDecoratedTop(firstVisibleView) - getPaddingTop();
			
			if (top <= dy) {
				offsetChildrenVertical(-dy);
				return dy;
			}
			
			final int position = getPosition(firstVisibleView);
			if (position > 0) {
				detachAndScrapAttachedViews(recycler);
				return findLesserPositionItemAndStartFill(position - 1, top - dy, dy, recycler, state);
			} else if (top < 0) {
				// that means 0 > top > dy
				// that means position == 0
				detachAndScrapAttachedViews(recycler);
				fill(position, 0, recycler, state);
				return top;
			} else {
				return 0;
			}
		}
	}
	
	/**
	 * Method to recursively find the new bottom view while scrolling down. An invocation
	 * means: (Recursively) Find a view bound to the specified position (in the adapter)
	 * whose bottom would be greater than 0 from the specified top downwards.<br>
	 * This method is not the exact opposite of
	 * {@link #findLesserPositionItemAndStartFill(int, int, int, Recycler, State)}
	 * because it does call that method to find the topmost view and trigger a fill
	 * after having found the bottommost and if necessary having adjusted the dy value<br>
	 * Calling this method guarantees that (position - 1) >= 0 && (position + 1) <= {@link #getItemCount()}
	 * 
	 * @param position in the adapter whose view should get tested to fulfill the specified requirements
	 * @param top value that view should get minus ({@link #getHeight()} - {@link #getPaddingBottom()}) so that
	 * 			it can be checked against 0 when actually checking against RecyclerViews' bottom padding boundary
	 * @param dy scroll delta
	 * @param recycler object to retrieve views from
	 * @param state
	 * @return how far scroll can succeed
	 */
	private int findGreaterPositionItemAndStartFill(int position, int top, int dy, Recycler recycler, State state) {
		final View view = recycler.getViewForPosition(position);
		addView(view);
		
		measureChildWithMargins(view, 0, 0);
		final int bottom = top + getDecoratedMeasuredHeight(view);
		
		if (bottom < 0) {
			if (position + 1 < getItemCount()) {
				return findGreaterPositionItemAndStartFill(position + 1, bottom, dy, recycler, state);
			} else {
				detachAndScrapAttachedViews(recycler);
				final int contentHeight = getHeight() - getPaddingBottom();
				return findLesserPositionItemAndStartFill(position - 1, top - bottom + contentHeight, dy + bottom, recycler, state);
				// same as
				// return dy + bottom;
			}
		} else {
			detachAndScrapAttachedViews(recycler);
			final int contentHeight = getHeight() - getPaddingBottom();
			return findLesserPositionItemAndStartFill(position - 1, top + contentHeight, dy, recycler, state);
			// same as
			// return dy;
		}
	}
	
	/**
	 * Method to recursively find the new top view while scrolling up. An invocation
	 * means: (Recursively) Find a view bound to the specified position (in the adapter)
	 * whose top would be less than 0 from the specified bottom upwards and eventually
	 * trigger a fill.
	 * 
	 * @param position in the adapter whose view should get tested to fulfill the specified requirements
	 * @param bottom value that view should get
	 * @param dy scroll delta
	 * @param recycler object to retrieve views from
	 * @param state
	 * @return how far scroll succeeded
	 */
	private int findLesserPositionItemAndStartFill(int position, int bottom, int dy, Recycler recycler, State state) {
		final View view = recycler.getViewForPosition(position);
		addView(view);
		
		measureChildWithMargins(view, 0, 0);
		final int top = bottom - getDecoratedMeasuredHeight(view);
		
		if (top > 0) {
			if (position > 0) {
				return findLesserPositionItemAndStartFill(position - 1, top, dy, recycler, state);
			} else {
				detachAndScrapAttachedViews(recycler);
				fill(position, 0, recycler, state);
				return dy + top;
			}
		} else {
			detachAndScrapAttachedViews(recycler);
			fill(position, top, recycler, state);
			return dy;
		}
	}
	
	
	public abstract class ExpandableViewHolder extends ViewHolder implements OnPreDrawListener{
		
		private boolean firstPreDrawPass = true;
		
		private int heightBefore, heightAfter;
		private int topBefore, topAfter;
		private int bottomAfter;

		public ExpandableViewHolder(View itemView) {
			super(itemView);
		}
		
		public void animateItemSizeChange() {
			RecyclerView rv = getRecyclerView();
			
			topBefore = getTop() - rv.getPaddingTop();
			heightBefore = getHeight();
			
			rv.getViewTreeObserver().addOnPreDrawListener(this);
			requestLayout();
		}
		
		public abstract RecyclerView getRecyclerView();
		public abstract int getHeight();
		public abstract void setHeight(int height);
		public abstract int getTop();
		public abstract int getBottom();
		public abstract void startAnimation(Animation animation);
		
		@Override
		public boolean onPreDraw() {
			RecyclerView rv = getRecyclerView();
			
			if (firstPreDrawPass) {
				firstPreDrawPass = false;
				
				// save values
				heightAfter = getHeight();
				topAfter = getTop() - rv.getPaddingTop();
				bottomAfter = rv.getHeight() - rv.getPaddingBottom() - getBottom();
				
				// restore values
				setHeight(heightBefore);
				
				// start at same position
				firstTopAddition += topBefore - topAfter;
				
				rv.requestLayout();
				return false;
			} else {
				firstPreDrawPass = true;
				rv.getViewTreeObserver().removeOnPreDrawListener(this);
				
				ExpansionAnimation ea = new ExpansionAnimation();
				startAnimation(ea);
				return true;
			}
		}
		
		
		private class ExpansionAnimation extends Animation {
			
			private int scrolled;
			
			public ExpansionAnimation() {
				setDuration(200);
			}
			
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime != 1) {
					final int height = (int) ((heightBefore * (1 - interpolatedTime)) + (heightAfter * (interpolatedTime)));
					setHeight(height);
				} else {
					setHeight(LayoutParams.WRAP_CONTENT);
				}
				
				// animate item in, if partly under lower bound
				int delta = (int) (bottomAfter < 0 ? bottomAfter * interpolatedTime - scrolled: 0);
				
				// animate item in, if partly over upper bound
				delta += (int) (topAfter < 0 ? (-topAfter) * interpolatedTime - scrolled : 0);
				
				// animate item into place, if it has to fill space below it
				delta += (int) (topBefore < topAfter ? (topAfter - topBefore) * interpolatedTime - scrolled: 0);
				
				scrolled += delta;
				firstTopAddition += delta;
				
				requestLayout();
			}
		}
		
	}
}
