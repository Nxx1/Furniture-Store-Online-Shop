package com.potensiutama.kusenstoreadmin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.potensiutama.kusenstoreadmin.EventBus.KategoriClick;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.calback.IRecyclerClickListener;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.KategoryModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyKategoriAdapter extends RecyclerView.Adapter<MyKategoriAdapter.MyViewHolder> {

    Context context;
    List<KategoryModel> kategoryModelList;

    public MyKategoriAdapter(Context context, List<KategoryModel> kategoryModelList) {
        this.context = context;
        this.kategoryModelList = kategoryModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_kategori_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(kategoryModelList.get(position).getImage())
                .into(holder.category_image);
        holder.category_name.setText(new StringBuilder(kategoryModelList.get(position).getName()));

        //Event Trigger
       /* holder.setListener((view, pos) -> {
            Common.categorySelected = kategoryModelList.get(pos);
            EventBus.getDefault().postSticky(new KategoriClick(true, kategoryModelList.get(pos)));
        });*/
    }

    @Override
    public int getItemCount() {
        return kategoryModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;
        @BindView(R.id.img_category)
        ImageView category_image;
        @BindView(R.id.txt_category)
        TextView category_name;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
           //listener.onItemClickListener(v,getAdapterPosition());
        }
    }


    @Override
    public int getItemViewType(int position) {
        if(kategoryModelList.size() == 1)
            return Common.DEFAULT_COLUMN_COUNT;
        else {
            if(kategoryModelList.size() % 2 == 0)
                return Common.DEFAULT_COLUMN_COUNT;
            else
                return  (position > 1 && position == kategoryModelList.size()-1) ? Common.FULL_WIDTH_COLUMN:Common.DEFAULT_COLUMN_COUNT;
        }
    }
}
