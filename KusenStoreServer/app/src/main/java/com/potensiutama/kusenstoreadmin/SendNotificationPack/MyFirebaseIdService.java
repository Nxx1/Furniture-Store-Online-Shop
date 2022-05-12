package com.potensiutama.kusenstoreadmin.SendNotificationPack;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.TokenModel;

import java.util.HashMap;

public class MyFirebaseIdService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s)
    {
        super.onNewToken(s);
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            updateToken(s);
        }
    }
    private void updateToken(String refreshToken){
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REF)
                .child(Common.currentServerUser.getUid())
                .setValue(new Token(refreshToken))
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
