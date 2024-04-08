package com.qs.minigridv5.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.AProcessCarousel;
import com.qs.minigridv5.entities.MovieTemplate;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.misc.C;

import java.util.ArrayList;

public class FTemplateList extends MyFragment {

    private RecyclerView customerMoviesList;

    ArrayList<Integer> thumbnailImages = new ArrayList<>();

    public FTemplateList() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        thumbnailImages.clear();
        thumbnailImages.add(R.drawable.theme_1_img);
        thumbnailImages.add(R.drawable.theme_2_img);
        thumbnailImages.add(R.drawable.theme_3_img);

    }

    @Override
    public void onResume() {
        super.onResume();
        parentActivity.setToolbarText(R.string.title_new_movie);
        parentActivity.navigationView.getMenu().getItem(C.NAV_MAKE_IDX).setChecked(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.f_template_list, container, false);

        customerMoviesList = view.findViewById(R.id.f_customer_template_list);
        customerMoviesList.setLayoutManager(new LinearLayoutManager(getContext()));

        customerMoviesList.setAdapter(new MyAdapter());

        return view;
    }

    @Override
    public String getName() {
        return "FTemplateList";
    }


    public class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.x_template_list_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {


            if (position < thumbnailImages.size()) {
                holder.thumbnail.setImageResource(thumbnailImages.get(position));
            }

            holder.textView.setText(myApp.getMovieTemplates().get(position).title);
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final MovieTemplate movieTemplate = myApp.getMovieTemplates().get(position).getCopy();

                    final Project project = Project.createNewProject(movieTemplate);

                    final Intent intent = new Intent(getContext(), AProcessCarousel.class);
                    intent.putExtra(AProcessCarousel.KEY_PROJECT_ID, project.ID);
                    startActivity(intent);

                }
            });


        }

        @Override
        public int getItemCount() {

            return myApp.getMovieTemplates().size();
        }


    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail;
        public TextView  textView;
        public ImageView startBtn;
        public FrameLayout container;

        public ViewHolder(View v) {
            super(v);
            thumbnail = v.findViewById(R.id.x_movie_thumbnail);
            textView = v.findViewById(R.id.x_movie_title);
            startBtn = v.findViewById(R.id.x_movie_start);
            container = v.findViewById(R.id.x_movie_item_container);
        }
    }

}
