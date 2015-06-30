package org.aweture.wonk.substitutions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.models.SubstitutionsGroup;
import org.aweture.wonk.storage.PlansLoader;
import org.aweture.wonk.substitutions.ExpandableLayoutManager.ExpandableViewHolder;
import org.aweture.wonk.substitutions.SubstitutionsFragment.Adapter.ViewHolder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;

public class SubstitutionsFragment extends Fragment {
	
	private static final String PLAN_INDEX_ARGUMENT = "plan_index";
	
	private RecyclerView recyclerView;
	private ExpandableLayoutManager expandableLayoutManager = new ExpandableLayoutManager();
	private Adapter adapter = new Adapter();
	private View noDataView;
	
	private int planIndex;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new Adapter();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Get the arguments and obtain the planIndex.
		Bundle argumentBundle = getArguments();
		planIndex = argumentBundle.getInt(PLAN_INDEX_ARGUMENT);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_recycler_view, container, false);

		// Set up the RecyclerView
		recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(expandableLayoutManager);
		recyclerView.setAdapter(adapter);

		noDataView = view.findViewById(R.id.noSubstitutions);
		
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		LoaderManager manager = getLoaderManager();
		manager.initLoader(R.id.substitutions_Fragement_PlansLoader, null, adapter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		LoaderManager manager = getLoaderManager();
		manager.destroyLoader(R.id.substitutions_Fragement_PlansLoader);
	}
	
	public void setPlanIndex(int planIndex) {
		Bundle argumentBundle = new Bundle();
		argumentBundle.putInt(PLAN_INDEX_ARGUMENT, planIndex);
		
		setArguments(argumentBundle);
	}
	
	class Adapter extends RecyclerView.Adapter<ViewHolder> implements LoaderCallbacks<List<Plan>> {
		
		private List<SubstitutionsGroup> sortedSubstitutionGroups;
		private Plan plan;
		
		private ViewHolder expanded = null;
		
		public Adapter() {
			sortedSubstitutionGroups = new ArrayList<SubstitutionsGroup>();
		}
		
		@Override
		public int getItemCount() {
			return sortedSubstitutionGroups.size();
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
		
		public class ViewHolder extends ExpandableViewHolder implements OnClickListener {
			
			private SubstGroupView classView;
			private SubstitutionView substitutionView;

			public ViewHolder(SubstGroupView itemView) {
				expandableLayoutManager.super(itemView);
				classView = itemView;
				classView.setOnSubstitutionsClickListener(this);
			}
			
			@Override
			public void onClick(View v) {
				substitutionView = (SubstitutionView) v;
				substitutionView.changeExpansionState(!substitutionView.isExpanded());
				animateItemSizeChange();
			}

			@Override
			public RecyclerView getRecyclerView() {
				return recyclerView;
			}

			@Override
			public int getHeight() {
				return substitutionView.getHeight();
			}

			@Override
			public void setHeight(int height) {
				LayoutParams lp = substitutionView.getLayoutParams();
				lp.height = height;
				classView.requestLayout();
			}

			@Override
			public int getTop() {
				return classView.getTop() + substitutionView.getTop();
			}

			@Override
			public int getBottom() {
				return classView.getTop() + substitutionView.getBottom();
			}
			
			@Override
			public void startAnimation(Animation animation) {
				classView.clearAnimation();
				classView.startAnimation(animation);
				substitutionView.onPreDraw();
			}
		}

		@Override
		public Loader<List<Plan>> onCreateLoader(int id, Bundle args) {
			return new PlansLoader(getActivity());
		}

		@Override
		public void onLoadFinished(Loader<List<Plan>> loader, List<Plan> planList) {
			plan = planList.get(planIndex);
			
			sortedSubstitutionGroups.clear();
			sortedSubstitutionGroups.addAll(plan.keySet());
			Collections.sort(sortedSubstitutionGroups);
			
			notifyDataSetChanged();
			
			if (plan.isEmpty()) {
				noDataView.setVisibility(View.VISIBLE);
			} else {
				noDataView.setVisibility(View.GONE);
			}
		}

		@Override
		public void onLoaderReset(Loader<List<Plan>> arg0) {
		}
	}
}