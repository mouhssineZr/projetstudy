package com.example.chatapp1.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.example.chatapp1.AjouterAnnonce;
import com.example.chatapp1.Annonce;
import com.example.chatapp1.AnnonceAdapter;
import com.example.chatapp1.R;
import com.example.chatapp1.databinding.ActivityMainBinding;
import com.example.chatapp1.utilities.Constants;
import com.example.chatapp1.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    //methode onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();


        int[] imageId={R.drawable.avatar0,R.drawable.avatar1,R.drawable.avatar2,R.drawable.avatar3};
        String[] nomAnnonce={"Preparation Bac","cours java","Oracle DBA1-DBA2","Python"};
        String[] prixAnnonce ={"2990,00 Dh","5000,00 Dh","299,00 Dh","299,00 Dh"};
        String[] dateAnnonce = {"09/10/22","09/10/22","09/10/22","09/10/22"};
        String[] dateDebut = {"09/1/22","09/11/22","09/11/22","09/12/22"};

        ArrayList<Annonce> annonceArrayList = new ArrayList<>();

        for(int i=0;i<imageId.length;i++){
            Annonce annonce = new Annonce(nomAnnonce[i],prixAnnonce[i],dateAnnonce[i],dateDebut[i],imageId[i]);
            annonceArrayList.add(annonce);
        }

        AnnonceAdapter annonceAdapter = new AnnonceAdapter(MainActivity.this,annonceArrayList);
        binding.listview.setAdapter(annonceAdapter);
    }

    //methode setListenner pour ecouter les cliques
    private void setListeners(){
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.imageProfile.setOnClickListener(view -> startActivity(
                new Intent(getApplicationContext(),AjouterAnnonce.class)));
    }


    //methode loadUserDetails pour telecharger les donnees depuis Firebase
    private void loadUserDetails(){
        //load le nom utilisateur
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));

        //load l'image de profil
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnSuccessListener(unused -> showToast("Token updated successfully"))
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    private void signOut(){
        showToast("Signing out ...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused ->{
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }
}