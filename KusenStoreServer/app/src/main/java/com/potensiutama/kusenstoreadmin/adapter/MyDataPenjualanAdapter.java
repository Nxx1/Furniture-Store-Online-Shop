package com.potensiutama.kusenstoreadmin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.calback.IRecyclerClickListener;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.CartItem;
import com.potensiutama.kusenstoreadmin.model.OrderModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyDataPenjualanAdapter extends RecyclerView.Adapter<MyDataPenjualanAdapter.MyViewHolder>  {
    Context context;
    List<OrderModel> dataPenjualanModelList;
    SimpleDateFormat simpleDateFormat;

    public MyDataPenjualanAdapter(Context context, List<OrderModel> dataPenjualanModels) {
        this.context = context;
        this.dataPenjualanModelList = dataPenjualanModels;
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        TimeZone tz = TimeZone.getTimeZone("Asia/Jakarta");
        simpleDateFormat.setTimeZone(tz);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_data_penjualan_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_order_number.setText(new StringBuilder("Tanggal : ").append(simpleDateFormat.format(dataPenjualanModelList.get(position).getCreateDate())));
        Common.setSpanStringColor("Nama : ", dataPenjualanModelList.get(position).getUserName(),
                holder.txt_name, Color.parseColor("#00574B"));
        Common.setSpanStringColor("Jumlah Pesanan : ", dataPenjualanModelList.get(position).getCartItemList() == null ? "0" :
                        String.valueOf(dataPenjualanModelList.get(position).getCartItemList().size()),
                holder.txt_num_item, Color.parseColor("#4B647D"));
        holder.txt_harga_order.setText("Rp"+ Common.formatPrice(dataPenjualanModelList.get(position).getTotalPayment()));

    }

    public List<OrderModel> getListDataPenjualan() {
        return dataPenjualanModelList;
    }


    @Override
    public int getItemCount() {
        return dataPenjualanModelList.size();
    }

    public OrderModel getItemAtPosition(int pos){
        return dataPenjualanModelList.get(pos);
    }

    public void removeItem(int pos) {
        dataPenjualanModelList.remove(pos);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_harga_order)
        TextView txt_harga_order;
        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_num_item)
        TextView txt_num_item;

        private Unbinder unbinder;

        IRecyclerClickListener recyclerClickListener;

        public void setRecyclerClickListener(IRecyclerClickListener recyclerClickListener) {
            this.recyclerClickListener = recyclerClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
//            recyclerClickListener.onItemClickListener(view,getAdapterPosition());
        }
    }
}
