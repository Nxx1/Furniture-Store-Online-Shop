package com.potensiutama.kusenstoreclient;

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

import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.Remote.ICloudFunctions;
import com.potensiutama.kusenstoreclient.Remote.RetrofitCloudClient;
import com.potensiutama.kusenstoreclient.model.UserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    public static int APP_REQUEST_CODE = 0000;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ICloudFunctions cloudFunctions;
    private DatabaseReference userRef;
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
        compositeDisposable.clear();
        super.onStop();
    }

    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers).build(),
                APP_REQUEST_CODE);
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

    private void goToHomeActivity(UserModel userModel) {

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    Common.currentUser = userModel;
                    startActivity(new Intent(MainActivity.this,HomeActivity.class));
                    finish();

                }).addOnCompleteListener(task -> {
                    Common.currentUser = userModel;
                    Common.updateToken(MainActivity.this,task.getResult().getToken());
                    startActivity(new Intent(MainActivity.this,HomeActivity.class));
                    finish();
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    private void init(){
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());


        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCESER);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions.class);
        listener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user != null){
                CheckUserFromFirebase(user);
            }else {
                phoneLogin();
            }
        };
    }

    private void CheckUserFromFirebase(FirebaseUser user){
        dialog.show();
        userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            //Toast.makeText(MainActivity.this, "Sudah terdaftar", Toast.LENGTH_SHORT).show();

                            UserModel userModel = snapshot.getValue(UserModel.class);
                            goToHomeActivity(userModel);
                        }else{
                            showRegisterDialog(user);
                        }
                        dialog.dismiss();
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
        builder.setTitle("Daftar Akun");
        builder.setMessage("Silahkan isi informasi dibawah ini");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_address = (EditText) itemView.findViewById(R.id.edt_address);
        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);

        edt_phone.setText(user.getPhoneNumber());

        builder.setView(itemView);
        builder.setNegativeButton("Batal", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        builder.setPositiveButton("Daftar", (dialogInterface, i) -> {
            if(TextUtils.isEmpty(edt_name.getText().toString())){
                Toast.makeText(this, "Silahkan isi nama kamu", Toast.LENGTH_SHORT).show();
                return;
            }else if(TextUtils.isEmpty(edt_address.getText().toString())){
                Toast.makeText(this, "Silahkan isi alamat kamu", Toast.LENGTH_SHORT).show();
                return;
            }

            UserModel userModel = new UserModel();
            userModel.setUid(user.getUid());
            userModel.setName(edt_name.getText().toString());
            userModel.setAddress(edt_address.getText().toString());
            userModel.setPhone(edt_phone.getText().toString());

            userRef.child(user.getUid())
                    .setValue(userModel)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            dialogInterface.dismiss();
                            Toast.makeText(MainActivity.this, "Selamat! Pendaftaran Berhasil", Toast.LENGTH_SHORT).show();
                            goToHomeActivity(userModel);
                        }
                    });

        });

        builder.setView(itemView);


        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}
