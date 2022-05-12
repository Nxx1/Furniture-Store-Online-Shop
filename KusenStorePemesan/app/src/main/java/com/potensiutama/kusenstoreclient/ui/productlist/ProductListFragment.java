package com.potensiutama.kusenstoreclient.ui.productlist;

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
import androidx.appcompat.app.AppCompatActivity;
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
import com.potensiutama.kusenstoreclient.EventBus.MenuItemBack;
import com.potensiutama.kusenstoreclient.R;
import com.potensiutama.kusenstoreclient.model.ProdukModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class ProductListFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private ImageView img_food;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;

    private ProductListViewModel listProdukViewModel;

    private List<ProdukModel> produkModelList;


    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_produk_list;


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
                ViewModelProviders.of(this).get(ProductListViewModel.class);
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