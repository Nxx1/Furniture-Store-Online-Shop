package com.potensiutama.kusenstoreadmin.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.potensiutama.kusenstoreadmin.ui.order.DetailPesananActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.MyViewHolder> {


    private Context context;
    private List<OrderModel> orderList;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;

    public MyOrderAdapter(Context context, List<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
        calendar = Calendar.getInstance();

        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        TimeZone tz = TimeZone.getTimeZone("Asia/Jakarta");
        simpleDateFormat.setTimeZone(tz);
    }

    public OrderModel getItemAtPosition(int pos){
        return orderList.get(pos);
    }

    public void setItemAtPosition(int pos, OrderModel item){
        orderList.set(pos,item);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_order_items,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(orderList.get(position).getCartItemList().get(0).getProdukImage())
                .into(holder.img_order);
        holder.txt_order_number.setText("ID Pesanan : "+orderList.get(position).getKey());
        holder.txt_order_date.setText(new StringBuilder("Tanggal : ").append(simpleDateFormat.format(orderList.get(position).getCreateDate())));
        holder.txt_order_status.setText(new StringBuilder("").append(Common.convertStatusToString(orderList.get(position).getOrderStatus())));
        holder.txt_name.setText(new StringBuilder("Metode Pembayaran : ").append(orderList.get(position).getRekeningPembayaran()));

        if(orderList.get(position).getOrderStatus() == -1){
            holder.txt_order_status.setTextColor(Color.RED);
        }

        holder.setRecyclerClickListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                Common.orderModelPembayaran = orderList.get(pos);
                context.startActivity(new Intent(context, DetailPesananActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void removeItem(int pos) {
        orderList.remove(pos);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.txt_order_status)
        TextView txt_order_status;
        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_order_date)
        TextView txt_order_date;
        @BindView(R.id.img_order)
        ImageView img_order;

        Unbinder unbinder;

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
            recyclerClickListener.onItemClickListener(view,getAdapterPosition());
        }
    }
}
