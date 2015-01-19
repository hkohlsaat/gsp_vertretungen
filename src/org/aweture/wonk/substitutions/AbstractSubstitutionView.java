package org.aweture.wonk.substitutions;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Subjects;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.models.Teachers;
import org.aweture.wonk.models.Subjects.Subject;
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
 * {@link View} representing {@link Substitution}s by expanding
 * when clicked but not animated.
 * 
 * @author Hannes Kohlsaat
 *
 */
public class AbstractSubstitutionView extends ViewGroup implements Expandable, OnClickListener {
	
	private static final int BLANK_WIDTH_DIP = 8;
	
	private final ViewHolder viewHolder;
	private final ExpansionCoordinator expansionCoordinator;
	
	private final int margin;
	private final int blankWidth;
	private final int screenWidth;
	
	private final Teachers teachers;
	private final Subjects subjects;
	
	private Substitution substitution;

	public AbstractSubstitutionView(Context context) {
		super(context);

		LayoutInflater layoutInflater = LayoutInflater.from(context);
		layoutInflater.inflate(R.layout.view_substitution, this, true);
		
		viewHolder = new ViewHolder();
		expansionCoordinator = ExpansionCoordinator.getInstance();
		
		int marginLeftResourceId = R.dimen.class_substitution_item_margin;
		Resources resources = getResources();
		margin = resources.getDimensionPixelSize(marginLeftResourceId);
		

		DisplayMetrics dm = resources.getDisplayMetrics();
		blankWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BLANK_WIDTH_DIP, dm);
		
		Rect rect = new Rect();
		getWindowVisibleDisplayFrame(rect);
		screenWidth = rect.width();
		
		teachers = new Teachers(context);
		subjects = new Subjects(context);
		
		setOnClickListener(this);
	}
	
	public ViewHolder getViewHolder() {
		return viewHolder;
	}
	
	public boolean isExpanded() {
		return expansionCoordinator.isExpanded(this);
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
	
	private void setExpandedVisibilities() {
		viewHolder.subject.setVisibility(VISIBLE);
		viewHolder.course.setVisibility(GONE);
		viewHolder.instdOf.setVisibility(VISIBLE);
		viewHolder.instdTeacher.setVisibility(VISIBLE);
		viewHolder.substTeacher.setVisibility(VISIBLE);
		if (viewHolder.text.getText().toString().isEmpty()) {
			viewHolder.info.setVisibility(GONE);
			viewHolder.text.setVisibility(GONE);
		} else {
			viewHolder.info.setVisibility(VISIBLE);
			viewHolder.text.setVisibility(VISIBLE);
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
		if (isVisible(viewHolder.info)) {
			measureChild(viewHolder.info, widthMeasureSpec, heightMeasureSpec);
			measureChild(viewHolder.text, widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	private int computeOwnHeightExpanded() {
		int bottom1 = viewHolder.period.getMeasuredHeight();
		int bottom2 = viewHolder.subject.getMeasuredHeight();
		int height = Math.max(bottom1, bottom2);
		
		bottom1 = viewHolder.instdOf.getMeasuredHeight();
		bottom2 = viewHolder.instdTeacher.getMeasuredHeight();
		height += Math.max(bottom1, bottom2);
		
		bottom1 = viewHolder.kind.getMeasuredHeight();
		bottom2 = viewHolder.substTeacher.getMeasuredHeight();
		height += Math.max(bottom1, bottom2);
		
		if (isVisible(viewHolder.info)) {
			bottom1 = viewHolder.info.getMeasuredHeight();
			bottom2 = viewHolder.text.getMeasuredHeight();
			height += Math.max(bottom1, bottom2);
		}
		return height;
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
		int bottom1 = viewHolder.period.getMeasuredHeight();
		int bottom2 = viewHolder.kind.getMeasuredHeight();
		int height = Math.max(bottom1, bottom2);
		return height;
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
		int leftWidth = viewHolder.period.getMeasuredWidth();
		leftWidth = Math.max(leftWidth, viewHolder.instdOf.getMeasuredWidth());
		leftWidth = Math.max(leftWidth, viewHolder.kind.getMeasuredWidth());
		if (isVisible(viewHolder.info)) {
			leftWidth = Math.max(leftWidth, viewHolder.info.getMeasuredWidth());
		}
		leftWidth += margin;
		
		int bottom1 = layoutLeftTextView(viewHolder.period, leftWidth, 0);
		int bottom2 = layoutRightTextView(viewHolder.subject, leftWidth, 0);
		int nextTop = Math.max(bottom1, bottom2);
		
		bottom1 = layoutLeftTextView(viewHolder.instdOf, leftWidth, nextTop);
		bottom2 = layoutRightTextView(viewHolder.instdTeacher, leftWidth, nextTop);
		nextTop = Math.max(bottom1, bottom2);
		
		bottom1 = layoutLeftTextView(viewHolder.kind, leftWidth, nextTop);
		bottom2 = layoutRightTextView(viewHolder.substTeacher, leftWidth, nextTop);
		nextTop = Math.max(bottom1, bottom2);
		
		if (isVisible(viewHolder.info)) {
			layoutLeftTextView(viewHolder.info, leftWidth, nextTop);
			layoutRightTextView(viewHolder.text, leftWidth, nextTop);
		}
	}
	
	private void onLayoutCollapsed() {
		int leftWidth = viewHolder.period.getMeasuredWidth();
		leftWidth += margin;
		layoutLeftTextView(viewHolder.period, leftWidth, 0);
		layoutRightTextView(viewHolder.kind, leftWidth, 0);
		
		// layout course info
		int left = getMeasuredWidth() - viewHolder.course.getMeasuredWidth() - margin;
		int top = 0;
		int right = getMeasuredWidth() - margin;
		int bottom = viewHolder.course.getMeasuredHeight();
		viewHolder.course.layout(left, top, right, bottom);
	}
	
	private int layoutLeftTextView(TextView view, int leftWidth, int top) {
		int left = leftWidth - view.getMeasuredWidth();
		int right = leftWidth;
		int bottom = top + view.getMeasuredHeight();
		view.layout(left, top, right, bottom);
		return bottom;
	}
	
	private int layoutRightTextView(TextView view, int leftWidth, int top) {
		int left = leftWidth + blankWidth;
		int right = left + view.getMeasuredWidth();
		int bottom = top + view.getMeasuredHeight();
		view.layout(left, top, right, bottom);
		return bottom;
	}
	
	boolean isVisible(View view) {
		return view.getVisibility() != GONE;
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
		}
	}
}
