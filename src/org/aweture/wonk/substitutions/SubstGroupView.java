package org.aweture.wonk.substitutions;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.models.SubstitutionsGroup;
import org.aweture.wonk.storage.SimpleData;
import org.aweture.wonk.substitutions.SubstitutionPresentation.PresentationFor;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SubstGroupView extends LinearLayout implements Comparator<Substitution> {

	private static final Queue<SubstitutionView> itemOverflow = new LinkedList<SubstitutionView>();
	
	private Queue<SubstitutionView> items;
	private TextView classNameTextView;
	
	private SubstitutionsGroup currentGroup;
	private List<Substitution> substitutions;
	

	public SubstGroupView(Context context) {
		super(context);
		init(context);
	}
	public SubstGroupView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public SubstGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SubstGroupView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}
	
	private void init(Context context) {
		setOrientation(VERTICAL);
		LayoutInflater.from(context).inflate(R.layout.view_class, this, true);

		items = new LinkedList<SubstitutionView>();
		
		classNameTextView = (TextView) findViewById(R.id.className);
	}
	
	public void setSubstitutions(SubstitutionsGroup currentGroup, List<Substitution> substitutions) {
		this.currentGroup = currentGroup;
		this.substitutions = substitutions;
		Collections.sort(substitutions, this);

		showClassName();
		recycleAllItems();
		applySubstitutionsToItems();
		showItems();
	}
	
	private void showClassName() {
		String className = currentGroup.getName();
		classNameTextView.setText(className);
	}
	
	private void recycleAllItems() {
		SubstitutionView item;
		while ((item = items.poll()) != null) {
			removeView(item);
			itemOverflow.add(item);
		}
	}
	
	private void applySubstitutionsToItems() {
		SimpleData simpleData = new SimpleData(getContext());
		boolean student = simpleData.isStudent();
		
		for (Substitution substitution : substitutions) {
			SubstitutionView nextItem = getUndisplayedSubstitutionView();
			if (student) {
				nextItem.setSubstitution(substitution, PresentationFor.Student);
			} else {
				if (currentGroup.isBasedUppon(substitution.getTaskProvider())) {
					nextItem.setSubstitution(substitution, PresentationFor.TaskProvider);
				} else {
					nextItem.setSubstitution(substitution, PresentationFor.Substitute);
				}
			}
			items.add(nextItem);
		}
	}
	
	private SubstitutionView getUndisplayedSubstitutionView() {
		SubstitutionView view = itemOverflow.poll();
		if (view == null) {
			view = new SubstitutionView(getContext());
		}
		return view;
	}
	
	private void showItems() {
		for (SubstitutionView view : items) {
			addView(view);
		}
	}
	
	@Override
	public int compare(Substitution lhs, Substitution rhs) {
		return lhs.getPeriodNumber() - rhs.getPeriodNumber();
	}
}
