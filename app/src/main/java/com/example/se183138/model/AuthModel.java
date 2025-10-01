package com.example.se183138.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthModel {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public AuthModel(){
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://prm392firebase-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
    }

    public void login(String email, String password, AuthCallBack callBack){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        callBack.onSuccess("Login successfully");
                    }else{
                        callBack.onFailure(task.getException().getMessage());
                    }
                });
    }
    public void signup(String email, String password,String username, AuthCallBack callBack){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String uid = mAuth.getCurrentUser().getUid();
                        User user = new User(uid,username,email);
                        mDatabase.child(uid).setValue(user);
                        callBack.onSuccess("Sign up successfully");
                    }else{
                        callBack.onFailure(task.getException().getMessage());
                    }
                });
    }
    public void logout(){
        mAuth.signOut();
    }

    public interface AuthCallBack{
        void onSuccess(String message);
        void onFailure(String message);
    }
}
