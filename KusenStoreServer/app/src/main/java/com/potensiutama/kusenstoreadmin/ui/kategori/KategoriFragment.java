package com.potensiutama.kusenstoreadmin.ui.kategori;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.potensiutama.kusenstoreadmin.EventBus.ToastEvent;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.adapter.MyKategoriAdapter;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.common.MySwipeHelper;
import com.potensiutama.kusenstoreadmin.model.KategoryModel;
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

public class KategoriFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private KategoriViewModel categoryViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyKategoriAdapter adapter;

    List<KategoryModel> kategoryModels;
    ImageView img_category;
    private Uri imageUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        categoryViewModel =
                ViewModelProviders.of(this).get(KategoriViewModel.class);
        View root = inflater.inflate(R.layout.fragment_kategori, container, false);

        unbinder = ButterKnife.bind(this,root);
        initView();
        categoryViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(),""+s,Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        categoryViewModel.getCategoryListMultable().observe(getViewLifecycleOwner(),categoryModelList -> {
            dialog.dismiss();
            kategoryModels = categoryModelList;
            adapter= new MyKategoriAdapter(getContext(), kategoryModels);
            recycler_menu.setAdapter(adapter);
            recycler_menu.setLayoutAnimation(layoutAnimationController);
        });

        return root;
    }

    private void initView() {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        //layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recycler_menu.setLayoutManager(layoutManager);
        //recycler_menu.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(),recycler_menu,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

                buf.add(new MyButton(getContext(),"Hapus",30,0, Color.parseColor("#333639"),
                        pos -> {
                            Common.categorySelected = kategoryModels.get(pos);
                            showDeleteDialog();

                        }));

                buf.add(new MyButton(getContext(),"Ubah",30,0, Color.parseColor("#560027"),
                        pos -> {
                            Common.categorySelected = kategoryModels.get(pos);
                            showUpdateDialog();

                        }));
            }
        };

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.action_create){
            showAddDialog();
        }
            

        return super.onOptionsItemSelected(item);

    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Hapus");
        builder.setMessage("Apakah anda yakin ingin menghapus kategori ini?");

        builder.setNegativeButton("Batal", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton("Hapus",((dialogInterface, i) -> deleteCategory()));
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteCategory() {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .removeValue()
                .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                   EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.DELETE,true));
                    //Toast.makeText(getContext(), "Hapus data berhasil", Toast.LENGTH_SHORT).show();
                });
    }

    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Ubah");
        builder.setMessage("Silahkan isi informasi dibawah ini...");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_kategori,null);
        EditText edt_category_name = (EditText) itemView.findViewById(R.id.edt_category_name);
        img_category = (ImageView) itemView.findViewById(R.id.img_category);

        edt_category_name.setText(new StringBuilder("").append(Common.categorySelected.getName()));
        Glide.with(getContext()).load(Common.categorySelected.getImage()).into(img_category);

        img_category.setOnClickListener(view -> {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Pilih Gambar"),PICK_IMAGE_REQUEST);

        });

        builder.setNegativeButton("Batal", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton("Proses", (dialogInterface, i) -> {

            Map<String,Object> updateData = new HashMap<>();
            updateData.put("name",edt_category_name.getText().toString());

            if(imageUri != null){
                dialog.setMessage("Uploading...");
                dialog.show();

                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/"+unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {
                            dialog.dismiss();
                            imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                updateData.put("image",uri.toString());
                                updateCategory(updateData);
                            });
                        }).addOnProgressListener(taskSnapshot -> {
                            double progress = (100* taskSnapshot.getBytesTransferred() /taskSnapshot.getTotalByteCount());
                            dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                        });

            }else {
                updateCategory(updateData);
            }

        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showAddDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Tambah Data");
        builder.setMessage("Silahkan isi informasi dibawah ini...");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_kategori,null);
        EditText edt_category_name = (EditText) itemView.findViewById(R.id.edt_category_name);
        img_category = (ImageView) itemView.findViewById(R.id.img_category);

        Glide.with(getContext()).load(R.drawable.ic_image_gray_24dp).into(img_category);

        img_category.setOnClickListener(view -> {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Pilih Gambar"),PICK_IMAGE_REQUEST);

        });

        builder.setNegativeButton("Batal", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton("Tambah", (dialogInterface, i) -> {

            KategoryModel kategoryModel = new KategoryModel();
            kategoryModel.setName(edt_category_name.getText().toString());
            kategoryModel.setProdukModelList(new ArrayList<>());

            if(imageUri != null){
                dialog.setMessage("Uploading...");
                dialog.show();

                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/"+unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                        kategoryModel.setImage(uri.toString());
                        addCategory(kategoryModel);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100* taskSnapshot.getBytesTransferred() /taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });

            }else {
                addCategory(kategoryModel);
            }

        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void updateCategory(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.UPDATE,true));
                });
    }

    private void addCategory(KategoryModel kategoryModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .push()
                .setValue(kategoryModel)
                .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.CREATE,true));
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            if(data != null && data.getData() != null){
                imageUri = data.getData();
                img_category.setImageURI(imageUri);
            }
        }
    }
}