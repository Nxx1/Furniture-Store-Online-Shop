package com.potensiutama.kusenstoreclient.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.potensiutama.kusenstoreclient.Callback.IProdukCallbackListener;
import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.model.ProdukModel;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel implements IProdukCallbackListener {

    private MutableLiveData<List<ProdukModel>> produkListMultable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private IProdukCallbackListener produkCallbackListener;

    public HomeViewModel() {
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
        Common.daftarProduk = new ArrayList<>();

        Query produkRef = FirebaseDatabase.getInstance().getReference(Common.PRODUK_REF)
                .orderByChild("createDate");
        produkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    ProdukModel produkModel = itemSnapshot.getValue(ProdukModel.class);
                    if(produkModel.getUnggulan()){
                        Common.daftarProduk.add(produkModel);
                    }
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