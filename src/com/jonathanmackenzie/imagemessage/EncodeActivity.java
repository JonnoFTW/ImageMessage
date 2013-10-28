package com.jonathanmackenzie.imagemessage;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.util.Base64;
import android.util.Log;

public class EncodeActivity extends Activity {

    private ImageView iv;
    private static final int GALLERY_PICK_EMBED = 203;
    private static final int CAMERA_REQUEST= 204;
    private Bitmap containerBitmap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);
        Intent intent = getIntent();
        // Get the image uri out of the intent and stick it into the imageview
        Uri img = intent.getData();
        containerBitmap = BitmapFactory.decodeFile(img.getPath());
        iv = (ImageView) findViewById(R.id.imageView);
        iv.setImageURI(img);
        
        final EditText et = (EditText) findViewById(R.id.editText);
        et.setHint("Your secret message...");
        // Calculate this properly
      //  et.setFilters(new InputFilter[] {new InputFilter.LengthFilter(iv.getHeight()*iv.getWidth()-10)});
    /*    et.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                encodeImageText(et.getText().toString());
                
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // Check if the message length exceeds what we can encode
                // Prevent adding more characters

            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.encode, menu);
        return true;
    }
    /**
     * Could probably be taken from MainActivity since they both do the
     * same thing
     * @param v
     */
    public void fetchImageGallery(View v) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        Intent chooser = Intent.createChooser(i, "Choose a Picture to Embed");
        if (v == findViewById(R.id.buttonEmbedImage))
            startActivityForResult(chooser, GALLERY_PICK_EMBED);
    }
    public void fetchImageCamera(View v) {
        Intent cameraIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("EncodeActivity", "Activity result returned " + requestCode + " "
                + resultCode);
        if (resultCode == RESULT_OK && data != null) {
            proceed(encodeImageImage(data.getData()));
            
        }
    }
    private void proceed(Bitmap container) {
        if(container == null) {
            
        } else {
            Intent intent = new Intent(this, FinalActivity.class);
            intent.putExtra("BitmapImage",container);
            startActivity(intent);
        }
            
    }
    private Bitmap encodeImageImage(Uri image) {
        ImageView ive = (ImageView) findViewById(R.id.imageViewEmbed);
        ive.setImageURI(image);
        Bitmap bitmap = BitmapFactory.decodeFile(image.getPath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageData = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT); 
        return encodeImageText(imageData);
    }
    /**
     * Take the image file f and make insert the message
     * http://web.mit.edu/~georgiou/www/steganography/
     * @param f
     * @param message
     *            the message to be encoded into the string
     */
   
    private Bitmap encodeImageText(String message) {
        Bitmap bitmap = containerBitmap;
        int[] pixels = new int[bitmap.getHeight()*bitmap.getWidth()];
        bitmap.getPixels(pixels, 0, 1, 0, 0, bitmap.getWidth(), bitmap.getHeight());
        
        String string = new StringBuffer().append("!@#").append(message).append((char)0).toString();
        int imageidx = 0;
        for (char c : string.toCharArray()) {
            int charCode = c;
            pixels[imageidx] = (pixels[imageidx] & (~3)) | (charCode & 3);
            imageidx++;
            if ((imageidx & 3) == 3) {
                imageidx++;
            }
            pixels[imageidx] = (pixels[imageidx] & (~3)) | ((charCode >> 2) & 3);
            imageidx++;
            if ((imageidx & 3) == 3) {
                imageidx++;
            }
            pixels[imageidx] = (pixels[imageidx] & (~3)) | ((charCode >> 4) & 3);
            imageidx++;
            if ((imageidx & 3) == 3) {
                imageidx++;
            }
            pixels[imageidx] = (pixels[imageidx] & (~3)) | ((charCode >> 6) & 3);
            imageidx++;
            if ((imageidx & 3) == 3) {
                imageidx++;
            }
        }
        return bitmap;
    
    }
}
