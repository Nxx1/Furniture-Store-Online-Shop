package com.potensiutama.kusenstoreclient.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.potensiutama.kusenstoreclient.R;

import java.util.List;
import java.util.Objects;

public class ViewPagerFotoProdukAdapter extends PagerAdapter {

    // Context object
    Context context;

    // Array of images
    List<String> images;

    // Layout Inflater
    LayoutInflater mLayoutInflater;


    // Viewpager Constructor
    public ViewPagerFotoProdukAdapter(Context context, List<String> images) {
        this.context = context;
        this.images = images;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // return the number of images
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((ConstraintLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        // inflating the item.xml
        View itemView = mLayoutInflater.inflate(R.layout.collapse_foto_produk, container, false);

        // referencing the image view from the item.xml file
        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageview_informasi_foto_produk);

        // setting the image in the imageView
       // imageView.setImageResource(images[position]);

        Glide.with(context).load(images.get(position)).into(imageView);

        // Adding the View
        Objects.requireNonNull(container).addView(itemView);

        TextView txtNomorFoto = itemView.findViewById(R.id.txt_informasi_jumlah_foto);

        txtNomorFoto.setText((position+1)+"/"+String.valueOf(getCount()));


        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((ConstraintLayout) object);
    }
}