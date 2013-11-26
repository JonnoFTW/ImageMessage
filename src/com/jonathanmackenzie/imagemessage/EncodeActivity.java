package com.jonathanmackenzie.imagemessage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

public class EncodeActivity extends Activity {

    private ImageView iv;
    private static final int GALLERY_PICK_EMBED = 203;
    private static final int CAMERA_REQUEST = 204;
    private EditText et;
    private Location l;
    private Bitmap containerBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);
        Intent intent = getIntent();
        // Get the image uri out of the intent and stick it into the imageview
        Uri img = intent.getData();
        try {
            containerBitmap = MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(), img);
            Log.i("EncodeActivity", "Got bitmap," + img.getPath());
            containerBitmap = containerBitmap.copy(Bitmap.Config.ARGB_8888,
                    true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        iv = (ImageView) findViewById(R.id.imageView);
        iv.setImageBitmap(containerBitmap);

        et = (EditText) findViewById(R.id.editText);
        et.setHint("Your secret message...");
        // Calculate this properly
        int maxlen = containerBitmap.getHeight() * containerBitmap.getWidth()
                / 8 - (32 + 24);
        Log.i("EncodeActivity", "Max message length:" + maxlen);
        et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxlen) });

        LocationManager lm = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria();
        String provider = lm.getBestProvider(c, false);
        l = lm.getLastKnownLocation(provider);
        if (l != null) {
            Log.i("EncodeActivity",
                    String.format("Location: Long: %f Lat: %f",
                            l.getLongitude(), l.getLatitude()));
        } else {
            Log.i("EncodeActivity", "No location found");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.encode, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        if(item.getItemId() == R.id.action_encode_next) {
            encodeText(et.getText().toString());
            saveAndContinue(containerBitmap);
            return true;
        }
        return false;
    }

    /**
     * Could probably be taken from MainActivity since they both do the same
     * thing
     * 
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
        if(et.getText().toString().length() != 0)
            encodeText(et.getText().toString());
        String fname = "toDecodeBitmap.png";
        saveBitmap(containerBitmap, fname);
        intent.putExtra("bitmap", fname);
        startActivity(intent);
    }
    private String saveBitmap(Bitmap bm, String filename) {
        try {
            String filepath = filename;
            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                
                AlertDialog ad = new AlertDialog.Builder(this).create();
                ad.setTitle("Error");
                ad.setMessage("External Storage is not mounted. The image was not saved");
                ad.show();
                Log.e("EncodeActivity","Could not save image as "+filename);
                return null;
            }
            String fname =Environment.getExternalStorageDirectory()+"/"+  filepath; 
            FileOutputStream fos = new FileOutputStream(fname);
            containerBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return fname;
        } catch (FileNotFoundException e) {
            Log.e("EncodeActivity",e.getMessage());
        } catch (IOException e) {
            Log.e("EncodeActivity",e.getMessage());
        }
        return null;
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("EncodeActivity", "Activity result returned " + requestCode + " "
                + resultCode);
        if (resultCode == RESULT_OK && data != null) {
            // Save and continue
            Log.i("EncodeActivity",data.getData().toString());
            Bitmap bm = encodeImage(data.getData());
            saveAndContinue(bm);
            
        }
    }

    private boolean saveAndContinue(Bitmap bm) {
        // Save the file as a timestamp,
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String fname = saveBitmap(bm,s.format(new Date())+".png");
        
        if(fname != null) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("image/png");
            Uri imageuri = Uri.fromFile(new File(fname));
            Log.i("EncodeActivity","Image uri:"+imageuri.toString());
            i.putExtra(Intent.EXTRA_STREAM, imageuri);
            startActivity(Intent.createChooser(i, "Share Image"));
            return true;
        } 
        return false;
    }

    private Bitmap encodeImage(Uri image) {
        ImageView ive = (ImageView) findViewById(R.id.imageViewEmbed);
        ive.setImageURI(image);
        Log.i("EncodeActivity","Image:"+image.toString());

        Bitmap bitmap= null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        // Compress the smaller image so that it fits inside the container image
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        bitmap = Bitmap.createScaledBitmap(bitmap,
                Math.min(containerBitmap.getWidth() / 4, bitmap.getWidth()),
                Math.min(containerBitmap.getHeight() / 4, bitmap.getHeight()),
                false);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageData = Base64.encodeToString(baos.toByteArray(),
                Base64.DEFAULT);
       return encodeText("data:image/png;base64," +imageData);
    }

    /**
     * Embed a byte into an image at the specified position
     * 
     * @param img
     *            the Bitmap to embed into
     * @param b
     *            the byte to embed
     * @param start
     *            the starting index.
     */
    private void embedByte(Bitmap img, byte b, int start) {
        int maxX = img.getWidth(), maxY = img.getHeight(), startX = start
                / maxY, startY = start - startX * maxY, count = 0;
        for (int i = startX; i < maxX && count < 8; i++) {
            for (int j = startY; j < maxY && count < 8; j++) {
                int rgb = img.getPixel(i, j), bit = getBitValue(b, count);
                rgb = setBitValue(rgb, 0, bit);

                img.setPixel(i, j, rgb);
                count++;
            }
        }
    }

    /**
     * Embed a byte into an image at the specified position
     * 
     * @param img
     *            the Bitmap to embed into
     * @param b
     *            the integer to embed
     * @param start
     *            the starting index.
     */
    private void embedInteger(Bitmap img, int b, int start) {
        int maxX = img.getWidth(), maxY = img.getHeight(), startX = start
                / maxY, startY = start - startX * maxY, count = 0;
        for (int i = startX; i < maxX && count < 32; i++) {
            for (int j = startY; j < maxY && count < 32; j++) {
                int rgb = img.getPixel(i, j), bit = getBitValue(b, count);
                rgb = setBitValue(rgb, 0, bit); // Always store in the least
                                                // significant bit
                img.setPixel(i, j, rgb);
                count++;
            }
        }
    }

    public static int getBitValue(int b, int location) {
        int v = b & (int) Math.round(Math.pow(2, location));
        return v == 0 ? 0 : 1;
    }

    public static int setBitValue(int n, int location, int bit) {
        int toggle = (int) Math.pow(2, location);
        int bv = getBitValue(n, location);
        if (bv == bit)
            return n;
        if (bv == 0 && bit == 1)
            n |= toggle;
        else if (bv == 1 && bit == 0)
            n ^= toggle;
        return n;
    }

    /**
     * Embed the string message into the container image Adapted from
     * http://developeriq
     * .in/articles/2013/feb/28/embedding-messages-in-digital-images-using-java/
     * 
     * @param message
     *            the message to be encoded into the string
     */

    private Bitmap encodeText(String message) {
        Log.i("EncodeActivity", "encoding message: (" + message.length() + ") "
                + message);
        // put the 3 chars at the start
        byte[] b = "!@#".getBytes();
        for (int i = 0; i < b.length; i++) {
            embedByte(containerBitmap, b[i], i * 8);
        }
        
        // put the real message in
        CheckBox cb = (CheckBox) findViewById(R.id.checkBoxCoords);
        if (cb.isChecked() && l != null) {
            message = String.format("##%f,%f##", l.getLatitude(),
                    l.getLongitude())
                    + message;
        }
     // put the message length in
        embedInteger(containerBitmap, message.length(), 24);
        b = message.getBytes();
        for (int i = 0; i < b.length; i++) {
            embedByte(containerBitmap, b[i], (i * 8) + (24 + 32));
        }
        return containerBitmap;
    }
}
