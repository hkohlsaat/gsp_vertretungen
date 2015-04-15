package org.aweture.wonk.substitutions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Class;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.PlansLoader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SubstitutionsFragment extends Fragment {
	
	private static final String PLAN_INDEX_ARGUMENT = "plan_index";
	
	private RecyclerView recyclerView;
	private Adapter adapter;
	
	private int planIndex;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new Adapter();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LoaderManager manager = getLoaderManager();
		manager.initLoader(R.id.substitutions_Fragement_PlansLoader, null, adapter);

		// Get the arguments and obtain the planIndex.
		Bundle argumentBundle = getArguments();
		planIndex = argumentBundle.getInt(PLAN_INDEX_ARGUMENT);
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		LoaderManager manager = getLoaderManager();
		manager.destroyLoader(R.id.substitutions_Fragement_PlansLoader);
	}
	
	public void setPlanIndex(int planIndex) {
		Bundle argumentBundle = new Bundle();
		argumentBundle.putInt(PLAN_INDEX_ARGUMENT, planIndex);
		
		setArguments(argumentBundle);
	}
	
	private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements LoaderCallbacks<List<Plan>> {
		
		private List<Class> sortedClasses;
		private Plan plan;
		
		public Adapter() {
			sortedClasses = new ArrayList<Class>();
			plan = new Plan();
		}
		
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

		@Override
		public Loader<List<Plan>> onCreateLoader(int id, Bundle args) {
			return new PlansLoader(getActivity());
		}

		@Override
		public void onLoadFinished(Loader<List<Plan>> loader, List<Plan> planList) {
			plan = planList.get(planIndex);
			
			// Obtain a list with all classes sorted.
			sortedClasses.clear();
			sortedClasses.addAll(plan.keySet());
			Collections.sort(sortedClasses, new ClassComparator());
			
			notifyDataSetChanged();
		}

		@Override
		public void onLoaderReset(Loader<List<Plan>> arg0) {
			plan = new Plan();
			sortedClasses.clear();
			notifyDataSetChanged();
		}
	}
	
	private class ClassComparator implements Comparator<Class> {

		@Override
		public int compare(Class lhs, Class rhs) {
			int difference;
			if (areBothOberstufe(lhs, rhs) && isOnlyOneALetterGrader(lhs, rhs)) {
				difference = lhs.isLetterGrader() ? -1 : 1;
			} else {
				difference = lhs.getGrade() - rhs.getGrade();
			}
			if (difference != 0) {
				return difference;
			} else {
				return lhs.getName().compareTo(rhs.getName());
			}
		}
		
		private boolean areBothOberstufe(Class x, Class y) {
			return x.isOberstufe() && y.isOberstufe();
		}
		
		private boolean isOnlyOneALetterGrader(Class x, Class y) {
			return x.isLetterGrader() ^ y.isLetterGrader();
		}
	}
}