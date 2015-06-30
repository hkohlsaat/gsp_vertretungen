package org.aweture.wonk.substitutions;

import org.aweture.wonk.models.Substitution;

import android.view.View;
import android.widget.TextView;

public class TeacherPresentation extends Presentation {

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
		views[1].setText(substitution.getKind());
		views[2].setText("in der");
		views[3].setText(substitution.getClassName());
		views[4].setText("statt");
		final String teacher = substitution.getInstdTeacher().getAccusative();
		final String subject = substitution.getInstdSubject().getName();
		views[5].setText(teacher + " in " + subject);
		
		if (displayText()) {
			views[6].setText("Info");
			views[7].setText(substitution.getText());
		} else {
			views[6].setText("");
			views[7].setText("");
		}
		views[8].setText("");
	}

	@Override
	boolean hasRightViewInCollapsedState() {
		return false;
	}

	@Override
	View getCollapsedStateChild(int position) {
		return views[position];
	}

	@Override
	int expandedStateChildCount() {
		if (displayText())
			return 8;
		else
			return 6;
	}

	@Override
	View getExpandedStateChild(int position) {
		return views[position];
	}

	@Override
	void setCollapsedStateVisibilities() {
		final int visibleChildCount = 2;
		for (int i = 0; i < visibleChildCount; i++) {
			views[i].setVisibility(View.VISIBLE);
		}
		
		for (int i = visibleChildCount; i < CHILD_COUNT; i++) {
			views[i].setVisibility(View.GONE);
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
	
}
