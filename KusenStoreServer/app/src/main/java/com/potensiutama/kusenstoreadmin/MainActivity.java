package com.potensiutama.kusenstoreadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.ServerUserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    public static int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private DatabaseReference serverRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if(listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        serverRef = FirebaseDatabase.getInstance().getReference(Common.SERVER_REF);

        firebaseAuth = FirebaseAuth.getInstance();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.show();
        listener = firebaseAuthLocal ->{
            FirebaseUser user = firebaseAuthLocal.getCurrentUser();

            if(user != null){
                checkUserFromFirebase(user);

            }else{
                phoneLogin();
            }
        };

    }

    private void checkUserFromFirebase(FirebaseUser user) {

        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            ServerUserModel userModel = dataSnapshot.getValue(ServerUserModel.class);
                            if(userModel.isActive()){
                                goToHomeActivity(userModel);
                            }else{
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "Kamu harus memiliki izin untuk mengakses aplikasi ini!", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            dialog.dismiss();
                            showRegisterDialog(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Daftar");
        builder.setMessage("Silahkan isi informasi dibawah ini... \n Admin akan melakukan verifikasi akun kamu segera");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);

        edt_phone.setText(user.getPhoneNumber());
        builder.setNegativeButton("BATALKAN", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("DAFTAR", (dialogInterface, i) -> {
                    if(TextUtils.isEmpty(edt_name.getText().toString())){
                        Toast.makeText(MainActivity.this, "Silahkan isi nama kamu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ServerUserModel serverUserModel = new ServerUserModel();
                    serverUserModel.setUid(user.getUid());
                    serverUserModel.setName(edt_name.getText().toString());
                    serverUserModel.setPhone(edt_phone.getText().toString());
                    serverUserModel.setActive(false);

                    dialog.show();

                    serverRef.child(serverUserModel.getUid())
                            .setValue(serverUserModel)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Selamat! Pendaftaran Berhasil!", Toast.LENGTH_SHORT).show();
                            //goToHomeActivity(serverUserModel);
                        }
                    });

                });

        builder.setView(itemView);

        androidx.appcompat.app.AlertDialog registerDialog = builder.create();
        registerDialog.show();
    }

    private void goToHomeActivity(ServerUserModel serverUserModel) {

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    Common.currentServerUser = serverUserModel;
                    startActivity(new Intent(MainActivity.this,HomeActivity.class));
                    finish();

                }).addOnCompleteListener(task -> {
                    Common.currentServerUser = serverUserModel;
                    Common.updateToken(MainActivity.this,task.getResult().getToken());
                    startActivity(new Intent(MainActivity.this,HomeActivity.class));
                    finish();
        });

    }

    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build(),APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == APP_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }else{
                Toast.makeText(this, "Login Gagal!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
