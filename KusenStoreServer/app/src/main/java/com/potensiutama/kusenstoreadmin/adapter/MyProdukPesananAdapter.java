package com.potensiutama.kusenstoreadmin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class MyProdukPesananAdapter extends BaseAdapter {
    Context context;
    private List<CartItem> cartItemArrayList = new ArrayList<>();

    public void setProdukCheckoutList(List<CartItem> cartItemArrayList) {
        this.cartItemArrayList = cartItemArrayList;
    }

    public MyProdukPesananAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return cartItemArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return cartItemArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View itemView = view;

        if (itemView == null) {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_item_checkout_produk, viewGroup, false);
        }

        ViewHolder viewHolder = new ViewHolder(itemView);

        CartItem cartItem = (CartItem) getItem(i);
        viewHolder.bind(cartItem);
        return itemView;
    }

    private class ViewHolder {
        private TextView tNamaProduk, tHargaProduk,tQuantity;
        private ImageView imgFotoProduk;

        ViewHolder(View view) {
            tNamaProduk = view.findViewById(R.id.txt_checkout_nama_produk);
            tHargaProduk = view.findViewById(R.id.txt_checkout_harga);
            tQuantity = view.findViewById(R.id.txt_checkout_quantity);
            imgFotoProduk = view.findViewById(R.id.checkout_foto_produk);
        }

        void bind(CartItem cartItem) {

            tNamaProduk.setText(cartItem.getProdukName());
            tHargaProduk.setText("Harga : Rp"+ Common.formatPrice(cartItem.getProdukPrice()).toString());
            tQuantity.setText("x"+String.valueOf(cartItem.getProdukQuantity()));
            Glide.with(context).load(cartItem.getProdukImage()).into(imgFotoProduk);
        }
    }
}
