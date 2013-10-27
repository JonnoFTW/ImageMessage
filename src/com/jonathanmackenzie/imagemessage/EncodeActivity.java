package com.jonathanmackenzie.imagemessage;

import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;

public class EncodeActivity extends Activity {

    private ImageView iv;
    private EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);
        Intent intent = getIntent();
        // Get the image uri out of the intent and stick it into the imageview
        Uri img = intent.getData();
        iv = (ImageView) findViewById(R.id.imageView);
        iv.setImageURI(img);
        et = (EditText) findViewById(R.id.editText);
        et.setHint("Your secret message...");
        et.setFilters(new InputFilter[] {new InputFilter.LengthFilter(iv.getHeight()*iv.getWidth()-10)});
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                encodeImage();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // Check if the message length exceeds what we can encode
                // Prevent adding more characters

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
     * Take the image file f and make insert the message
     * 
     * @param f
     * @param message
     *            the message to be encoded into the string
     */
    private boolean encodeImage() {
        Bitmap bitmap = iv.getDrawingCache();
        String string = et.getText().toString();
        for (int i = 0; i < 10; i++) {
            bitmap.setPixel(i, 0, i%2);
        }
        int x = 10, y = 0;
        for (char c : string.toCharArray()) {
            bitmap.setPixel(x, y, bitmap.getPixel(x, y) >> c);
            x++;
            if (y % bitmap.getWidth() == 0) {
                x = 0;
                y++;
            }
        }
        iv.invalidate();
        
        return true;
    }

}
