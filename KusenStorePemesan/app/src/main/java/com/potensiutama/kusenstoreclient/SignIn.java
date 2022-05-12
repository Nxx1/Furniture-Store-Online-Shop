package com.potensiutama.kusenstoreclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.potensiutama.kusenstoreclient.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class SignIn extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPassword = (MaterialEditText) findViewById(R.id.edtPassword);
        edtUsername = (MaterialEditText) findViewById(R.id.edtUsername);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("Users");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                mDialog.setMessage("Mohon Tunggu...");
                mDialog.show();


                table_user.addValueEventListener(new ValueEventListener() {


                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //Check if User not exist in database
                        if(dataSnapshot.child(edtUsername.getText().toString()).exists()){
                            //Get User Information
                            mDialog.dismiss();
                            User user = dataSnapshot.child(String.valueOf(edtUsername.getText())).getValue(User.class);
                            if(user.getPassword().equals(edtPassword.getText().toString())){
                                Toast.makeText(SignIn.this,"Sign In Success", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignIn.this, HomeActivity.class));
                                finish();
                            }else{
                                Toast.makeText(SignIn.this,"Sign In Failed", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            mDialog.dismiss();
                            Toast.makeText(SignIn.this,"Username Tidak Terdaftar", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });


    }
}
