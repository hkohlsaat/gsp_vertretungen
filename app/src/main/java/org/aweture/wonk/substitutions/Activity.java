package org.aweture.wonk.substitutions;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.aweture.wonk.Application;
import org.aweture.wonk.LicensesDialogFragment;
import org.aweture.wonk.R;
import org.aweture.wonk.background.PlanUpdateReceiver;
import org.aweture.wonk.background.PlanUpdateService;
import org.aweture.wonk.log.LogUtil;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.storage.SimpleData;

import java.util.ArrayList;

public class Activity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Plan> {

    private View feedbackContainer;
    private View noDataText;
    private View progessBar;

    private PartAdapter adapter;
    private Plan plan;

    private PlanUpdateReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (shouldDisplayLanding()) {
            Intent intent = new Intent(this, org.aweture.wonk.landing.Activity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_substitutions);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        adapter = new PartAdapter();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        feedbackContainer = findViewById(R.id.feedbackContainer);
        noDataText = findViewById(R.id.noDataText);
        progessBar = findViewById(R.id.progressBar);

        ((ProgressBar) progessBar).getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);

        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(R.id.SUBSTITUTIONS_ACTIVTY_PLAN_LOADER, null, this);
    }

    private boolean shouldDisplayLanding() {
        SimpleData data = new SimpleData(this);
        return !data.isPasswordEntered();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (Application.IN_DEBUG_MODE) {
            inflater.inflate(R.menu.substitutes_debug_mode, menu);
        } else {
            inflater.inflate(R.menu.substitutes, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update:
                performPlanUpdate();
                return true;
            case R.id.action_show_licenses:
                LicensesDialogFragment fragment = new LicensesDialogFragment();
                fragment.show(getFragmentManager(), "LicensesDialog");
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, org.aweture.wonk.settings.Activity.class));
                return true;
            default:
                return false;
        }
    }

    private void performPlanUpdate() {
        if (!Application.hasConnectivity(this)) {
            Toast.makeText(this, "Keine Internetverbindung", Toast.LENGTH_SHORT).show();
            return;
        } else if (receiver != null) {
            Toast.makeText(this, "Update läuft bereits.", Toast.LENGTH_SHORT).show();
            return;
        }

        feedbackContainer.setVisibility(View.VISIBLE);
        noDataText.setVisibility(View.GONE);
        progessBar.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, PlanUpdateService.class);
        startService(intent);

        receiver = new PlanUpdateReceiver(new PlanUpdateReceiver.Handler() {
            @Override
            public void handleEvent(final Intent intent) {
                LogUtil.d("Processing intent form PlanUpdateService.");
                if (!intent.getBooleanExtra(PlanUpdateService.EXTRA_FINISHED_SUCCESSFULLY, false)) {
                    LogUtil.d("PlanUpdateService finished with exception. Showing pre update UI.");
                    feedbackContainer.setVisibility(View.GONE);
                    noDataText.setVisibility(View.GONE);
                    progessBar.setVisibility(View.GONE);

                    Toast.makeText(Activity.this, "Fehler beim Herunterladen", Toast.LENGTH_SHORT).show();
                } else {
                    LogUtil.d("PlanUpdateService finished successfully. Loader will update UI.");
                }

                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(Activity.this);
                broadcastManager.unregisterReceiver(receiver);
                receiver = null;
                LogUtil.d("Unregister PlanUpdateReceiver.");
            }

        });

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(receiver, receiver.getIntentFilter());
    }

    @Override
    public Loader<Plan> onCreateLoader(int id, Bundle args) {
        return new PlanLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Plan> loader, Plan data) {
        LogUtil.currentMethod();
        plan = data;
        if (plan == null || plan.parts.length == 0) {
            // No plan or exception while reading.
            feedbackContainer.setVisibility(View.VISIBLE);
            noDataText.setVisibility(View.VISIBLE);
            progessBar.setVisibility(View.GONE);
        } else {
            // There is a new plan. Update UI.
            feedbackContainer.setVisibility(View.GONE);
            noDataText.setVisibility(View.GONE);
            progessBar.setVisibility(View.GONE);

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Plan> loader) {
        //onLoadFinished(loader, null);
    }

    private class PartAdapter extends PagerAdapter {

        private SubstitutionsAdapter[] adapters;

        public PartAdapter() {
            adapters = new SubstitutionsAdapter[2];
        }

        @Override
        public int getCount() {
            if (plan == null) {
                return 0;
            } else {
                return plan.parts.length;
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void notifyDataSetChanged() {
            // Adjust array size if necessary.
            if (getCount() > adapters.length) {
                SubstitutionsAdapter[] adapters = new SubstitutionsAdapter[getCount()];
                for (int i = 0; i < this.adapters.length; i++) {
                    adapters[i] = this.adapters[i];
                }
                this.adapters = adapters;
            }

            // Call super to inform super class.
            super.notifyDataSetChanged();

            // Inform the views.
            for (SubstitutionsAdapter a : adapters) {
                if (a != null) {
                    a.notifyDataSetChanged();
                }
            }
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            // Create the RecyclerView.
            RecyclerView rv = new RecyclerView(Activity.this);
            rv.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.MATCH_PARENT));
            rv.setLayoutManager(new LinearLayoutManager(Activity.this));
            rv.addItemDecoration(new DividerItemDecoration());

            // Create an Adapter.
            adapters[position] = new SubstitutionsAdapter(position);
            rv.setAdapter(adapters[position]);

            // Add the RecyclerView to the ViewPager.
            collection.addView(rv);

            return rv;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            ((ViewPager) collection).removeView((TextView) view);
            adapters[position] = null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Plan.Part part = plan.parts[position];
            String relativeWord = part.day.resolveToRelativeWord();
            if (relativeWord == null) {
                return part.day.toDateString();
            } else {
                return relativeWord;
            }
        }

        private class DividerItemDecoration extends RecyclerView.ItemDecoration {
            Paint paint = new Paint();
            int deviderHeight = 1;

            public DividerItemDecoration() {
                paint.setColor(Color.LTGRAY);
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                if (parent.getChildAdapterPosition(view) == 0) {
                    return;
                }

                outRect.top = deviderHeight;
            }

            @Override
            public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
                int dividerLeft = parent.getPaddingLeft();
                int dividerRight = parent.getWidth() - parent.getPaddingRight();

                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount - 1; i++) {
                    View child = parent.getChildAt(i);

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int dividerTop = child.getBottom() + params.bottomMargin;
                    int dividerBottom = dividerTop + deviderHeight;

                    canvas.drawRect(dividerLeft, dividerTop, dividerRight, dividerBottom, paint);
                }
            }
        }
    }



    private class SubstitutionsAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private int part;
        private ViewInfo[] viewInfos;

        public SubstitutionsAdapter(int part) {
            this.part = part;
        }

        @Override
        public int getItemViewType(int position) {
            return viewInfos[position].type;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                return new MyViewHolder((TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_class_headline, parent, false));
            } else {
                return new MyViewHolder((TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_substitution, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            ViewInfo vi = viewInfos[position];
            Substitution s = plan.parts[part].substitutions[vi.reference];
            if (vi.type == 0) {
                holder.view.setText(s.className);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(s.period);
                sb.append(". Stunde: ");
                if (s.instdSubject.name.length() > 0) {
                    sb.append(s.instdSubject.name);
                } else {
                    sb.append(s.instdSubject.abbr);
                }
                sb.append(" (");
                if (s.instdTeacher.name.length() > 0) {
                    sb.append(s.instdTeacher.name);
                } else {
                    sb.append(s.instdTeacher.abbr);
                }
                sb.append(")\n");
                sb.append(s.kind);
                if (s.kind.equals("Vertretung")) {
                    sb.append(": ");
                    if (s.substTeacher.name.length() > 0) {
                        sb.append(s.substTeacher.name);
                    } else {
                        sb.append(s.substTeacher.abbr);
                    }
                }
                if (s.text.length() > 0) {
                    sb.append("\n");
                    sb.append(s.text);
                }
                holder.view.setText(sb.toString());
            }
        }

        @Override
        public int getItemCount() {
            if (plan == null) {
                return 0;
            } else {
                ArrayList<ViewInfo> viewInfos = new ArrayList<ViewInfo>(plan.parts[part].substitutions.length);
                for (int i = 0; i < plan.parts[part].substitutions.length; i++) {
                    if (i == 0) {
                        viewInfos.add(new ViewInfo(0, 0));
                        viewInfos.add(new ViewInfo(1, 0));
                    } else if (plan.parts[part].substitutions[i].className.equals(plan.parts[part].substitutions[i - 1].className)) {
                        viewInfos.add(new ViewInfo(1, i));
                    } else {
                        viewInfos.add(new ViewInfo(0, i));
                        viewInfos.add(new ViewInfo(1, i));
                    }
                }
                this.viewInfos = viewInfos.toArray(new ViewInfo[viewInfos.size()]);
                return viewInfos.size();
            }
        }

        private class ViewInfo {
            public int type;
            public int reference;

            public ViewInfo(int type, int reference) {
                this.type = type;
                this.reference = reference;
            }
        }

    }


    private class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView view;

        public MyViewHolder(TextView v) {
            super(v);
            view = v;
        }

    }
}
