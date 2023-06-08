package com.gba.facy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button button;
    ImageView imageView;
    TextView textView;

    private final static int REQUEST_IMAGE_CAPTURE = 124;
    InputImage firabaseVision;
    FaceDetector faceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        FirebaseApp.initializeApp(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenFile();
            }
        });
        Toast.makeText(this, "App is startted", Toast.LENGTH_SHORT).show();
    }

    private void OpenFile() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Failed !!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle bundle = data.getExtras();
        Bitmap bitmap = (Bitmap)bundle.get("data");

        FaceDetectionProcess(bitmap);
        Toast.makeText(this, "Success !!!", Toast.LENGTH_SHORT).show();

    }

    private void FaceDetectionProcess(Bitmap bitmap) {
        textView.setText("Processing image ...");

        final StringBuilder stringBuilder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        FaceDetectorOptions highAccuracyOpt = new FaceDetectorOptions
                .Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking().build();

        FaceDetector detector = FaceDetection.getClient(highAccuracyOpt);
        Task<List<Face>> result = detector.process(image);

        result.addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(List<Face> faces) {
                if (faces.size() != 0) {
                    if (faces.size() == 1) {
                        stringBuilder.append(faces.size()).append(" face detected.\n\n");
                    } else {
                        stringBuilder.append(faces.size()).append(" faces detected.\n\n");
                    }
                }

                for (Face face: faces) {
                    int id = face.getTrackingId();
                    float rotY = face.getHeadEulerAngleY();
                    float rotZ = face.getHeadEulerAngleZ();

                    stringBuilder.append("1. Face tracking ID: " + id + "\n");
                    stringBuilder.append("2. Head Rotation to Right: " + rotY + "\n");
                    stringBuilder.append("3. Head Rotation to Down: " + rotZ + "\n");
                    stringBuilder.append("4. Smiling probability: " + face.getSmilingProbability() + "\n");
                    stringBuilder.append("5. Left eye open probability: " + face.getLeftEyeOpenProbability() + "\n");
                    stringBuilder.append("6. Right eye open probability: " + face.getRightEyeOpenProbability() + "\n");
                    stringBuilder.append("\n");


                }
                ShowDetection("Face Detection", stringBuilder, true);
            }

        });
        result.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                StringBuilder stringBuilder1 = new StringBuilder();
                stringBuilder1.append("Sorry!!! There was an error.");
                ShowDetection("Face Detection", stringBuilder1, false);
            }
        });
    }

    private void ShowDetection(String title, StringBuilder stringBuilder, boolean success) {
        if (success) {
            textView.setText(null);
            textView.setMovementMethod(new ScrollingMovementMethod());

            if (stringBuilder.length() != 0) {
                textView.append(stringBuilder);

                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(title, stringBuilder);
                        clipboardManager.setPrimaryClip(clip);
                        return true;
                    }
                });
            } else {
                textView.setText("Failed !!!");
            }
        }
    }
}