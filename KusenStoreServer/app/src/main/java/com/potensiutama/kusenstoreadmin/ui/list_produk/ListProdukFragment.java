package com.potensiutama.kusenstoreadmin.ui.list_produk;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.potensiutama.kusenstoreadmin.EventBus.ChangeMenuClick;
import com.potensiutama.kusenstoreadmin.EventBus.ToastEvent;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.adapter.MyListProdukAdapter;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.common.MySwipeHelper;
import com.potensiutama.kusenstoreadmin.model.ProdukModel;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class ListProdukFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private ImageView img_food;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;

    private ListProdukViewModel listProdukViewModel;

    private List<ProdukModel> produkModelList;


    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_produk_list;

    @BindView(R.id.fab_tambah_produk)
    FloatingActionButton fab_tambah_produk;

    LayoutAnimationController layoutAnimationController;
    MyListProdukAdapter adapter;
    private Uri imageUri = null;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.produk_list_menu,menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearchProduk(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(view -> {
            EditText ed = (EditText) searchView.findViewById(R.id.search_src_text);
            ed.setText("");
            searchView.setQuery("",false);
            searchView.onActionViewCollapsed();
            menuItem.collapseActionView();
            listProdukViewModel.getProdukListMultable().observe(getViewLifecycleOwner(),produkModels -> {
                dialog.dismiss();
                produkModelList = produkModels;
                adapter= new MyListProdukAdapter(getContext(), produkModelList);
                recycler_produk_list.setAdapter(adapter);
                recycler_produk_list.setLayoutAnimation(layoutAnimationController);
            });

        });
    }

    private void startSearchProduk(String s){
        List<ProdukModel> resultProduk = new ArrayList<>();
        for (int i = 0; i<Common.daftarProduk.size(); i++){
            ProdukModel produkModel = Common.daftarProduk.get(i);
            if(produkModel.getNama_produk().toLowerCase().contains(s)) {
                produkModel.setPositionInList(i);
                resultProduk.add(produkModel);
            }
        }
        listProdukViewModel.getProdukListMultable().setValue(resultProduk);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        listProdukViewModel =
                ViewModelProviders.of(this).get(ListProdukViewModel.class);
        View root = inflater.inflate(R.layout.fragment_produk_list, container, false);
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

        fab_tambah_produk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),TambahProdukActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }

    private void initViews() {

        setHasOptionsMenu(true);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
       // recyclerView.setLayoutManager(mLayoutManager);
        recycler_produk_list.setHasFixedSize(true);
        recycler_produk_list.setLayoutManager(mLayoutManager);

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        //EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }
}