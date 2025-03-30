package com.example.sqltp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sqltp.classes.Etudiant;
import com.example.sqltp.service.EtudiantService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder>{
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private Context context;
    private List<Etudiant> etudiants;
    private EtudiantService etudiantService;
    private Calendar calendar;
    private byte[] modifiedPhotoData;
    private AlertDialog currentDialog;
    public EtudiantAdapter(Context context, List<Etudiant> etudiants) {
        this.context = context;
        this.etudiants = etudiants;
        this.etudiantService = new EtudiantService(context);
        this.calendar = Calendar.getInstance();
    }
    public EtudiantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.etudiant_item, parent, false);
        return new EtudiantViewHolder(view);
    }
    public void onBindViewHolder( EtudiantViewHolder holder, int position) {
        Etudiant e = etudiants.get(position);
        holder.bind(e);
        holder.itemView.setOnClickListener(v -> showOptionsDialog(e, position));
    }
    private void showOptionsDialog(Etudiant etudiant, int position) {
        String[] options = {"Modify", "Delete"};
        new AlertDialog.Builder(context)
                .setTitle("Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showModifyDialog(etudiant, position);
                    else showDeleteConfirmationDialog(etudiant, position);
                })
                .show();
    }
    private void showModifyDialog(Etudiant etudiant, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Modifier l'Etudiant");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.etudiant_popup, null);
        builder.setView(dialogView);

        // Initialisation des vues
        EditText nomInput = dialogView.findViewById(R.id.editFirstName);
        EditText prenomInput = dialogView.findViewById(R.id.editLastName);
        EditText dateNaissanceInput = dialogView.findViewById(R.id.editBirthDate);
        ImageView imageEtudiant = dialogView.findViewById(R.id.imageStudent);
        Button btnSelectPhoto = dialogView.findViewById(R.id.btnSelectPhoto);


        // Remplissage des champs
        nomInput.setText(etudiant.getNom());
        prenomInput.setText(etudiant.getPrenom());
        dateNaissanceInput.setText(etudiant.getDateNaissance());

        // Gestion de la photo
        modifiedPhotoData = etudiant.getPhoto();
        if (modifiedPhotoData != null && modifiedPhotoData.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(modifiedPhotoData, 0, modifiedPhotoData.length);
            imageEtudiant.setImageBitmap(bitmap);
        } else {
            imageEtudiant.setImageResource(R.drawable.ic_launcher_background);
        }

        // Gestion de date
        dateNaissanceInput.setFocusable(false);
        dateNaissanceInput.setOnClickListener(v -> showDatePickerDialog(dateNaissanceInput));

        // Gestion de photo
        btnSelectPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            ((Activity) context).startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
        });



        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String newNom = nomInput.getText().toString().trim();
            String newPrenom = prenomInput.getText().toString().trim();
            String newDateNaissance = dateNaissanceInput.getText().toString().trim();

            if (validateInputs(newNom, newPrenom, newDateNaissance)) {
                updateEtudiant(etudiant, position, newNom, newPrenom, newDateNaissance);
            }
        });

        builder.setNegativeButton("Cancel", null);
        currentDialog = builder.create();
        currentDialog.show();
    }
    private boolean validateInputs(String nom, String prenom, String dateNaissance) {
        if (nom.isEmpty() || prenom.isEmpty() || dateNaissance.isEmpty()) {
            Toast.makeText(context, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void updateEtudiant(Etudiant etudiant, int position, String nom, String prenom, String dateNaissance) {
        etudiant.setNom(nom);
        etudiant.setPrenom(prenom);
        etudiant.setDateNaissance(dateNaissance);
        etudiant.setPhoto(modifiedPhotoData);

        etudiantService.update(etudiant);
        etudiants.set(position, etudiant);
        notifyItemChanged(position);
        Toast.makeText(context, "Étudiant modifié Avec Succés", Toast.LENGTH_SHORT).show();
    }
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && currentDialog != null) {
            ImageView imageEtudiant = currentDialog.findViewById(R.id.imageStudent);
            if (imageEtudiant == null) return;

            try {
                Bitmap bitmap = null;
                if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                    Uri selectedImageUri = data.getData();
                    InputStream imageStream = context.getContentResolver().openInputStream(selectedImageUri);
                    bitmap = BitmapFactory.decodeStream(imageStream);
                } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                    Bundle extras = data.getExtras();
                    bitmap = (Bitmap) extras.get("data");
                }

                if (bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                    modifiedPhotoData = stream.toByteArray();
                    imageEtudiant.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Image Processing Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showDatePickerDialog(EditText dateInput) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            dateInput.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(calendar.getTime()));
        };

        try {
            String existingDate = dateInput.getText().toString();
            if (!existingDate.isEmpty()) {
                calendar.setTime(new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).parse(existingDate));
            }
        } catch (Exception e) {
            calendar = Calendar.getInstance();
        }

        new DatePickerDialog(context, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void showDeleteConfirmationDialog(Etudiant etudiant, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmer ")
                .setMessage("Confirmer La Suppression de L'Etudiant ")
                .setPositiveButton("Oui", (dialog, which) -> deleteEtudiant(etudiant, position))
                .setNegativeButton("Non", null)
                .show();
    }
    private void deleteEtudiant(Etudiant etudiant, int position) {
        etudiantService.delete(etudiant);
        etudiants.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, etudiants.size());
        Toast.makeText(context, "Étudiant supprimé", Toast.LENGTH_SHORT).show();
    }
    public int getItemCount() {
        return etudiants.size();
    }
    public static class EtudiantViewHolder extends RecyclerView.ViewHolder {
        private final TextView etudiantId, etudiantName, etudiantBirthDate;
        private final de.hdodenhof.circleimageview.CircleImageView etudiantPhoto;
        private final Context context; // Add context reference

        public EtudiantViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            etudiantId = itemView.findViewById(R.id.tvId);
            etudiantName = itemView.findViewById(R.id.tvName);
            etudiantBirthDate = itemView.findViewById(R.id.tvBirthDate);
            etudiantPhoto = itemView.findViewById(R.id.img);
            etudiantPhoto.setContentDescription(context.getString(R.string.default_photo_description));
        }

        public void bind(Etudiant e) {
            etudiantId.setText(context.getString(R.string.student_id, e.getId()));
            etudiantName.setText(context.getString(R.string.student_name, e.getNom(), e.getPrenom()));
            etudiantBirthDate.setText(context.getString(R.string.student_birth_date, e.getDateNaissance()));

            if (e.getPhoto() != null && e.getPhoto().length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(e.getPhoto(), 0, e.getPhoto().length);
                etudiantPhoto.setImageBitmap(bitmap);
            } else {
                etudiantPhoto.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }

}
