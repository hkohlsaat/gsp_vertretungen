package org.aweture.wonk.substitutions;

import org.aweture.wonk.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SubstitutionsFragment extends Fragment {
	
	private RecyclerView recyclerView;
	private RecyclerView.Adapter adapter;
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
	
	
	private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
		
		@Override
		public int getItemCount() {
			return 0;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return null;
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, int position) {
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder{

			public ViewHolder(View itemView) {
				super(itemView);
			}
			
		}
	}
}
