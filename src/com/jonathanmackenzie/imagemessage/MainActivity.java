package com.jonathanmackenzie.imagemessage;

import com.jonathanmackenzie.imagemessage.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

    private static final int CAMERA_REQUEST = 200;
    private static final int GALLERY_PICK_ENCODE = 201;
    private static final int GALLERY_PICK_DECODE = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Taken from the tutorial at https://developer.android.com/training/sharing/receive.html
        final Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        final MainActivity ma = this;
        if(Intent.ACTION_SEND.equals(action) && type != null) {
            if(type.startsWith("image/")) {
                // Prompt the user to pick which method to use
                Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("What do you want to do with the image?");
                builder.setTitle("Select Operation");
                builder.setPositiveButton("Encode", new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Intent incomingIntent = new Intent(ma, EncodeActivity.class);
                        incomingIntent.setData(intent.getData());
                        startActivity(incomingIntent);
                    }
                });
                builder.setNegativeButton("Decode", new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Intent incomingIntent = new Intent(ma, DecodeActivity.class);
                        incomingIntent.setData(intent.getData());
                        startActivity(incomingIntent);
                    }
                });
                builder.setNeutralButton("Cancel", new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit or something
                        
                    }
                });
                
                AlertDialog ad = builder.create();
                ad.show();
                
              
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void fetchImageGallery(View v) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        Intent chooser = Intent.createChooser(i, "Choose a Picture");
        if (v == findViewById(R.id.buttonEncodeExisting))
            startActivityForResult(chooser, GALLERY_PICK_ENCODE);
        else if (v == findViewById(R.id.buttonDecode))
            startActivityForResult(chooser, GALLERY_PICK_DECODE);
    }

    public void fetchImageCamera(View v) {
        Intent cameraIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null)
            return;
        Log.i("MainActivity", "Activity result returned " + requestCode + " "
                + resultCode+" Data="+data.getDataString());
        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == CAMERA_REQUEST
                    || requestCode == GALLERY_PICK_ENCODE) {
                // Start the encode activity with the image
                Intent intent = new Intent(this, EncodeActivity.class);
                intent.setData(data.getData());
                startActivity(intent);
            } else if (requestCode == GALLERY_PICK_DECODE) {
                // Start the decode activity
                Intent intent = new Intent(this, DecodeActivity.class);
                intent.setData(data.getData());
                startActivity(intent);
            }

        } else if (resultCode == RESULT_CANCELED || data == null) {
            AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setMessage("You have cancelled selecting an image");
            ad.setTitle("Operation Cancelled");
            ad.setButton("Ok", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    dialog.cancel();
                }
            });
            ad.show();
        }
    }

    public void openDecodeActivity() {
        startActivity(new Intent(this, DecodeActivity.class));
    }
}
