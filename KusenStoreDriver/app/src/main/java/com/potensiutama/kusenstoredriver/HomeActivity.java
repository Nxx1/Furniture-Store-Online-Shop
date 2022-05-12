package com.potensiutama.kusenstoredriver;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.potensiutama.kusenstoredriver.EventBus.ChangeMenuClick;
import com.potensiutama.kusenstoredriver.EventBus.ToastEvent;
import com.potensiutama.kusenstoredriver.common.Common;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private int menuClick=-1;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();


        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_order)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString("", Common.currentServerUser.getName(),txt_user);

        menuClick = R.id.nav_order;

        checkIsOpenFromActivity();
    }

    private void init() {

        subscribeToTopic(Common.createTopicOrder());

        dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setMessage("Mohon tunggu...")
                .create();
    }

    private void checkIsOpenFromActivity() {
        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,false);
        if(isOpenFromNewOrder){
            navController.popBackStack();
            navController.navigate(R.id.nav_order);
            menuClick = R.id.nav_order;
        }
    }

    private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    if(!task.isSuccessful())
                        Toast.makeText(this, "Gagal "+task.isSuccessful(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event){
        if(event.getAction() == Common.ACTION.CREATE){
            Toast.makeText(this, "Tambah data berhasil!", Toast.LENGTH_SHORT).show();
        }else if(event.getAction() == Common.ACTION.UPDATE){
            Toast.makeText(this, "Ubah data berhasil!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Hapus data berhasil!", Toast.LENGTH_SHORT).show();
        }
        EventBus.getDefault().postSticky(new ChangeMenuClick(event.isFromFoodList()));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();
        switch (menuItem.getItemId()){
            case R.id.nav_order:
                if(menuItem.getItemId() != menuClick){
                    navController.popBackStack();
                    navController.navigate(R.id.nav_order);
                }
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
                default:
                    menuClick = -1;
                    break;
        }
        menuClick = menuItem.getItemId();
        return true;
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signout")
                .setMessage("Apakah anda yakin ingin keluar dari aplikasi ini?")
                .setNegativeButton("Batalkan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentServerUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
