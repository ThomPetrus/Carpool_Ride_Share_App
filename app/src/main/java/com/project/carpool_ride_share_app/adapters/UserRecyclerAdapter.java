package com.project.carpool_ride_share_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.carpool_ride_share_app.R;
import com.project.carpool_ride_share_app.models.User;

import java.util.ArrayList;

/**
 *  Credit goes to CodingWithMitch. The Chat portion of his open source tutorial was used as a basis for the
 *  project.
 */

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder>{

    private ArrayList<User> mUsers = new ArrayList<>();


    public UserRecyclerAdapter(ArrayList<User> users) {
        this.mUsers = users;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ((ViewHolder)holder).username.setText(mUsers.get(position).getUsername());

        // Changed it from email to just display the role of user / and snippet - should probably fix naming for rest.
        String text= mUsers.get(position).getRole();
        text += mUsers.get(position).getSnippet() != null ? " - "+ mUsers.get(position).getSnippet():"";
        ((ViewHolder)holder).email.setText(text);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView username, email;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.email);
        }


    }

}
















