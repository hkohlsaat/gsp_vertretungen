package org.aweture.wonk.substitutions;

import org.aweture.wonk.models.Substitution;

import android.view.View;
import android.widget.TextView;

public class SubstitutionPresentation {
	
	public enum PresentationFor {
		Student, Substitute, TaskProvider;
	}
	
	private final TextView[] textViews = new TextView[9];
	private final PresentationFor repMode;
	private final Substitution substitution;
	
	public SubstitutionPresentation(AbstractSubstitutionView view, Substitution substitution, PresentationFor rep) {
		for (int i = 0; i < 9; i++) {
			textViews[i] = (TextView) view.getChildAt(i);
		}
		repMode = rep;
		this.substitution = substitution;
		fillInTexts();
	}
	
	private void fillInTexts() {
		switch (repMode) {
		case Student:
			fillInStudentModeTexts();
			break;
		case Substitute:
			fillInSubstituteModeTexts();
			break;
		case TaskProvider:
			fillInTaskProviderModeTexts();
			break;
		default:
			throw new RuntimeException("No presentation mode matched. Must be some mode!");
		}
	}

	private void fillInStudentModeTexts() {
		textViews[0].setText(substitution.getPeriodNumber() + ". Stunde");
		textViews[1].setText(substitution.getInstdSubject().getName());
		textViews[2].setText("statt");
		textViews[3].setText(substitution.getInstdTeacher().getAccusative());
		textViews[4].setText(substitution.getKind());
		textViews[5].setText(substitution.getSubstTeacher().getNameWithCompellation());
		textViews[6].setText("Info");
		textViews[7].setText(substitution.getText());
		
		if (substitution.getInstdSubject().isConcurrentlyTaught()) {
			String courseName = substitution.getInstdSubject().getAbbreviation()
					+ " " + substitution.getInstdTeacher().getShortName();
			textViews[8].setText(courseName);
		} else {
			textViews[8].setText("");
		}
	}
	private void fillInSubstituteModeTexts() {
		textViews[0].setText(substitution.getPeriodNumber() + ". Stunde");
		textViews[1].setText(substitution.getKind());
		textViews[2].setText("in der");
		textViews[3].setText(substitution.getClassName());
		textViews[4].setText("statt");
		textViews[5].setText(substitution.getInstdTeacher().getAccusative()
				+ " in " + substitution.getInstdSubject().getName());
		textViews[6].setText("Info");
		textViews[7].setText(substitution.getText());
		textViews[8].setText("");
	}
	private void fillInTaskProviderModeTexts() {
		textViews[0].setText(substitution.getPeriodNumber() + ". Stunde");
		textViews[1].setText("Aufgabe stellen");
		textViews[2].setText("für die");
		textViews[3].setText(substitution.getClassName() + " (eig. "
				+ substitution.getInstdSubject().getName() + ")");
		textViews[4].setText(substitution.getKind());
		textViews[5].setText(substitution.getSubstTeacher().getNameWithCompellation());
		textViews[6].setText("Info");
		textViews[7].setText(substitution.getText());
		textViews[8].setText("");
	}

	public TextView getCollapsedLeft() {
		return textViews[0];
	}

	public TextView getCollapsedMiddle() {
		if (repMode == PresentationFor.Student) {
			return textViews[4];
		} else {
			return textViews[1];
		}
	}

	public TextView getCollapsedRight() {
		return textViews[8];
	}

	public TextView getExpanded(int position) {
		return textViews[position];
	}
	
	public void setCollapsedVisibilities() {
		for (TextView tv : textViews) {
			tv.setVisibility(View.GONE);
		}
		getCollapsedLeft().setVisibility(View.VISIBLE);
		getCollapsedMiddle().setVisibility(View.VISIBLE);
		getCollapsedRight().setVisibility(View.VISIBLE);
	}
	
	public void setExpandedVisibilities() {
		for (TextView tv : textViews) {
			tv.setVisibility(View.VISIBLE);
		}
		getCollapsedRight().setVisibility(View.GONE);
	}

}
