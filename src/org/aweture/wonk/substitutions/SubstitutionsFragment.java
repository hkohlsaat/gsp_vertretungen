package org.aweture.wonk.substitutions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Class;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.SubstitutionsStore;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SubstitutionsFragment extends Fragment {
	
	private static final String PLAN_INDEX_ARGUMENT = "plan_index";
	
	private Plan plan;
	private List<Class> sortedClasses;
	
	private RecyclerView recyclerView;
	private RecyclerView.Adapter<Adapter.ViewHolder> adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new Adapter();
		plan = new Plan();
		sortedClasses = new ArrayList<Class>();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupData(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_recycler_view, container, false);
		
		// Get the RecyclerView
		recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
		
		// Set up the RecyclerView
		Context context = inflater.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

		return view;
	}
	
	public void setPlanIndex(int planIndex) {
		Bundle argumentBundle = new Bundle();
		argumentBundle.putInt(PLAN_INDEX_ARGUMENT, planIndex);
		
		setArguments(argumentBundle);
	}
	
	public void setupData(Context context) {
		// Get the arguments and obtain the planIndex.
		Bundle argumentBundle = getArguments();
		int planIndex = argumentBundle.getInt(PLAN_INDEX_ARGUMENT);
		
		// Access the SubstitutionsStore and store the plan.
		SubstitutionsStore dataStore = SubstitutionsStore.getInstance(context);
		plan = dataStore.getCurrentPlans()[planIndex];
		
		// Obtain a list with all classes sorted.
		sortedClasses.clear();
		sortedClasses.addAll(plan.keySet());
		Collections.sort(sortedClasses, new ClassComparator());
	}
	
	private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
		
		@Override
		public int getItemCount() {
			return plan.size();
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	        ClassView view = new ClassView(getActivity());
	        ViewHolder vh = new ViewHolder(view);
	        return vh;

		}

		@Override
		public void onBindViewHolder(ViewHolder vh, int position) {
			Class currentClass = sortedClasses.get(position);
			vh.classView.setSubstitutions(currentClass, plan.get(currentClass));
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder{
			
			public ClassView classView;

			public ViewHolder(ClassView itemView) {
				super(itemView);
				classView = itemView;
			}
		}
	}
	
	private class ClassComparator implements Comparator<Class> {

		@Override
		public int compare(Class lhs, Class rhs) {
			int difference = lhs.getGrade() - rhs.getGrade();
			if (difference > 0) {
				return 1;
			} else if (difference < 0) {
				return -1;
			} else {
				return lhs.getName().compareTo(rhs.getName());
			}
		}
	}
}