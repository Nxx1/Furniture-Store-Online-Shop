package com.potensiutama.kusenstoreclient.SendNotificationPack;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.potensiutama.kusenstoreclient.Common.Common;

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
                .child(Common.currentUser.getUid())
                .setValue(new Token(refreshToken))
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
