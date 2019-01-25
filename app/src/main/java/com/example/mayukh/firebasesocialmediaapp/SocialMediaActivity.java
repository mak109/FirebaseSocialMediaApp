package com.example.mayukh.firebasesocialmediaapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SocialMediaActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private FirebaseAuth mAuth;
    private Button btnImagePost;
    private ListView listViewUsers;
    private EditText edtDes;
    private ImageView imgPost;
    private Bitmap imageBitmap;
    private ArrayList<String> usernames,uids;
    private ArrayAdapter adapter,uidAdapter;
    private String imageIdentifier;
    private String imageDownloadLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);
        mAuth  = FirebaseAuth.getInstance();
        btnImagePost = findViewById(R.id.btnImagePost);
        listViewUsers = findViewById(R.id.listViewUsers);
        edtDes = findViewById(R.id.edtDes);
        imgPost = findViewById(R.id.imgPost);
        uids = new ArrayList<>();
        usernames = new ArrayList<>();
        adapter = new ArrayAdapter(SocialMediaActivity.this,android.R.layout.simple_list_item_1,usernames);
        listViewUsers.setAdapter(adapter);
        listViewUsers.setOnItemClickListener(this);
        btnImagePost.setOnClickListener(this);
        imgPost.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logOutItem:
                logOut();
            case R.id.viewPostsItem:
                startActivity(new Intent(SocialMediaActivity.this,ViewPostsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        logOut();
    }
    private void logOut(){
        mAuth.signOut();
        finish();
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.btnImagePost:
                postImageToServer();
                break;
            case R.id.imgPost:
                getImageFromUserDevice();
                break;
        }

    }

    private void getImageFromUserDevice() {
        if(Build.VERSION.SDK_INT < 23){
            getChosenImage();
        }
        else if(Build.VERSION.SDK_INT >= 23)
            if(ContextCompat.checkSelfPermission(SocialMediaActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1000);
              else {
            getChosenImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getChosenImage();
            }

        }
    }

    private void getChosenImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2000 && resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();
                try{

                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                    imgPost.setImageBitmap(imageBitmap);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

        }
    }

    private void postImageToServer() {
        // Get the data from an ImageView as bytes
        if (imageBitmap != null) {
            imageIdentifier = UUID.randomUUID() + ".png";
            imgPost.setDrawingCacheEnabled(true);
            imgPost.buildDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("my_images").child(imageIdentifier).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(SocialMediaActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Toast.makeText(SocialMediaActivity.this, "Uploading Successful!!!", Toast.LENGTH_LONG).show();
                    edtDes.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance().getReference().child("my_users").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            String username = (String) dataSnapshot.child("username").getValue();
                            uids.add(dataSnapshot.getKey());
                            usernames.add(username);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                         if(task.isSuccessful()){
                             imageDownloadLink = task.getResult().toString();
                         }
                        }
                    });

                }
            });

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        HashMap<String,String> dataMap = new HashMap<>();
        dataMap.put("fromWhom",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        dataMap.put("imageIdentifier",imageIdentifier);
        dataMap.put("imageLink",imageDownloadLink);
        dataMap.put("des",edtDes.getText().toString());
        FirebaseDatabase.getInstance().getReference().child("my_users").child(uids.get(position))
                .child("received_posts").push().setValue(dataMap);
        Toast.makeText(SocialMediaActivity.this,"Data sent successfully",Toast.LENGTH_SHORT).show();

    }
}
