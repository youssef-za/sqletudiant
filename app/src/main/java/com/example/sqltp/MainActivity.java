package com.example.sqltp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.sqltp.classes.Etudiant;
import com.example.sqltp.service.EtudiantService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText nom, prenom, dateNaissance, idEtudiant;
    private Button valider, chercher, supprimer, lister, btnSelectPhoto;
    private TextView res;
    private ImageView imageViewSelectedPhoto;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 100;
    private static final int PERMISSION_REQUEST_STORAGE = 101;

    private String currentPhotoPath;
    private byte[] photoData;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        dateNaissance = findViewById(R.id.datenaissance);
        idEtudiant = findViewById(R.id.idetudiant);
        valider = findViewById(R.id.valider);
        chercher = findViewById(R.id.chercher);
        supprimer = findViewById(R.id.supprimer);
        lister = findViewById(R.id.lister);
        res = findViewById(R.id.res);
        imageViewSelectedPhoto = findViewById(R.id.imageViewSelectedPhoto);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);


        calendar = Calendar.getInstance();
        final EtudiantService es = new EtudiantService(this);

        // Date picker setup
        dateNaissance.setFocusable(false);
        dateNaissance.setOnClickListener(v -> showDatePicker());

        // Photo buttons setup
        btnSelectPhoto.setOnClickListener(v -> checkStoragePermissionAndOpenGallery());

        // Main buttons setup
        valider.setOnClickListener(v -> {
            String nomText = nom.getText().toString().trim();
            String prenomText = prenom.getText().toString().trim();
            String dateText = dateNaissance.getText().toString().trim();

            if (validateInputs(nomText, prenomText, dateText)) {
                Etudiant nouvelEtudiant = new Etudiant(nomText, prenomText, dateText);
                if (photoData != null) {
                    nouvelEtudiant.setPhoto(photoData);
                }

                long id = es.create(nouvelEtudiant);
                if (id != -1) {
                    Toast.makeText(this, "Étudiant ajouté avec ID: " + id, Toast.LENGTH_SHORT).show();

                    clearFields();
                } else {
                    Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                }
            }
        });


        chercher.setOnClickListener(v -> {
            String idText = idEtudiant.getText().toString().trim();
            if (idText.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int id = Integer.parseInt(idText);
                Etudiant e = es.findById(id);
                if (e != null) {
                    displayStudentInfo(e);
                    Toast.makeText(this, "Étudiant trouvé: " + e.getNom(), Toast.LENGTH_SHORT).show();
                } else {
                    res.setText("Étudiant non trouvé");
                    clearImage();
                    Toast.makeText(this, "Aucun étudiant avec cet ID", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "ID invalide", Toast.LENGTH_SHORT).show();
            }
        });

        supprimer.setOnClickListener(v -> {
            String idText = idEtudiant.getText().toString().trim();
            if (idText.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                return;
            }

            int id = Integer.parseInt(idText);
            es.delete(id);
            Toast.makeText(this, "Étudiant supprimé", Toast.LENGTH_SHORT).show();
            clearFields();
        });

        lister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Liste des étudiants", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInputs(String nom, String prenom, String dateNaissance) {
        if (nom.isEmpty() || prenom.isEmpty() || dateNaissance.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateEditText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateEditText() {
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.FRANCE);
        dateNaissance.setText(sdf.format(calendar.getTime()));
    }

    private void checkCameraPermissionAndTakePicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void checkStoragePermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void displayStudentInfo(Etudiant e) {
        nom.setText(e.getNom());
        prenom.setText(e.getPrenom());
        dateNaissance.setText(e.getDateNaissance());
        res.setText(e.getNom() + " " + e.getPrenom());

        if (e.getPhoto() != null && e.getPhoto().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(e.getPhoto(), 0, e.getPhoto().length);
            imageViewSelectedPhoto.setImageBitmap(bitmap);
            photoData = e.getPhoto();
        } else {
            clearImage();
        }
    }

    private void clearFields() {
        nom.setText("");
        prenom.setText("");
        dateNaissance.setText("");
        idEtudiant.setText("");
        res.setText("");
        clearImage();
    }

    private void clearImage() {
        imageViewSelectedPhoto.setImageResource(R.mipmap.homme);
        photoData = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                processImageBitmap(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                try {
                    Uri selectedImageUri = data.getData();
                    InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    processImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void processImageBitmap(Bitmap bitmap) {
        int maxDimension = 1024;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > maxDimension || height > maxDimension) {
            float ratio = (float) width / height;
            if (width > height) {
                width = maxDimension;
                height = (int) (width / ratio);
            } else {
                height = maxDimension;
                width = (int) (height * ratio);
            }
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        photoData = stream.toByteArray();
        imageViewSelectedPhoto.setImageBitmap(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permission de caméra requise", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission de stockage requise", Toast.LENGTH_SHORT).show();
            }
        }
    }
}