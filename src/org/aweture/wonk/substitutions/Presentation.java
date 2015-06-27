package org.aweture.wonk.substitutions;

import org.aweture.wonk.models.Substitution;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;


public abstract class Presentation {
	
	public static final int CHILD_COUNT = 9;
	
	public enum PresentationMode {
		StudentMode, SubstituteMode, TaskProviderMode;
	}
	
	private Substitution substitution;
	
	public static Presentation applyPresentation(Substitution substitution, TextView[] views, PresentationMode presentationMode) {
		Presentation presentation = getPresentation(presentationMode);
		presentation.setTextViews(views);
		presentation.applyPresenation(substitution);
		return presentation;
	}
	
	public static TextView[] createNewTextViews(Context context, float textSize) {
		TextView[] views = new TextView[CHILD_COUNT];
		for (int i = 0; i < CHILD_COUNT; i++) {
			TextView view = new TextView(context);
			view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			views[i] = view;
		}
		return views;
	}
	
	private static Presentation getPresentation(PresentationMode mode) {
		switch (mode) {
		case StudentMode:
			return new StudentPresentation();
		case SubstituteMode:
			return new TeacherPresentation();
		case TaskProviderMode:
			return new TaskProviderPresentation();
		default:
			throw new RuntimeException("Mode \"" + mode + "\" not known.");
		}
	}
	
	public void applyPresenation(Substitution substitution) {
		this.substitution = substitution;
		setSubstitution(substitution);
		fillInTexts();
	}

	abstract void setSubstitution(Substitution subst);
	abstract void setTextViews(TextView[] views);
	abstract void fillInTexts();
	abstract boolean hasRightViewInCollapsedState();
	abstract View getCollapsedStateChild(int position);
	abstract int expandedStateChildCount();
	abstract View getExpandedStateChild(int position);
	abstract void setCollapsedStateVisibilities();
	abstract void setExpandedStateVisibilities();
	
	boolean displayText() {
		return substitution.hasText();
	}
	
}
