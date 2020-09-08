package com.abhi.dev.facedetect;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    ImageView imgv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
         imgv = (ImageView) findViewById(R.id.imgview);
    }

    public void init() {
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Detect();
            }
        });





    }

    private void Detect() {

        Intent pickimage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivity(pickimage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    try {
                        setUpFaceDetector(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void setUpFaceDetector(Uri selectedImage) throws FileNotFoundException{
        FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                                    .setTrackingEnabled(false).build();
        if(!faceDetector.isOperational()){
            new AlertDialog.Builder(this).setMessage("Unable to setup face detector");
            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        InputStream stream = getContentResolver().openInputStream(selectedImage);

        Bitmap bitmap = BitmapFactory.decodeStream(stream);

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faceArrays = faceDetector.detect(frame);
        Log.d("Detections","Faces = " + faceArrays.size());

        responseDetected(bitmap,faceArrays);

    }

    private void responseDetected(Bitmap bitmap, SparseArray<Face> faceArrays) {
        Paint paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);

        Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(bitmap2);
        tempCanvas.drawBitmap(bitmap2,0,0,null);

        for(int i=0;i<faceArrays.size();i++){
            Face thisFace = faceArrays.valueAt(i);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();
            tempCanvas.drawRoundRect(new RectF(x1,y1,x2,y2),2,2,paint);


        }

        imgv.setImageDrawable(new BitmapDrawable(getResources(),bitmap2));

        if(faceArrays.size() < 1){
            new AlertDialog.Builder(this).setMessage("No face in image").show();
        }else if(faceArrays.size() == 1){
            new AlertDialog.Builder(this).setMessage("One face detected in image").show();
        }else if(faceArrays.size() >1){
            new AlertDialog.Builder(this).setMessage(faceArrays.size() + "faces detected").show();
        }

    }


}
