package com.potensiutama.kusenstoreadmin.ui.list_produk;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.potensiutama.kusenstoreadmin.calback.IProdukCallbackListener;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.ProdukModel;

import java.util.ArrayList;
import java.util.List;

public class ListProdukViewModel extends ViewModel implements IProdukCallbackListener {

    private MutableLiveData<List<ProdukModel>> produkListMultable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private IProdukCallbackListener produkCallbackListener;

    public ListProdukViewModel() {
        produkCallbackListener = this;
    }

    public MutableLiveData<List<ProdukModel>> getProdukListMultable() {
        if (produkListMultable == null){
            produkListMultable = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadProduk();
        }
        return produkListMultable;

    }

    public void loadProduk() {
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Common.PRODUK_REF);
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Common.daftarProduk = new ArrayList<>();
                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    ProdukModel produkModel = itemSnapshot.getValue(ProdukModel.class);
                    Common.daftarProduk.add(produkModel);
                }
                produkCallbackListener.onProdukLoadSuccess(Common.daftarProduk);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                produkCallbackListener.onProdukLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onProdukLoadSuccess(List<ProdukModel> produkModelList) {
        produkListMultable.setValue(produkModelList);
    }

    @Override
    public void onProdukLoadFailed(String message) {
        messageError.setValue(message);
    }
}