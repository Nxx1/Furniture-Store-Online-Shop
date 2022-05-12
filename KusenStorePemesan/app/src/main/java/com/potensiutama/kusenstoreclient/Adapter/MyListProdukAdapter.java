package com.potensiutama.kusenstoreclient.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.potensiutama.kusenstoreclient.Callback.IRecyclerClickListener;
import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.Database.CartDataSource;
import com.potensiutama.kusenstoreclient.Database.CartDatabase;
import com.potensiutama.kusenstoreclient.Database.LocalCartDataSource;
import com.potensiutama.kusenstoreclient.R;
import com.potensiutama.kusenstoreclient.model.ProdukModel;
import com.potensiutama.kusenstoreclient.ui.productlist.InformasiProdukActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;

public class MyListProdukAdapter extends RecyclerView.Adapter<MyListProdukAdapter.MyViewHolder> {

    private Context context;
    private List<ProdukModel> produkModelList;
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;

    public MyListProdukAdapter(Context context, List<ProdukModel> produkModelList) {
        this.context = context;
        this.produkModelList = produkModelList;
        this.compositeDisposable = new CompositeDisposable();
        this.cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_produk_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(produkModelList.get(position).getGambar1()).into(holder.img_food_image);
        holder.txt_food_price.setText(new StringBuilder("Rp")
                .append(Common.formatPrice(produkModelList.get(position).getHarga_produk())));
        holder.txt_food_name.setText(new StringBuilder("")
                .append(produkModelList.get(position).getNama_produk()));

        //Event
        holder.setListener((view, pos) -> {
            Common.selectedProduk = produkModelList.get(pos);
            Intent intent = new Intent(context.getApplicationContext(), InformasiProdukActivity.class);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return produkModelList.size();
    }

    public ProdukModel getItemAtPosition(int pos){
        return produkModelList.get(pos);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Unbinder unbinder;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.img_food_image)
        ImageView img_food_image;

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
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }
}
