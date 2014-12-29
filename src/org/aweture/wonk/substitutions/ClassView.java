package org.aweture.wonk.substitutions;

import java.util.List;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Class;
import org.aweture.wonk.models.Substitution;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ClassView extends LinearLayout {
	
	private TextView classNameTextView;
	private SubstitutionsView substitutionsView;

	public ClassView(Context context) {
		this(context, null);
	}
	public ClassView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public ClassView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}
	public ClassView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		classNameTextView = (TextView) findViewById(R.id.className);
		substitutionsView = (SubstitutionsView) findViewById(R.id.substitutionsList);
	}
	
	public void setSubstitutions(Class currentClass, List<Substitution> substitutions) {
		String className = currentClass.getName();
		classNameTextView.setText(className);
		
		substitutionsView.displaySubstitutions(substitutions);
	}

}
