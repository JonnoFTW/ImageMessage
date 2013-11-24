package com.jonathanmackenzie.imagemessage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera.Size;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class DecodeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);
        // Show the Up button in the action bar.
        setupActionBar();
        Intent intent = getIntent();
        ImageView iv = (ImageView) findViewById(R.id.imageViewInput);
        String content = null;
        if (intent.hasExtra("bitmap")) {

            try {
                FileInputStream fis;
                fis = openFileInput(intent.getStringExtra("bitmap"));
                Bitmap bm = BitmapFactory.decodeStream(fis);
                iv.setImageBitmap(bm);
                content = decodeImage(bm);
            } catch (FileNotFoundException e) {
                Log.e("DecodeActivity", e.getMessage());
            }

        } else {
            try {
                Bitmap bm = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(), intent.getData());
                iv.setImageBitmap(bm);
                content = decodeImage(bm);
            } catch (FileNotFoundException e) {
                Log.e("DecodeActivity", e.getMessage());
            } catch (IOException e) {
                Log.e("DecodeActivity", e.getMessage());
            }
        }
        LinearLayout layout = (LinearLayout) findViewById(R.id.decodeOutput);
        if (content != null) {
            // Detect if the string is either
            boolean image = content.startsWith("data:image/");
            if (image) {
                // Put the hidden image below
                ImageView ivout = new ImageView(this);
                byte[] decodedString = Base64.decode(content, Base64.DEFAULT);
                iv.setImageBitmap(BitmapFactory.decodeByteArray(decodedString,
                        0, decodedString.length));
                layout.addView(ivout);
            } else {
                // Put the text message in
                Log.i("Decode", "using message " + content);
                TextView tv = new TextView(this);
                tv.setText(content);
                layout.addView(tv);
            }
        } else {
            TextView tv = new TextView(this);
            tv.setText("No hidden message found");
            layout.addView(tv);
        }

    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.decode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Take the image and return a string. Taken from
     * http://developeriq.in/articles
     * /2013/feb/28/embedding-messages-in-digital-images-using-java/
     * 
     * @return
     */
    private static String decodeImage(Bitmap bm) {
        // return null if it doesn't start with !@#
        // Extract the first 3 chars, check if they are "!@#",
        // read until the next #, this is the length of the message

        byte[] b = new byte[3];
        for (int i = 0; i < b.length; i++) {
            b[i] = extractByte(bm, i * 8);
        }
        String start = new String(b);
        if (start.equals("!@#")) {
            int len = extractInteger(bm, 24);
            Log.i("DecodeActivity","Length: "+len);
            byte[] bytes = new byte[len+7];
            for (int i = 0; i < len; i++) {
                bytes[i] = extractByte(bm, (i*8) + (24+32));
            }
            String out = new String(bytes);
            Log.i("DecodeActivity", "Message=" + out);
            return out;
        } else {
            Log.i("DecodeActivity", "No message found");
            return null;
        }
    }

    /**
     * 
     * @param bm
     * @param start
     * @return
     */
    private static int extractInteger(Bitmap bm, int start) {
        int maxX = bm.getWidth(), maxY = bm.getHeight(), startX = start / maxY, startY = start
                - startX * maxY, count = 0;
        int length = 0;
        for (int i = startX; i < maxX && count < 32; i++) {
            for (int j = startY; j < maxY && count < 32; j++) {
                int rgb = bm.getPixel(i, j),
                    bit = EncodeActivity.getBitValue(rgb, 0);
                length = EncodeActivity.setBitValue(length, count, bit);
                count++;
            }
        }
        return length;
    }

    /**
     * 
     * @param bm
     * @param start
     * @return
     */
    private static byte extractByte(Bitmap bm, int start) {
        int maxX = bm.getWidth(),
                maxY = bm.getHeight(),
                startX = start / maxY,
                startY = start - startX * maxY,
                count = 0;
        byte b = 0;
        for (int i = startX; i < maxX && count < 8; i++) {
            for (int j = startY; j < maxY && count < 8; j++) {
                int rgb = bm.getPixel(i, j),
                    bit = EncodeActivity.getBitValue(rgb, 0);
                b = (byte) EncodeActivity.setBitValue(b, count, bit);
                count++;
            }
        }
        return b;
    }

}
