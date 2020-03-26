package com.project.carpool_ride_share_app.ui;


import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.project.carpool_ride_share_app.R;
import com.project.carpool_ride_share_app.adapters.ImageListRecyclerAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * The list of images shown when selecting an avatar in profile.
 *
 */
public class ImageListFragment extends Fragment implements ImageListRecyclerAdapter.ImageListRecyclerClickListener{

    private static final String TAG = "ImageListFragment";
    private static final int NUM_COLUMNS = 2;

    //widgets
    private RecyclerView mRecyclerView;


    //vars
    private ArrayList<Integer> mImageResources = new ArrayList<>();
    private IProfile mIProfile;

    public ImageListFragment() {
        // Required empty public constructor
    }


    public static ImageListFragment newInstance() {
        return new ImageListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_list, container, false);
        mRecyclerView = view.findViewById(R.id.image_list_recyclerview);

        getImageResouces();
        initRecyclerview();

        return view;
    }

    /**
     *  TODO: Add more images to be used as avatars ...
     */


    private void getImageResouces(){
        mImageResources.add(R.drawable.stock_bg_login);
        mImageResources.add(R.drawable.yeet);
        mImageResources.add(R.drawable.yaaah);
        mImageResources.add(R.drawable.uhhm);
        mImageResources.add(R.drawable.another);
        mImageResources.add(R.drawable.horse);
    }

    private void initRecyclerview(){
        ImageListRecyclerAdapter mAdapter = new ImageListRecyclerAdapter(getActivity(), mImageResources, this);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIProfile = (IProfile) getActivity();
    }

    @Override
    public void onImageSelected(int position) {
        mIProfile.onImageSelected(mImageResources.get(position));
    }
}








