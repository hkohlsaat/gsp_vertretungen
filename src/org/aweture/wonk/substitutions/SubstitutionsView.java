package org.aweture.wonk.substitutions;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Substitution;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SubstitutionsView extends LinearLayout {
	
	private Context context;
	
	public SubstitutionsView(Context context) {
		this(context, null);
	}
	public SubstitutionsView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public SubstitutionsView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}
	public SubstitutionsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.context = context;
	}
	
	public void displaySubstitutions(List<Substitution> substitutions) {
		removeAllViews();
		Collections.sort(substitutions, new SubstitutionComparator());
		for (Substitution substitution : substitutions) {
			LayoutInflater layoutInflater = LayoutInflater.from(context);
			View view = layoutInflater.inflate(R.layout.item_substitution, this, true);
			TextView periodTextView = (TextView) view.findViewById(R.id.periodNumber);
			TextView substitutionView = (TextView) view.findViewById(R.id.substitutionSpecification);
			String period = substitution.getPeriodNumber() + ". Stunde";
			String info = substitution.getKind() + " " + substitution.getSubstTeacher();
			periodTextView.setText(period);
			substitutionView.setText(info);
		}
	}
	
	private class SubstitutionComparator implements Comparator<Substitution> {

		@Override
		public int compare(Substitution lhs, Substitution rhs) {
			int difference = lhs.getPeriodNumber() - rhs.getPeriodNumber();
			if (difference > 0) {
				return 1;
			} else if (difference < 0) {
				return -1;
			} else {
				return 0;
			}
		}
		
	}
}
