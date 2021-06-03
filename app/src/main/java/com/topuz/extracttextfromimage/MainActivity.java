package com.topuz.extracttextfromimage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private Button buton, yazdir, dosyadanSec;
    private ImageView resim;
    private TextView yazi;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private Switch sw;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buton = findViewById(R.id.button);
        resim = findViewById(R.id.imageView);
        yazi = findViewById(R.id.textView);
        dosyadanSec = findViewById(R.id.button3);
        sw = findViewById(R.id.switch1);

        yazdir = findViewById(R.id.button2);


        sp = this.getSharedPreferences("adetKaydet", Context.MODE_PRIVATE);
        editor = sp.edit();
        boolean deger = sp.getBoolean("gecegunduz", false);
        if (!deger) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            sw.setChecked(false);
        }
        if (deger) {
            sw.setChecked(true);
        }
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    sw.setChecked(true);
                    editor.putBoolean("gecegunduz", true);
                    editor.apply();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    sw.setChecked(false);
                    editor.putBoolean("gecegunduz", false);
                    editor.apply();

                }
            }
        });

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }
        dosyadanSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                } else {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(i, "secilen resim"), 121);

                }


            }
        });


        buton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 101);
                }


            }
        });
        yazdir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                } else {


                    final View seekbar_layout = getLayoutInflater().inflate(R.layout.metingir, null);
                    final EditText metinburaya = seekbar_layout.findViewById(R.id.editTextTextPersonName);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                    builder.setTitle("Dosya Adı:");
                    final String finalMet = yazi.getText().toString();
                    ;

                    builder.setPositiveButton("Kaydet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String dosyaAdi = metinburaya.getText().toString();
                            if (dosyaAdi.equals("")) {

                                int kacinci = sp.getInt("adet", 0);
                                dosyaAdi = "adsiz" + kacinci;

                                PdfDocument pdfDocument = new PdfDocument();
                                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                                Paint paint = new Paint();
                                paint.setTextSize(13);

                                int x = 40, y = 50;

                                for (String line : finalMet.split("\n")) {
                                    page.getCanvas().drawText(line, x, y, paint);
                                    y += paint.descent() - paint.ascent();

                                }
                                pdfDocument.finishPage(page);


                                String adres = Environment.getExternalStorageDirectory().getPath() + "/" + dosyaAdi + ".pdf";
                                File dosya = new File(adres);
                                try {
                                    pdfDocument.writeTo(new FileOutputStream(dosya));
                                    int gec = kacinci + 1;
                                    editor.putInt("adet", gec);
                                    editor.apply();
                                    Toast.makeText(getApplicationContext(), dosyaAdi + ".pdf \n Dahili Hafızaya Kaydedildi", Toast.LENGTH_LONG).show();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "HATA", Toast.LENGTH_SHORT).show();
                                }
                                pdfDocument.close();


                            } else {

                                PdfDocument pdfDocument = new PdfDocument();
                                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                                Paint paint = new Paint();
                                paint.setTextSize(13);
                                int x = 40, y = 50;

                                for (String line : finalMet.split("\n")) {
                                    page.getCanvas().drawText(line, x, y, paint);
                                    y += paint.descent() - paint.ascent();

                                }
                                pdfDocument.finishPage(page);
                                String adres = Environment.getExternalStorageDirectory().getPath() + "/" + dosyaAdi + ".pdf";
                                File dosya = new File(adres);
                                try {
                                    pdfDocument.writeTo(new FileOutputStream(dosya));
                                    Toast.makeText(getApplicationContext(), dosyaAdi + ".pdf \n Dahili Hafızaya Kaydedildi", Toast.LENGTH_LONG).show();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "HATA", Toast.LENGTH_SHORT).show();
                                }
                                pdfDocument.close();

                            }


                        }
                    });
                    final AlertDialog alert_yazi = builder.create();

                    alert_yazi.setView(seekbar_layout);

                    alert_yazi.show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
            resim.setImageBitmap(bitmap);
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

            FirebaseVision firebaseVision = FirebaseVision.getInstance();

            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();

            Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(image);

            task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    String veri = firebaseVisionText.getText();
                    yazi.setText(veri);

                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            resim.setImageURI(data.getData());
            FirebaseVisionImage Resim;
            try {

                Resim = FirebaseVisionImage.fromFilePath(getApplicationContext(), data.getData());

                FirebaseVision firebaseVision = FirebaseVision.getInstance();

                FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();

                Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(Resim);

                task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        String veri = firebaseVisionText.getText();
                        yazi.setText(veri);

                    }
                });
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            } catch (Exception e) {

            }

        }


    }
}