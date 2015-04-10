package org.aweture.wonk.substitutions;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.aweture.wonk.R;
import org.aweture.wonk.models.Class;
import org.aweture.wonk.models.Substitution;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ClassView extends LinearLayout implements Comparator<Substitution> {

	private static final Queue<SubstitutionView> itemOverflow = new LinkedList<SubstitutionView>();
	
	private Queue<SubstitutionView> items;
	private TextView classNameTextView;
	
	private Class currentClass;
	private List<Substitution> substitutions;
	

	public ClassView(Context context) {
		super(context);
		init(context);
	}
	public ClassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public ClassView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ClassView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}
	
	private void init(Context context) {
		setOrientation(VERTICAL);
		LayoutInflater.from(context).inflate(R.layout.view_class, this, true);

		items = new LinkedList<SubstitutionView>();
		
		classNameTextView = (TextView) findViewById(R.id.className);
	}
	
	public void setSubstitutions(Class currentClass, List<Substitution> substitutions) {
		this.currentClass = currentClass;
		this.substitutions = substitutions;
		Collections.sort(substitutions, this);

		showClassName();
		recycleAllItems();
		applySubstitutionsToItems();
		showItems();
	}
	
	private void showClassName() {
		String className = currentClass.getName();
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
		for (Substitution substitution : substitutions) {
			SubstitutionView nextItem = getUndisplayedSubstitutionView();
			nextItem.setSubstitution(substitution);
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
