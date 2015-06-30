package org.aweture.wonk.substitutions;

import org.aweture.wonk.models.Substitution;

import android.widget.TextView;

public class TaskProviderPresentation extends TeacherPresentation {
	
	private Substitution substitution;
	private TextView[] views;

	@Override
	void setSubstitution(Substitution subst) {
		substitution = subst;
	}
	
	@Override
	void setTextViews(TextView[] v) {
		super.setTextViews(v);
		views = v;
	}
	
	@Override
	void fillInTexts() {
		views[0].setText(substitution.getPeriodNumber() + ". Stunde");
		views[1].setText("Aufgabe stellen");
		views[2].setText("für die");
		String className = substitution.getClassName();
		String instdSubject = substitution.getInstdSubject().getName();
		views[3].setText(className + " (eig. " + instdSubject + ")");
		views[4].setText(substitution.getKind());
		views[5].setText(substitution.getSubstTeacher().getNameWithCompellation());
		
		if (displayText()) {
			views[6].setText("Info");
			views[7].setText(substitution.getText());
		} else {
			views[6].setText("");
			views[7].setText("");
		}
		views[8].setText("");
	}
	
}
