package com.example.android.travelmatics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {
    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference mDatabaseReferece;
    private static final int PICTURE_RESULT = 42;
    EditText txtTitle,txtPrice,txtDescription;
    ImageView imageView;
    TravelDeal deal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
       // FirebaseUtil.openFbReference("traveldeals",this);
        mfirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReferece = FirebaseUtil.mDatabaseReference;

        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtDescription = (EditText) findViewById(R.id.txtDescription);
        txtPrice = (EditText) findViewById(R.id.txtPrice);
        imageView = (ImageView)findViewById(R.id.image);

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal)intent.getSerializableExtra("Deal");
        if(deal == null){
            deal = new TravelDeal();
        }
        this.deal = deal;
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());
        Button btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,"Insert Picture"),PICTURE_RESULT);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deals Created Successfully", Toast.LENGTH_SHORT).show();
                clean();
                backToUser();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_SHORT).show();
                backToUser();
                return true;
                default:
                    return super.onOptionsItemSelected(item);

        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu,menu);
        if(FirebaseUtil.isAdmin ){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditTexts(true);
            findViewById(R.id.btnImage).setEnabled(true);
        }else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditTexts(false);
            findViewById(R.id.btnImage).setEnabled(false);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String url = taskSnapshot.getDownloadUrl().toString();
                    String pictureName = taskSnapshot.getStorage().getPath();
                    deal.setImageUrl(url);
                    deal.setImageName(pictureName);
                    showImage(url);
                }
            });
        }
    }

    private void clean() {
        txtTitle.setText("");
        txtDescription.setText("");
        txtPrice.setText("");

         txtTitle.requestFocus();
    }

    private void saveDeal()
    {
        deal.setTitle(txtTitle.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());
        if(deal.getId() == null){
            mDatabaseReferece.push().setValue(deal);
        }else {
            mDatabaseReferece.child(deal.getId()).setValue(deal);
        }

    }

    private void  deleteDeal(){
        if(deal == null){
            Toast.makeText(this, "Please save deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReferece.child(deal.getId()).removeValue();
        if(deal.getImageName() != null && deal.getImageName().isEmpty()==false){
            StorageReference picRef = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image","Image deleted successfully");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image", e.getMessage());

                }
            });
        }
    }
    private  void backToUser(){
        Intent intent = new Intent(this,UserActivity.class);
        startActivity(intent);
    }

    private void enableEditTexts(boolean isEnabled){
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
}
    private void showImage(String url){
        if(url != null && url.isEmpty()== false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this)
                    .load(url)
                    .resize(width, width *2/3)
                    .centerCrop()
                    .into(imageView);
        }
    }

}
