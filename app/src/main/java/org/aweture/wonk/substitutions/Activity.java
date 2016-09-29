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
import android.support.v4.content.ContextCompat;
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
import org.aweture.wonk.models.Teacher;
import org.aweture.wonk.storage.SimpleData;

import java.util.ArrayList;
import java.util.List;

public class Activity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Plan> {

    private View feedbackContainer;
    private View noDataText;
    private View progessBar;

    private PartAdapter adapter;
    private List<RecyclerView> recyclerViews = new ArrayList<RecyclerView>();
    private Plan plan;

    private PlanUpdateReceiver receiver;

    private boolean isStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This Activity is in every case the landing activity. But that doesn't mean that the
        // users should be shown this Activity. Maybe they should be redirected to the
        // landing Activity.
        if (shouldDisplayLanding()) {
            displayLandingActivity();
            return;
        }

        // Set content view and toolbar
        setContentView(R.layout.activity_substitutions);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // Set up ViewPager with an adapter and tabs.
        adapter = new PartAdapter();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        // Set up the member variables.
        feedbackContainer = findViewById(R.id.feedbackContainer);
        noDataText = findViewById(R.id.noDataText);
        progessBar = findViewById(R.id.progressBar);

        // The ProgressBar might have a color that makes it hard to distinguish.
        // So it is set to the primary color to make sure everything is laid out properly.
        int primaryColor = ContextCompat.getColor(this, R.color.primary);
        ((ProgressBar) progessBar).getIndeterminateDrawable().setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);

        // Init the LoaderManager to load the content.
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(R.id.SUBSTITUTIONS_ACTIVTY_PLAN_LOADER, null, this);
    }

    /**
     * shouldDisplayLanding() determines whether the users should be shown the landing Activity
     * before interacting with this Activity.
     * @return true in the case that the user should be redirected
     */
    private boolean shouldDisplayLanding() {
        SimpleData data = new SimpleData(this);
        return !data.isPasswordEntered();
    }

    /**
     * displayLandingActivity() redirects the user to the landing Activity and finishes
     * this Activity.
     */
    private void displayLandingActivity() {
        Intent intent = new Intent(this, org.aweture.wonk.landing.Activity.class);
        startActivity(intent);
        finish();
    }

    /**
     * showLoadingLayout() displays a progressbar and is intended to be used in loading
     * situations, as the name suggests.
     */
    private void showLoadingLayout() {
        feedbackContainer.setVisibility(View.VISIBLE);
        noDataText.setVisibility(View.GONE);
        progessBar.setVisibility(View.VISIBLE);
    }

    /**
     * showNoDataLayout() shows a text which acknowleges that the Activity has no data to show.
     */
    private void showNoDataLayout() {
        feedbackContainer.setVisibility(View.VISIBLE);
        noDataText.setVisibility(View.VISIBLE);
        progessBar.setVisibility(View.GONE);
    }

    /**
     * showNormalLayout() reverses the changes made by {@link #showLoadingLayout()}
     * and {@link #showNoDataLayout()}.
     */
    private void showNormalLayout() {
        feedbackContainer.setVisibility(View.GONE);
        noDataText.setVisibility(View.GONE);
        progessBar.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu according to IN_DEBUG_MODE flag.
        MenuInflater inflater = getMenuInflater();
        int menuId = Application.IN_DEBUG_MODE ? R.menu.substitutes_debug_mode : R.menu.substitutes;
        inflater.inflate(menuId, menu);
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

        showLoadingLayout();

        Intent intent = new Intent(this, PlanUpdateService.class);
        startService(intent);

        receiver = new PlanUpdateReceiver(new PlanUpdateReceiver.Handler() {
            @Override
            public void handleEvent(final Intent intent) {
                LogUtil.d("Processing intent form PlanUpdateService.");

                if (intent.getBooleanExtra(PlanUpdateService.EXTRA_FINISHED_SUCCESSFULLY, true)) {
                    // The update was successfull.
                    LogUtil.d("PlanUpdateService finished successfully. Loader will update UI.");
                } else {
                    // The update failed.
                    LogUtil.d("PlanUpdateService finished with exception. Showing pre update UI.");
                    showNormalLayout();
                    Toast.makeText(Activity.this, "Fehler beim Herunterladen", Toast.LENGTH_SHORT).show();
                }

                // Unregister the receiver when this broadcast is handled.
                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(Activity.this);
                broadcastManager.unregisterReceiver(receiver);
                receiver = null;
                LogUtil.d("Unregistered PlanUpdateReceiver.");
            }

        });

        // Register the receiver.
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(receiver, receiver.getIntentFilter());
    }

    @Override
    public Loader<Plan> onCreateLoader(int id, Bundle args) {
        return new PlanLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Plan> loader, Plan data) {
        // Set the new display mode.
        SimpleData sd = new SimpleData(this);
        isStudent = sd.isStudent();

        // Set the now
        plan = data;
        adapter.notifyDataSetChanged();

        if (plan.parts.length == 0) {
            // No plan or exception while reading.
            showNoDataLayout();
        } else {
            // There is a new plan. Update UI.
            showNormalLayout();

            // Inform all RecyclerViews about the new data.
            for (RecyclerView r : recyclerViews) {
                r.getAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Plan> loader) {
    }

    private class PartAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            if (plan == null)
                return 0;
            return plan.parts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            // Create the item.
            View v = getLayoutInflater().inflate(R.layout.view_part, collection, false);
            RecyclerView rv = (RecyclerView) v.findViewById(R.id.recyclerView);
            recyclerViews.add(rv);
            rv.setLayoutManager(new LinearLayoutManager(Activity.this));
            rv.addItemDecoration(new DividerItemDecoration());

            // Create an Adapter.
            rv.setAdapter(new SubstitutionsAdapter(position));

            // Add the item to the ViewPager.
            collection.addView(v);

            if (plan.parts[position].substitutions.length == 0) {
                rv.setVisibility(View.GONE);
            }

            return v;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            ((ViewPager) collection).removeView((View) view);
            RecyclerView rv = (RecyclerView) ((View) view).findViewById(R.id.recyclerView);
            recyclerViews.remove(rv);
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
            if (isStudent) {
                holder.view.setText(textForStudent(vi, s));
            } else {
                holder.view.setText(textForTeacher(vi, s));
            }
        }

        private String textForStudent(ViewInfo vi, Substitution s) {
            if (vi.type == 0) {
                return s.className;
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
                return sb.toString();
            }
        }

        private String textForTeacher(ViewInfo vi, Substitution s) {
            if (vi.type == 0) {
                return s.modeTaskProvider ? s.taskProvider.getName() : s.substTeacher.getName();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(s.period);
                sb.append(". Stunde: ");
                if (s.modeTaskProvider) {
                    sb.append("Aufgabe stellen");
                } else if (s.substTeacher.abbr.equals(s.taskProvider.abbr)) {
                    sb.append(s.kind);
                    sb.append("/Aufgabe stellen");
                } else {
                    sb.append(s.kind);
                }
                sb.append("\n");
                sb.append(s.className);
                sb.append(", statt ");
                sb.append(s.instdSubject.name);
                sb.append(" (");
                sb.append(s.instdTeacher.abbr);
                sb.append(")");
                if (s.text.length() > 0) {
                    sb.append("\n");
                    sb.append(s.text);
                }
                return sb.toString();
            }
        }

        @Override
        public int getItemCount() {
            ArrayList<ViewInfo> viewInfos = new ArrayList<ViewInfo>(plan.parts[part].substitutions.length);
            for (int i = 0; i < plan.parts[part].substitutions.length; i++) {
                if (i == 0) {
                    // The first shown item is always a header.
                    // Make a header pointing to 0.
                    viewInfos.add(new ViewInfo(0, 0));
                    // Make a subtitution item showing the content of 0.
                    viewInfos.add(new ViewInfo(1, 0));
                } else if (isStudent) {
                    if (plan.parts[part].substitutions[i].className.equalsIgnoreCase(plan.parts[part].substitutions[i - 1].className)) {
                        // If this class name equals the previous one, the next item shows the content of i.
                        viewInfos.add(new ViewInfo(1, i));
                    } else {
                        // If this class name does not equal the previous one, the next item is a header.
                        viewInfos.add(new ViewInfo(0, i));
                        // Then the actual i'th content is shown.
                        viewInfos.add(new ViewInfo(1, i));
                    }
                } else {
                    // Get this teacher and the previous one.
                    Substitution s = plan.parts[part].substitutions[i];
                    Teacher thisTeacher = s.modeTaskProvider ? s.taskProvider : s.substTeacher;
                    s = plan.parts[part].substitutions[i - 1];
                    Teacher prevTeacher = s.modeTaskProvider ? s.taskProvider : s.substTeacher;

                    if (thisTeacher.abbr.equalsIgnoreCase(prevTeacher.abbr)) {
                        // If the teacher names are equal, the next item shows the content of i.
                        viewInfos.add(new ViewInfo(1, i));
                    } else {
                        // If the teacher names are not equal, the next item is a header of i.
                        viewInfos.add(new ViewInfo(0, i));
                        // Then the actual i'th content is shown.
                        viewInfos.add(new ViewInfo(1, i));
                    }
                }
            }
            this.viewInfos = viewInfos.toArray(new ViewInfo[viewInfos.size()]);
            return viewInfos.size();
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