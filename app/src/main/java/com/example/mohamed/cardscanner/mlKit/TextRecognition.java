package com.example.mohamed.cardscanner.mlKit;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mohamed.cardscanner.graphic.GraphicOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;

public class TextRecognition {
    private static final String TAG = "TextRecognision" + "TAGG";

    private ImageView capturedImageView;
    private Bitmap capturedImage;
    private GraphicOverlay mGraphicOverlay;
    // Max width (portrait mode)
    private Integer mImageMaxWidth;
    // Max height (portrait mode)
    private Integer mImageMaxHeight;

    private Context mContext;
    private OnCardNumberDetectedListener mListener;
    private String cardNumber = "";

    public TextRecognition(Context mContext, Bitmap capturedImage, OnCardNumberDetectedListener mListener){
        this.mContext = mContext;
        this.capturedImage = capturedImage;
        this.mListener = mListener;
    }

    public void runTextRecognition() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(capturedImage);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        detector.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                //processTextRecognitionResult(texts);
                                new OCRTask().execute(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                e.printStackTrace();
                            }
                        });
    }

    private boolean processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            //mListener.onNoNumberFound();
            return false;
        }
        //mGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                //List<FirebaseVisionText.Element> elements = lines.get(j).getElements();

                Log.v(TAG+"char", "Line #" + j+ " : " + lines.get(j).getText());

                if(isValidCardNumber(lines.get(j).getText())){
                    Log.v(TAG, "card number is: "+ cardNumber);
                    //mListener.onNumberDetection(cardNumber);
                    break;
                    //return true;
                }

            }
        }
        // if no valid number was found
        if(cardNumber.equals("")){
            //mListener.onNoNumberFound();
            return false;
        }else {
            return true;
        }
    }

    private boolean isValidCardNumber(String num){
        int totalDigits = 0 , totalSpaces = 0;
        if(num != null) {
            for (int i = 0; i < num.length(); i++) {
                if (Character.isDigit(num.charAt(i))) {
                    totalDigits++;
                } else if (num.charAt(i) == ' ') {
                    totalSpaces++;
                }
            }

            Log.v(TAG, "total digits: " + totalDigits + ", total spaces: " + totalSpaces);

            if ((totalDigits == 16 || totalDigits == 14) && totalSpaces > 1) {
                num = num.replaceAll("\\s", "");
                Log.v(TAG, "Num with no spaces: " + num);
                cardNumber = num;
                return true;
            }
        }
        return false;
    }


    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    // Functions for loading images from app assets.

    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxWidth() {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = capturedImageView.getWidth();
        }

        return mImageMaxWidth;
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxHeight() {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight =
                    capturedImageView.getHeight();
        }

        return mImageMaxHeight;
    }

    // Gets the targeted width / height.
    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }



    public interface OnCardNumberDetectedListener{
        void onNumberDetection(String cardNumber);
        void onNoNumberFound();
    }



    private class OCRTask extends AsyncTask<FirebaseVisionText, Void, Boolean> {
        private FirebaseVisionText Texts;


        @Override
        protected Boolean doInBackground(FirebaseVisionText... Texts) {
            return processTextRecognitionResult(Texts[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                mListener.onNumberDetection(cardNumber);
                Log.v("TAGG", "found");
            }else{
                mListener.onNoNumberFound();
                Log.v("TAGG", "not found");
            }
        }
    }
}
