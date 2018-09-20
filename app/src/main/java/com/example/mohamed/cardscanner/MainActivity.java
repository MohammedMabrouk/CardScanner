package com.example.mohamed.cardscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mohamed.cardscanner.Graphic.GraphicOverlay;
import com.example.mohamed.cardscanner.Utils.PermissionsUtilities;
import com.example.mohamed.cardscanner.MLkit.TextRecognition;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements TextRecognition.OnCardNumberDetectedListener{

    private final static String TAG = "MainActivity" + "TAGG";
    private View mainLayout;


    private Button startBtn;
    private ImageView capturedImageView;
    private Bitmap capturedImage;
    private GraphicOverlay mGraphicOverlay;


    private static final int REQUEST_GROUP = 0;
    private static final int REQUEST_STORAGE = 1;

    private static final int REQUEST_TAKE_PHOTO = 1;

    private static String[] PERMISSIONS_GROUP = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // path to the captured photo
    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        capturedImageView = findViewById(R.id.image_view);
        mainLayout = findViewById(R.id.main_layout);

        mGraphicOverlay = findViewById(R.id.graphic_overlay);

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startCamera();

            }
        });
    }

    private void startCamera() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission are not allowed, asking for it now.");
            requestPermissions();
        }else {
            Log.v(TAG, "Permission are allowed, starting camera.");
            dispatchTakePictureIntent();
        }
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
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_GROUP);
                        }
                    })
                    .show();
        } else {
            Log.v(TAG, "First time to ask.");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_GROUP);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Log.v(TAG, "inside on request result.");
        if (requestCode == REQUEST_GROUP) {

            if (PermissionsUtilities.verifyPermissions(grantResults)) {
                // All required permissions have been granted, display contacts fragment.
                Snackbar.make(mainLayout, R.string.permissions_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                // TODO: add continue to camera
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
            File photoFile = null;
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
        // Get the dimensions of the View
        int targetW = capturedImageView.getWidth();
        int targetH = capturedImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        capturedImage = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        capturedImageView.setImageBitmap(capturedImage);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            // decode image
            setPic();
            // make it public
            galleryAddPic();
            // run OCR
            TextRecognition tr = new TextRecognition(this, capturedImage, this);
            tr.runTextRecognition();
            //Log.v(TAG, "card Number is: " + tr.getCardNumber());

        }
    }


    @Override
    public void onNumberDetection(String cardNumber) {
        Toast.makeText(this, cardNumber, Toast.LENGTH_SHORT).show();
    }
}
