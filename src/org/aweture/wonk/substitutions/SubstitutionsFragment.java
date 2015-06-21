package org.aweture.wonk.substitutions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Class;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.models.SubstitutionsGroup;
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
		
		private List<SubstitutionsGroup> sortedSubstitutionGroups;
		private Plan plan;
		
		public Adapter() {
			sortedSubstitutionGroups = new ArrayList<SubstitutionsGroup>();
			plan = new Plan();
		}
		
		@Override
		public int getItemCount() {
			return plan.size();
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			SubstGroupView view = new SubstGroupView(getActivity());
			ViewHolder vh = new ViewHolder(view);
			return vh;
		}

		@Override
		public void onBindViewHolder(ViewHolder vh, int position) {
			SubstitutionsGroup currentGroup = sortedSubstitutionGroups.get(position);
			vh.classView.setSubstitutions(currentGroup, plan.get(currentGroup));
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder{
			
			public SubstGroupView classView;

			public ViewHolder(SubstGroupView itemView) {
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
			sortedSubstitutionGroups.clear();
			sortedSubstitutionGroups.addAll(plan.keySet());
			Collections.sort(sortedSubstitutionGroups);
			
			notifyDataSetChanged();
		}

		@Override
		public void onLoaderReset(Loader<List<Plan>> arg0) {
			plan = new Plan();
			sortedSubstitutionGroups.clear();
			notifyDataSetChanged();
		}
	}
}