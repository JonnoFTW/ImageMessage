package com.jonathanmackenzie.imagemessage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
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
        try {
            containerBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),img);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.i("EncodeActivity", "Container: "+containerBitmap);
        iv = (ImageView) findViewById(R.id.imageView);
        iv.setImageURI(img);
        
        final EditText et = (EditText) findViewById(R.id.editText);
        et.setHint("Your secret message...");
        // Calculate this properly
      //  et.setFilters(new InputFilter[] {new InputFilter.LengthFilter(iv.getHeight()*iv.getWidth()-10)});
        final View rootview =  findViewById(R.id.encodeRoot);
        rootview.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            
            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                encodeText(et.getText().toString());
            }
        });

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
    public void testDecode(View v) {
        Intent intent = new Intent(this, DecodeActivity.class);
        // Put the containerBitamp into storage and 
        // keep the path in the intent
        try {
            String filepath = "toDecodeBitmap";
            FileOutputStream fos = openFileOutput(filepath, Context.MODE_PRIVATE);
            containerBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            intent.putExtra("bitmap", filepath);
            startActivity(intent);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("EncodeActivity", "Activity result returned " + requestCode + " "
                + resultCode);
        if (resultCode == RESULT_OK && data != null) {
            encodeImage(data.getData());
        }
    }
    private void proceed(Bitmap container) {
        if(container == null) {
            Log.i("EncodeActivity", "Container bitmap is null");
        } else {
            Intent intent = new Intent(this, FinalActivity.class);
            intent.putExtra("BitmapImage",container);
            startActivity(intent);
        }
            
    }
    private Bitmap encodeImage(Uri image) {
        ImageView ive = (ImageView) findViewById(R.id.imageViewEmbed);
        ive.setImageURI(image);
   /*     Bitmap bitmap = BitmapFactory.decodeFile(image.getPath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageData = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT); */
        return null;
        //return encodeText(imageData);
    }
    /**
     * Take the image file f and make insert the message
     * http://web.mit.edu/~georgiou/www/steganography/
     * @param f
     * @param message
     *            the message to be encoded into the string
     */
   
    private Bitmap encodeText(String message) {
        Log.i("EncodeActivity", "encoding message:"+message);
        Bitmap bitmap = containerBitmap;
        int[] pixels = new int[bitmap.getHeight()*bitmap.getWidth()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        
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
        iv.setImageBitmap(bitmap);
        return bitmap;
    
    }
}
