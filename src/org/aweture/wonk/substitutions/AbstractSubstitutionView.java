package org.aweture.wonk.substitutions;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Subjects;
import org.aweture.wonk.models.Subjects.Subject;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.models.Teachers;
import org.aweture.wonk.models.Teachers.Teacher;

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
	
	private final Teachers teachers;
	private final Subjects subjects;
	
	private Substitution substitution;
	
	
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
		
		teachers = new Teachers(context);
		subjects = new Subjects(context);
		
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
	public void onClick(View v) {
		expansionCoordinator.clicked(this);
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
		
		Teacher teacher = teachers.getTeacher(substTeacherShort);
		String substTeacher = teacher.getName();
		teacher = teachers.getTeacher(instdTeacherShort);
		String instdTeacher = teacher.getAccusative();
		
		Subject subject = subjects.getSubject(instdSubjectShort);
		String instdSubject = subject.getName();
		
		viewHolder.period.setText(period);
		viewHolder.subject.setText(instdSubject);
		viewHolder.instdTeacher.setText(instdTeacher);
		viewHolder.kind.setText(kind);
		viewHolder.substTeacher.setText(substTeacher);
		viewHolder.text.setText(text);
		
		if (subject.isConcurrentlyTaught()) {
			String subjectAbbreviation = subject.getAbbreviation();
			String teacherShort = teacher.getShortName();
			viewHolder.course.setText(subjectAbbreviation + " " + teacherShort);
		} else {
			viewHolder.course.setText("");
		}
	}
	
	private void applyVisibilityProperties() {
		if (isExpanded()) {
			setExpandedVisibilities();
		} else {
			setCollapsedVisibilities();
		}
	}
	
	private boolean isExpanded() {
		return expansionCoordinator.isExpanded(this);
	}
	
	private void setExpandedVisibilities() {
		viewHolder.subject.setVisibility(VISIBLE);
		viewHolder.course.setVisibility(GONE);
		viewHolder.instdOf.setVisibility(VISIBLE);
		viewHolder.instdTeacher.setVisibility(VISIBLE);
		viewHolder.substTeacher.setVisibility(VISIBLE);
		if (substitution.hasText()) {
			viewHolder.info.setVisibility(VISIBLE);
			viewHolder.text.setVisibility(VISIBLE);
		} else {
			viewHolder.info.setVisibility(GONE);
			viewHolder.text.setVisibility(GONE);
		}
	}
	
	private void setCollapsedVisibilities() {
		viewHolder.subject.setVisibility(GONE);
		viewHolder.course.setVisibility(VISIBLE);
		viewHolder.instdOf.setVisibility(GONE);
		viewHolder.instdTeacher.setVisibility(GONE);
		viewHolder.substTeacher.setVisibility(GONE);
		viewHolder.text.setVisibility(GONE);
		viewHolder.info.setVisibility(GONE);
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
		measureChild(viewHolder.period, widthMeasureSpec, heightMeasureSpec);
		measureChild(viewHolder.kind, widthMeasureSpec, heightMeasureSpec);
		measureChild(viewHolder.subject, widthMeasureSpec, heightMeasureSpec);
		measureChild(viewHolder.instdOf, widthMeasureSpec, heightMeasureSpec);
		measureChild(viewHolder.instdTeacher, widthMeasureSpec, heightMeasureSpec);
		measureChild(viewHolder.substTeacher, widthMeasureSpec, heightMeasureSpec);
		if (substitution.hasText()) {
			measureChild(viewHolder.info, widthMeasureSpec, heightMeasureSpec);
			measureChild(viewHolder.text, widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	private int computeOwnHeightExpanded() {
		int leftHeight = viewHolder.period.getMeasuredHeight();
		int rightHeight = viewHolder.subject.getMeasuredHeight();
		int ownHeight = Math.max(leftHeight, rightHeight);
		
		leftHeight = viewHolder.instdOf.getMeasuredHeight();
		rightHeight = viewHolder.instdTeacher.getMeasuredHeight();
		ownHeight += Math.max(leftHeight, rightHeight);
		
		leftHeight = viewHolder.kind.getMeasuredHeight();
		rightHeight = viewHolder.substTeacher.getMeasuredHeight();
		ownHeight += Math.max(leftHeight, rightHeight);
		
		if (substitution.hasText()) {
			leftHeight = viewHolder.info.getMeasuredHeight();
			rightHeight = viewHolder.text.getMeasuredHeight();
			ownHeight += Math.max(leftHeight, rightHeight);
		}
		return ownHeight;
	}
	
	private int computeChildrenMeasuredStateExpanded() {
		int mode = viewHolder.period.getMeasuredState();
		mode = mode | viewHolder.subject.getMeasuredState();
		mode = mode | viewHolder.instdOf.getMeasuredState();
		mode = mode | viewHolder.instdTeacher.getMeasuredState();
		mode = mode | viewHolder.kind.getMeasuredState();
		mode = mode | viewHolder.substTeacher.getMeasuredState();
		mode = mode | viewHolder.info.getMeasuredState();
		mode = mode | viewHolder.text.getMeasuredState();
		return mode;
	}
	
	private void measureChildrenCollapsed(int widthMeasureSpec, int heightMeasureSpec) {
		measureChild(viewHolder.period, widthMeasureSpec, heightMeasureSpec);
		measureChild(viewHolder.kind, widthMeasureSpec, heightMeasureSpec);
		measureChild(viewHolder.course, widthMeasureSpec, heightMeasureSpec);
	}
	
	private int computeOwnHeightCollapsed() {
		int leftHeight = viewHolder.period.getMeasuredHeight();
		int rightHeight = viewHolder.kind.getMeasuredHeight();
		int ownHeight = Math.max(leftHeight, rightHeight);
		return ownHeight;
	}
	
	private int computeChildrenMeasuredStateCollapsed() {
		int mode = viewHolder.period.getMeasuredState();
		mode = mode | viewHolder.kind.getMeasuredState();
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
		int leftColumnWidth = viewHolder.period.getMeasuredWidth();
		leftColumnWidth = Math.max(leftColumnWidth, viewHolder.instdOf.getMeasuredWidth());
		leftColumnWidth = Math.max(leftColumnWidth, viewHolder.kind.getMeasuredWidth());
		if (substitution.hasText()) {
			leftColumnWidth = Math.max(leftColumnWidth, viewHolder.info.getMeasuredWidth());
		}
		leftColumnWidth += paddingLeft;
		
		int top = 0;
		int leftBottom = layoutLeftTextView(viewHolder.period, leftColumnWidth, top);
		int rightBottom = layoutRightTextView(viewHolder.subject, leftColumnWidth, top);
		
		top = Math.max(leftBottom, rightBottom);
		leftBottom = layoutLeftTextView(viewHolder.instdOf, leftColumnWidth, top);
		rightBottom = layoutRightTextView(viewHolder.instdTeacher, leftColumnWidth, top);
		
		top = Math.max(leftBottom, rightBottom);
		leftBottom = layoutLeftTextView(viewHolder.kind, leftColumnWidth, top);
		rightBottom = layoutRightTextView(viewHolder.substTeacher, leftColumnWidth, top);
		
		if (substitution.hasText()) {
			top = Math.max(leftBottom, rightBottom);
			layoutLeftTextView(viewHolder.info, leftColumnWidth, top);
			layoutRightTextView(viewHolder.text, leftColumnWidth, top);
		}
	}
	
	private void onLayoutCollapsed() {
		int leftWidth = viewHolder.period.getMeasuredWidth() + paddingLeft;
		int top = 0;
		layoutLeftTextView(viewHolder.period, leftWidth, top);
		layoutRightTextView(viewHolder.kind, leftWidth, top);
		
		// layout course info
		int left = getMeasuredWidth() - viewHolder.course.getMeasuredWidth() - paddingLeft;
		int right = getMeasuredWidth() - paddingLeft;
		int bottom = viewHolder.course.getMeasuredHeight();
		viewHolder.course.layout(left, top, right, bottom);
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
		public final TextView period;
		public final TextView subject;
		public final TextView course;
		public final TextView instdOf;
		public final TextView instdTeacher;
		public final TextView kind;
		public final TextView substTeacher;
		public final TextView info;
		public final TextView text;
		
		public AppearanceAnimation periodAnimation;
		public AppearanceAnimation subjectAnimation;
		public AppearanceAnimation courseAnimation;
		public AppearanceAnimation instdOfAnimation;
		public AppearanceAnimation instdTeacherAnimation;
		public AppearanceAnimation kindAnimation;
		public AppearanceAnimation substTeacherAnimation;
		public AppearanceAnimation infoAnimation;
		public AppearanceAnimation textAnimation;
		
		
		private ViewHolder() {
			period = (TextView) findViewById(R.id.periodTextView);
			subject = (TextView) findViewById(R.id.subjectTextView);
			course = (TextView) findViewById(R.id.courseSpecificationTextView);
			instdOf = (TextView) findViewById(R.id.instdOfTextView);
			instdTeacher = (TextView) findViewById(R.id.instdTeacherTextView);
			kind = (TextView) findViewById(R.id.kindTextView);
			substTeacher = (TextView) findViewById(R.id.substTeacherTextView);
			text = (TextView) findViewById(R.id.textTextView);
			info = (TextView) findViewById(R.id.infoTextView);

			periodAnimation = new AppearanceAnimation(period);
			subjectAnimation = new AppearanceAnimation(subject);
			courseAnimation = new AppearanceAnimation(course);
			instdOfAnimation = new AppearanceAnimation(instdOf);
			instdTeacherAnimation = new AppearanceAnimation(instdTeacher);
			kindAnimation = new AppearanceAnimation(kind);
			substTeacherAnimation = new AppearanceAnimation(substTeacher);
			textAnimation = new AppearanceAnimation(text);
			infoAnimation = new AppearanceAnimation(info);
		}
		
		public void setAnimationDuration(long durationMillis) {
			periodAnimation.setDuration(durationMillis);
			subjectAnimation.setDuration(durationMillis);
			courseAnimation.setDuration(durationMillis);
			instdOfAnimation.setDuration(durationMillis);
			instdTeacherAnimation.setDuration(durationMillis);
			kindAnimation.setDuration(durationMillis);
			substTeacherAnimation.setDuration(durationMillis);
			textAnimation.setDuration(durationMillis);
			infoAnimation.setDuration(durationMillis);
		}
		
		public void queryOldPositions() {
			periodAnimation.queryOldPosition();
			subjectAnimation.queryOldPosition();
			courseAnimation.queryOldPosition();
			instdOfAnimation.queryOldPosition();
			instdTeacherAnimation.queryOldPosition();
			kindAnimation.queryOldPosition();
			substTeacherAnimation.queryOldPosition();
			textAnimation.queryOldPosition();
			infoAnimation.queryOldPosition();
		}
		
		public void startAnimations() {
			period.startAnimation(periodAnimation);
			subject.startAnimation(subjectAnimation);
			course.startAnimation(courseAnimation);
			instdOf.startAnimation(instdOfAnimation);
			instdTeacher.startAnimation(instdTeacherAnimation);
			kind.startAnimation(kindAnimation);
			substTeacher.startAnimation(substTeacherAnimation);
			text.startAnimation(textAnimation);
			info.startAnimation(infoAnimation);
		}
	}
}
