package com.example.mohamed.cardscanner.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.UniversalTimeScale;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mohamed.cardscanner.R;
import com.example.mohamed.cardscanner.SettingsActivity;
import com.example.mohamed.cardscanner.utils.PermissionsUtilities;
import com.example.mohamed.cardscanner.mlKit.TextRecognition;
import com.example.mohamed.cardscanner.utils.PreferenceUtilities;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements TextRecognition.OnCardNumberDetectedListener {

    private final static String TAG = "MainActivity" + "TAGG";


    // TODO: Remove LOGS
    // TODO: Make resource files (styles) + set theme colors
    // TODO: Add on resume activity

    private String cardNumber = "";

    // UI components
    private View mainLayout;
    private Button startBtn;
    private Button newScanBtn;
    private ImageView capturedImageView;
    private Bitmap capturedImage;
    private TextView msgTextView;
    private ProgressBar loadingSpinner;
    private Boolean goNext;


    private static final int GROUP_REQUEST_NUM = 0;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static String[] PERMISSIONS_GROUP = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // path to the captured photo
    String mCurrentPhotoPath;

    File photoFile = null;

    // preferences
    private PreferenceUtilities prefUtils;
    private static final boolean PREF_SAVE_IMG_ENABLED = true;
    private static final boolean PREF_SAVE_IMG_DISABLED = false;
    private static final int PREF_FONT_SIZE_NORMAL = 18;
    private static final int PREF_FONT_SIZE_LARGE = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting the Toolbar
        Toolbar mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);

        // init
        goNext = false;
        prefUtils = new PreferenceUtilities(this);
        capturedImageView = findViewById(R.id.image_view);
        mainLayout = findViewById(R.id.main_layout);
        msgTextView = findViewById(R.id.message_tv);
        startBtn = findViewById(R.id.btn_start);
        newScanBtn = findViewById(R.id.btn_new_scan);
        loadingSpinner = findViewById(R.id.loading_spinner);



        // load prefs
        loadPreferences();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //goToOperatorActivity();

                //Toast.makeText(MainActivity.this, "save image: " + prefUtils.getPrefSaveImg() + ", font size: " + prefUtils.getPrefFontSize(), Toast.LENGTH_SHORT).show();
                if(!goNext){
                    startCamera();

                }else{

                    goToOperatorActivity();
                }

            }
        });

        newScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera();
                // reset
                hideNewScanBtn();
                startBtn.setText(getString(R.string.start_btn));
                goNext = false;
            }
        });
    }

    @Override
    protected void onResume() {
        loadPreferences();
        super.onResume();
    }

    private void loadPreferences(){
        if(prefUtils.getPrefFontSize().equals("0")){
            msgTextView.setTextSize(PREF_FONT_SIZE_NORMAL);
            startBtn.setTextSize(PREF_FONT_SIZE_NORMAL);
            newScanBtn.setTextSize(PREF_FONT_SIZE_NORMAL);
        }else if(prefUtils.getPrefFontSize().equals("1")){
            msgTextView.setTextSize(PREF_FONT_SIZE_LARGE);
            startBtn.setTextSize(PREF_FONT_SIZE_LARGE);
            newScanBtn.setTextSize(PREF_FONT_SIZE_LARGE);
        }
    }

    // inflating toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startCamera() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission are not allowed, asking for it now.");
            requestPermissions();
        } else {
            Log.v(TAG, "Permission are allowed, starting camera.");
            dispatchTakePictureIntent();
        }
    }

    private void goToOperatorActivity(){
        //
        // deleteImageFile();
        Intent operatorIntent = new Intent(this, OperatorActivity.class);
        operatorIntent.putExtra("cardNumber", cardNumber);
        startActivity(operatorIntent);
    }

    // permissions
    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Log.v(TAG, "second time to ask.");

            Snackbar.make(mainLayout, R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.allow, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_GROUP,
                                    GROUP_REQUEST_NUM);
                        }
                    })
                    .show();
        } else {
            Log.v(TAG, "First time to ask.");
            ActivityCompat.requestPermissions(this, PERMISSIONS_GROUP, GROUP_REQUEST_NUM);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Log.v(TAG, "inside on request result.");
        if (requestCode == GROUP_REQUEST_NUM) {

            if (PermissionsUtilities.verifyPermissions(grantResults)) {

                Snackbar.make(mainLayout, R.string.permissions_granted,
                        Snackbar.LENGTH_SHORT).addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);

                        //dispatchTakePictureIntent();
                    }
                }).show();

            } else {

                Snackbar.make(mainLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.mohamed.cardscanner",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
            //startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }


    // make image public to other apps

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    // decode the image
    private void setPic() {
        try {
            // Get the dimensions of the View
            int targetW = capturedImageView.getWidth();
            int targetH = capturedImageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            //BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Log.v(TAG, "setting image of: " + mCurrentPhotoPath);

            capturedImage = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);


            //capturedImage = BitmapFactory.decodeResource(getResources(),R.drawable.tiger);


            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(capturedImage, 168, 300);

            capturedImageView.setImageBitmap(ThumbImage);

        }catch (Exception e){

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = getFilesDir();

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.v(TAG, "Createfile, path: " + mCurrentPhotoPath);

        //galleryAddPic();

        return image;
    }

    private void deleteImageFile(){
        //File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File file = new File(mCurrentPhotoPath);
        Log.v(TAG, "delete file, path: " + mCurrentPhotoPath);
        //Log.v(TAG, "parent path: " + path);
        if(file.exists()){
            if(file.delete()){
                Log.v(TAG, "Image Deleted");
            }else{
                Log.v(TAG, "Image not deleted");
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        hideMessageText();
        showSpinner();
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            // decode image
            setPic();
            addCapturedImageShape();
            // make it public
            //galleryAddPic();
            // run OCR
            TextRecognition tr = new TextRecognition(this, capturedImage, this);
            tr.runTextRecognition();
            //Log.v(TAG, "card Number is: " + tr.getCardNumber());
            //Log.v(TAG, "rows deleted: " + this.getContentResolver().delete(data.getData(), null, null));

        } else {
            //Toast.makeText(this, R.string.no_image_received, Toast.LENGTH_SHORT).show();
            hideSpinner();
            setMessageText(getString(R.string.no_image_received));
        }

        // important! Mandatory
        deleteImageFile();
    }




    // UI changes
    private void addCapturedImageShape() {
        capturedImageView.setBackground(getResources().getDrawable(R.drawable.bg_captured_image));
    }

    private void setMessageText(String message) {
        msgTextView.setVisibility(View.VISIBLE);
        msgTextView.setText(message);
    }

    private void hideMessageText(){
        msgTextView.setVisibility(View.GONE);
    }

    @Override
    public void onNumberDetection(String cardNumber) {
        hideSpinner();
        this.cardNumber = cardNumber;
        setMessageText(getString(R.string.number_found));
        startBtn.setText(getString(R.string.next_btn));
        goNext = true;
        showNewScanBtn();
    }

    @Override
    public void onNoNumberFound() {
        hideSpinner();
        //Toast.makeText(this, R.string.number_not_found, Toast.LENGTH_SHORT).show();
        setMessageText(getString(R.string.number_not_found));
    }

    private void showSpinner(){
        loadingSpinner.setVisibility(View.VISIBLE);
    }

    private void hideSpinner(){
        loadingSpinner.setVisibility(View.GONE);
    }

    private void showNewScanBtn(){
        newScanBtn.setVisibility(View.VISIBLE);
    }

    private void hideNewScanBtn(){
        newScanBtn.setVisibility(View.GONE);
    }

}
