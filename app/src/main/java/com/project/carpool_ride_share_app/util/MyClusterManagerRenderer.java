package com.project.carpool_ride_share_app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.project.carpool_ride_share_app.R;
import com.project.carpool_ride_share_app.models.MarkerCluster;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<MarkerCluster> {

    private final IconGenerator iconGenerator;
    private ImageView imageView;
    private final int markerWidth;
    private final int markerHeight;

    public MyClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<MarkerCluster> clusterManager) {
        super(context, map, clusterManager);


        iconGenerator = new IconGenerator(context.getApplicationContext());
        imageView = new ImageView(context.getApplicationContext());
        markerWidth = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        markerHeight = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        int padding = (int) context.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setContentView(imageView);
    }

    @Override
    protected void onBeforeClusterItemRendered(MarkerCluster item, MarkerOptions markerOptions) {
        imageView.setImageResource(item.getPicture());
        Bitmap icon  = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());
    }


    // We don't actually need the clustering.
    @Override
    protected boolean shouldRenderAsCluster(Cluster<MarkerCluster> cluster) {
        return false;
    }

    /**
     * Update the GPS coordinate of a ClusterItem
     * @param clusterMarker
     */
    public void setUpdateMarker(MarkerCluster clusterMarker) {
        Marker marker = getMarker(clusterMarker);
        if (marker != null) {
            marker.setPosition(clusterMarker.getPosition());
        }
    }
}
