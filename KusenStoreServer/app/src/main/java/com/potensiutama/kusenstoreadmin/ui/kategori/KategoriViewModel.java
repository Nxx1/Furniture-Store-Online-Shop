package com.potensiutama.kusenstoreadmin.ui.kategori;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.potensiutama.kusenstoreadmin.calback.IKategoriCallbackListener;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.KategoryModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class KategoriViewModel extends ViewModel implements IKategoriCallbackListener {

    private MutableLiveData<List<KategoryModel>> categoryListMultable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private IKategoriCallbackListener categoryCallbackListener;

    public KategoriViewModel() {
        categoryCallbackListener = this;

    }

    public MutableLiveData<List<KategoryModel>> getCategoryListMultable() {
        if (categoryListMultable == null){
            categoryListMultable = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadCategories();
        }
        return categoryListMultable;

    }

    public void loadCategories() {

        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF);
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<KategoryModel> tempList = new ArrayList<>();
                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    KategoryModel kategoryModel = itemSnapshot.getValue(KategoryModel.class);
                    kategoryModel.setMenu_id(itemSnapshot.getKey());
                    tempList.add(kategoryModel);
                }
                categoryCallbackListener.onCategoryLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                categoryCallbackListener.onCategoryLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onCategoryLoadSuccess(List<KategoryModel> kategoryModelList) {
        categoryListMultable.setValue(kategoryModelList);
    }

    @Override
    public void onCategoryLoadFailed(String message) {
        messageError.setValue(message);
    }
}