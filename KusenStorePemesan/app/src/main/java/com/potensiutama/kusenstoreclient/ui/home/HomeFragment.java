package com.potensiutama.kusenstoreclient.ui.home;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.potensiutama.kusenstoreclient.Adapter.MyListProdukAdapter;
import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.Common.SpacesItemDecoration;
import com.potensiutama.kusenstoreclient.R;
import com.potensiutama.kusenstoreclient.model.ProdukModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class HomeFragment extends Fragment {

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;

    private HomeViewModel listProdukViewModel;

    private List<ProdukModel> produkModelList;


    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_produk_list;

    LayoutAnimationController layoutAnimationController;
    MyListProdukAdapter adapter;
    private Uri imageUri = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        listProdukViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this,root);
        initViews();
        listProdukViewModel.getProdukListMultable().observe(getViewLifecycleOwner(), produkModels -> {
            if(produkModels != null){
                produkModelList = produkModels;
                adapter = new MyListProdukAdapter(getContext(), produkModelList);
                recycler_produk_list.setAdapter(adapter);
                recycler_produk_list.setLayoutAnimation(layoutAnimationController);
            }
        });

        return root;
    }

    private void initViews() {

        setHasOptionsMenu(true);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(adapter != null){
                    switch (adapter.getItemViewType(position)){
                        case Common.DEFAULT_COLUMN_COUNT : return 1;
                        case Common.FULL_WIDTH_COLUMN: return 2;
                        default: return -1;
                    }
                }
                return -1;
            }
        });
        recycler_produk_list.setLayoutManager(layoutManager);
        recycler_produk_list.addItemDecoration(new SpacesItemDecoration(8));

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}