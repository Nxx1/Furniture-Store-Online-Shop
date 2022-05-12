package com.potensiutama.kusenstoreadmin.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.model.RekeningBankModel;

import java.util.ArrayList;

public class MyDataRekeningAdapter  extends BaseAdapter {
    Context context;
    private ArrayList<RekeningBankModel> rekeningBankModelArrayList = new ArrayList<>();

    public void setRekeningList(ArrayList<RekeningBankModel> rekeningBankModelArrayList) {
        this.rekeningBankModelArrayList = rekeningBankModelArrayList;
    }

    public MyDataRekeningAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return rekeningBankModelArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return rekeningBankModelArrayList.get(i);
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
                    .inflate(R.layout.layout_rekening_item, viewGroup, false);
        }

        ViewHolder viewHolder = new ViewHolder(itemView);

        RekeningBankModel rekeningBankModel = (RekeningBankModel) getItem(i);
        viewHolder.bind(rekeningBankModel);
        return itemView;
    }

    private class ViewHolder {
        private TextView tNamaPemilik, tNamaBank,tNomorRekening;

        ViewHolder(View view) {
            tNamaPemilik = view.findViewById(R.id.txt_rekening_nama_pemilik);
            tNamaBank = view.findViewById(R.id.txt_rekening_nama_bank);
            tNomorRekening = view.findViewById(R.id.txt_rekening_nomor_rekening);
        }

        void bind(RekeningBankModel rekeningBankModel) {

            tNamaPemilik.setText(rekeningBankModel.getNama_lengkap());
            tNamaBank.setText(rekeningBankModel.getNama_bank());
            tNomorRekening.setText(rekeningBankModel.getNomor_rekening().toString());
        }
    }
}
