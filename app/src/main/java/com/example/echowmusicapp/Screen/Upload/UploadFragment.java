package com.example.echowmusicapp.Screen.Upload;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.echowmusicapp.Models.UploadSong;
import com.example.echowmusicapp.NavigationActivity;
import com.example.echowmusicapp.R;
import com.example.echowmusicapp.Screen.Auth.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    TextView textViewImage;
    ProgressBar progressBar;
    Spinner spinner;
    Uri audioUri;
    StorageReference mStorageref;
    StorageTask mUploadTask;
    DatabaseReference referenceSongs;
    String songsCategory;
    MediaMetadataRetriever metadataRetriever;
    byte[] art;
    String title1, artista1, album_art1 = "", duracion1;
    TextView title, artista, album, dataa, duracion;
    ImageView album_art;

    Button openAudioFiles, uploadFileTofirebase;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UploadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UploadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UploadFragment newInstance(String param1, String param2) {
        UploadFragment fragment = new UploadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_upload, container, false);

        textViewImage = rootView.findViewById(R.id.textViewSongsFilesSelected);
        progressBar = rootView.findViewById(R.id.progressbar);
        title = rootView.findViewById(R.id.title);
        artista = rootView.findViewById(R.id.artista);
        duracion = rootView.findViewById(R.id.duracion);
        album = rootView.findViewById(R.id.album);
        dataa = rootView.findViewById(R.id.data);
        album_art = rootView.findViewById(R.id.imageView);
        spinner = rootView.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        openAudioFiles = rootView.findViewById(R.id.openAudioFiles);
        uploadFileTofirebase = rootView.findViewById(R.id.uploadFileTofirebase);

        openAudioFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAudioFiles();
            }
        });

        uploadFileTofirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFileTofirebase();
            }
        });

        metadataRetriever = new MediaMetadataRetriever();
        referenceSongs = FirebaseDatabase.getInstance().getReference().child("songs");
        mStorageref = FirebaseStorage.getInstance().getReference().child("songs");

        List <String> categorias = new ArrayList<>();
        categorias.add("Música Romántica");
        categorias.add("Pop de los 80");
        categorias.add("Rock Alternativo");
        categorias.add("Electrónica Chillout");
        categorias.add("Hip-Hop Old School");
        categorias.add("Musica de Cumpleaños");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categorias);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {

        songsCategory = adapterView.getItemAtPosition(i).toString();
        Toast.makeText(getActivity(), "Seleccionado: "+songsCategory, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private final int REQUEST_PERMISSION_CODE = 100;
    private final ActivityResultLauncher<Intent> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            audioUri = result.getData().getData();
            assert audioUri != null;

            String fileNames = getFileName(audioUri);
            textViewImage.setText(fileNames);
        } else {
            Toast.makeText(requireContext(), "Seleccion de audio cancelada.", Toast.LENGTH_SHORT).show();
        }
    });

    public void openAudioFiles() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                getSongMetadataSelected.launch(new Intent(Intent.ACTION_GET_CONTENT).setType("*/*"));
            } else {
                requestPermissionLauncher.launch(new Intent(Intent.ACTION_GET_CONTENT).setType("*/*"));
            }
        } else {
            getSongMetadataSelected.launch(new Intent(Intent.ACTION_GET_CONTENT).setType("*/*"));
        }
    }

    ActivityResultLauncher<Intent> getSongMetadataSelected = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            audioUri = result.getData().getData();
            String fileNames = getFileName(audioUri);
            textViewImage.setText(fileNames);

            try {
                if (audioUri != null) {
                    metadataRetriever.setDataSource(requireContext(), audioUri);

                    art = metadataRetriever.getEmbeddedPicture();
                    if (art != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                        album_art.setImageBitmap(bitmap);
                    }
                    album.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                    artista.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                    dataa.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
                    duracion.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    title.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));

                    artista1 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    title1 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    duracion1 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error al obtener los metadatos del audio.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Seleccion de audio cancelada.", Toast.LENGTH_SHORT).show();
        }
    });


    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            result = uri.getLastPathSegment();
        } else {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void  uploadFileTofirebase() {
        if (textViewImage.getText().toString().isEmpty()){
            Toast.makeText(getActivity(), "Por favor seleccione una imagen", Toast.LENGTH_SHORT).show();
        }
        else {
            if (mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(getActivity(), "Carga de canciones en progreso", Toast.LENGTH_SHORT).show();
            }
            else {
                uploadFiles();
            }
        }
    }

    private void uploadFiles() {
        if (audioUri != null) {
            Toast.makeText(requireActivity(), "Cargando, por favor espera...", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference storageReference = mStorageref.child(System.currentTimeMillis() + "." + getfileextension(audioUri));
            mUploadTask = storageReference.putFile(audioUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UploadSong uploadSong = new UploadSong(songsCategory, title1, artista1, album_art1, duracion1, uri.toString());
                                    String uploadId = referenceSongs.push().getKey();

                                    // Guardar la información de la canción en la base de datos Firebase
                                    referenceSongs.child(uploadId).setValue(uploadSong)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Mostrar un diálogo informativo al finalizar la subida
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                                    builder.setTitle("Carga exitosa")
                                                            .setMessage("El archivo se ha cargado correctamente. ¿Deseas ver la lista de canciones?")
                                                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Intent intent = new Intent(requireActivity(), NavigationActivity.class);
                                                                    startActivity(intent);

                                                                    requireActivity().finish();
                                                                }
                                                            })
                                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    // Limpiar los campos de texto
                                                                    textViewImage.setText("No hay archivo seleccionado.");
                                                                    album_art.setImageBitmap(null);
                                                                    album.setText("");
                                                                    artista.setText("");
                                                                    dataa.setText("");
                                                                    duracion.setText("");
                                                                    title.setText("");

                                                                    progressBar.setProgress(0);
                                                                }
                                                            })
                                                            .show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(requireContext(), "Error al guardar la información de la canción: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(requireContext(), "Error al subir el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        } else {
            Toast.makeText(requireActivity(), "No hay archivos seleccionados", Toast.LENGTH_SHORT).show();
        }
    }

    private String getfileextension(Uri audioUri){
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(audioUri));
    }
}