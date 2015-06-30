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
import org.aweture.wonk.substitutions.Presentation.PresentationMode;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SubstGroupView extends LinearLayout implements Comparator<Substitution> {

	private static final Queue<AbstractSubstitutionView> itemOverflow = new LinkedList<AbstractSubstitutionView>();
	
	private static final int ITEM_GAP_DIP = 4;
	
	private int itemGap;
	
	private Queue<AbstractSubstitutionView> items;
	private TextView classNameTextView;
	
	private SubstitutionsGroup currentGroup;
	private List<Substitution> substitutions;
	
	private OnClickListener onSubstitutionsClickListener;
	

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
		
		Resources resources = getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		itemGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ITEM_GAP_DIP, dm);
		
		items = new LinkedList<AbstractSubstitutionView>();
		
		classNameTextView = (TextView) findViewById(R.id.className);
	}
	public void setOnSubstitutionsClickListener(OnClickListener onSubstitutionsClickListener) {
		this.onSubstitutionsClickListener = onSubstitutionsClickListener;
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
		AbstractSubstitutionView item;
		while ((item = items.poll()) != null) {
			item.setOnClickListener(null);
			removeView(item);
			itemOverflow.add(item);
		}
	}
	
	private void applySubstitutionsToItems() {
		SimpleData simpleData = new SimpleData(getContext());
		boolean student = simpleData.isStudent();
		
		for (Substitution substitution : substitutions) {
			AbstractSubstitutionView nextItem = getUndisplayedSubstitutionView();
			if (student) {
				nextItem.setSubstitution(substitution, PresentationMode.StudentMode);
			} else {
				if (currentGroup.isBasedUppon(substitution.getTaskProvider())) {
					nextItem.setSubstitution(substitution, PresentationMode.TaskProviderMode);
				} else {
					nextItem.setSubstitution(substitution, PresentationMode.SubstituteMode);
				}
			}
			items.add(nextItem);
		}
	}
	
	private AbstractSubstitutionView getUndisplayedSubstitutionView() {
		AbstractSubstitutionView view = itemOverflow.poll();
		if (view == null) {
			view = new SubstitutionView(getContext());
		}
		view.setOnClickListener(onSubstitutionsClickListener);
		return view;
	}
	
	private void showItems() {
		for (AbstractSubstitutionView view : items) {
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			lp.bottomMargin = itemGap;
			addView(view, lp);
		}
	}
	
	@Override
	public int compare(Substitution lhs, Substitution rhs) {
		return lhs.getPeriodNumber() - rhs.getPeriodNumber();
	}
}
