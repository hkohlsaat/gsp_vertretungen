package org.aweture.wonk.substitutions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Class;
import org.aweture.wonk.models.Plan;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SubstitutionsFragment extends Fragment {
	
	private Plan plan;
	private List<Class> sortedKeys;
	
	private RecyclerView recyclerView;
	private RecyclerView.Adapter<Adapter.ViewHolder> adapter;
	private RecyclerView.LayoutManager layoutManager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_stubstitutions, container, false);
		
		recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(inflater.getContext());
        recyclerView.setLayoutManager(layoutManager);
        
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);

		return view;
	}


	public void setPlan(Plan plan) {
		Set<Class> classesSet = plan.keySet();
		List<Class> classes = new ArrayList<Class>(classesSet);
		Collections.sort(classes, new ClassComparator());
		sortedKeys = classes;
		this.plan = plan;
		
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	
	public Plan getPlan() {
		return plan;
	}


	private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
		
		@Override
		public int getItemCount() {
			Set<Class> keys = plan.keySet();
			return keys.size();
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			 // create a new view
	        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
	        ClassView view = (ClassView) layoutInflater.inflate(R.layout.card_substitutions, parent, false);
	        
	        ViewHolder vh = new ViewHolder(view);
	        return vh;

		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, int position) {
			Class key = sortedKeys.get(position);
			viewHolder.classView.setSubstitutions(key, plan.get(key));
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
