package org.aweture.wonk.substitutions;

import org.aweture.wonk.models.Substitution;

import android.view.View;
import android.widget.TextView;

public class StudentPresentation extends Presentation {
	
	private static final int[] collapsedStateIndexArr = new int[]{0,4,8};
	
	private Substitution substitution;
	private TextView[] views;

	@Override
	void setSubstitution(Substitution subst) {
		substitution = subst;
	}
	
	@Override
	void setTextViews(TextView[] v) {
		views = v;
	}

	@Override
	void fillInTexts() {
		views[0].setText(substitution.getPeriodNumber() + ". Stunde");
		views[1].setText(substitution.getInstdSubject().getName());
		views[2].setText("statt");
		views[3].setText(substitution.getInstdTeacher().getAccusative());
		views[4].setText(substitution.getKind());
		views[5].setText(substitution.getSubstTeacher().getNameWithCompellation());
		
		if (displayText()) {
			views[6].setText("Info");
			views[7].setText(substitution.getText());
		}
		
		if (displayCourse()) {
			String subjectAbbreviation = substitution.getInstdSubject().getAbbreviation();
			String teacherAbbreviation = substitution.getInstdTeacher().getShortName();
			String course = subjectAbbreviation + " " + teacherAbbreviation;
			views[8].setText(course);
		}
	}

	@Override
	boolean hasRightViewInCollapsedState() {
		return true;
	}

	@Override
	View getCollapsedStateChild(int position) {
		int index = collapsedStateIndexArr[position];
		return views[index];
	}

	@Override
	int expandedStateChildCount() {
		if (displayText())
			return 8;
		else return 6;
	}

	@Override
	View getExpandedStateChild(int position) {
		// position equals index is this case
		return views[position];
	}

	@Override
	void setCollapsedStateVisibilities() {
		for (int i = 0; i < collapsedStateIndexArr.length; i++) {
			views[collapsedStateIndexArr[i]].setVisibility(View.VISIBLE);
		}
		
		final int notVisibleChildrenIndexArr[] = new int[]{1,2,3,5,6,7};
		for (int i = 0; i < notVisibleChildrenIndexArr.length; i++) {
			views[notVisibleChildrenIndexArr[i]].setVisibility(View.GONE);
		}
	}

	@Override
	void setExpandedStateVisibilities() {
		final int visibleChildCount = expandedStateChildCount();
		for (int i = 0; i < visibleChildCount; i++) {
			views[i].setVisibility(View.VISIBLE);
		}
		
		views[8].setVisibility(View.GONE);
	}
	
	private boolean displayCourse() {
		return substitution.getInstdSubject().isConcurrentlyTaught();
	}

}
