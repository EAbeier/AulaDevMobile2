package com.example.meuapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meuapp.R;
import com.example.meuapp.data.connection.ApiService;
import com.example.meuapp.data.connection.response.FilmesResult;
import com.example.meuapp.data.mapper.FilmeMapper;
import com.example.meuapp.data.model.Filme;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaFilmesActivity extends AppCompatActivity implements SensorEventListener,
        ListaFilmesAdapter.ItemFilmeClickListener {
    SensorManager sensorManager;
    Sensor sensor;
    long TempoEvento;
    FirebaseAuth  firebaseAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    String userid;
    List<Filme> listaFilmesFirebase= new ArrayList<>();
    DocumentReference mDocRef = FirebaseFirestore.getInstance().document("filmes/favoritados");
    private static final int SOLICITAR_PERMISSAO = 1;
    private ListaFilmesAdapter FilmeAdapter1 = new ListaFilmesAdapter(this);
    private ListaFilmesAdapter FilmeAdapter2 = new ListaFilmesAdapter(this);
    private ListaFilmesAdapter FilmeAdapter3 = new ListaFilmesAdapter(this);
    RecyclerView rv1;
    RecyclerView rv2;
    RecyclerView rv3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_filmes);



        obtemFilmes();

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(sensor == null){
            Log.e("Sensor","Sensor nao encontrado!" );
        }
    }
    @Override
    protected  void onResume(){
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }



    public void configuraAdapter() {
        rv1 = findViewById(R.id.rv_populares);
        rv2 = findViewById(R.id.rv_reproduzindo);
        rv3 = findViewById(R.id.rv_bem_avaliados);

        RecyclerView.LayoutManager linearLayoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView.LayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView.LayoutManager linearLayoutManager3 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv1.setLayoutManager(linearLayoutManager1);
        rv2.setLayoutManager(linearLayoutManager2);
        rv3.setLayoutManager(linearLayoutManager3);
        rv1.setAdapter(FilmeAdapter1);
        rv2.setAdapter(FilmeAdapter2);
        rv3.setAdapter(FilmeAdapter3);
    }

    private void obtemFilmes() {
        firebaseAuth = FirebaseAuth.getInstance();
        userid = firebaseAuth.getUid();
        String  userCompara = userid;
        db.collection("Filmes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map filmeMapFire =  document.getData();
                                String idFilme  = (String)filmeMapFire.get("idFilme");
                                String tituloFilme  = (String)filmeMapFire.get("tituloFilme");
                                String userSalvo  = (String)filmeMapFire.get("user");
                                String caminhoSalvo  = (String)filmeMapFire.get("caminhoPoster");
                                String descricao = "";
                                if(userCompara.equals(userSalvo)){
                                    Filme filme = new Filme(tituloFilme, caminhoSalvo, idFilme, descricao);
                                    listaFilmesFirebase.add(filme);
                                }else{
                                    Toast.makeText(ListaFilmesActivity.this, "deuRuim", Toast.LENGTH_SHORT).show();
                                }


                            }
                        } else {
                            Log.w("erro", "Error getting documents.", task.getException());
                        }
                    }
                });
        if(listaFilmesFirebase.isEmpty()) {
            ApiService.getInstance()
                    .FilmesPopulares("799a1f0649735842ab24e00e80ad2b30", "pt-BR")
                    .enqueue(new Callback<FilmesResult>() {
                        @Override
                        public void onResponse(@NotNull Call<FilmesResult> call, @NotNull Response<FilmesResult> response) {
                            if (response.isSuccessful()) {
                                final List<Filme> listaFilmes = FilmeMapper
                                        .responseToDomain(response.body().getResultados());
                                FilmeAdapter1.setFilmes(listaFilmes);
                                ;

                                configuraAdapter();
                            } else {
                                mostraErro();
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<FilmesResult> call, Throwable t) {
                            mostraErro();
                        }
                    });
        }else{
            FilmeAdapter1.setFilmes(listaFilmesFirebase);
            configuraAdapter();
        }
        ApiService.getInstance()
                .FilmesReproduzidos( "799a1f0649735842ab24e00e80ad2b30", "pt-BR")
                .enqueue(new Callback<FilmesResult>() {
                    @Override
                    public void onResponse(@NotNull Call<FilmesResult> call, @NotNull Response<FilmesResult> response) {
                        if (response.isSuccessful()) {
                            final List<Filme> listaFilmes = FilmeMapper
                                    .responseToDomain(response.body().getResultados());

                            FilmeAdapter2.setFilmes(listaFilmes);

                            configuraAdapter();
                        } else {
                            mostraErro();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<FilmesResult> call, Throwable t) {
                        mostraErro();
                    }
                });
        ApiService.getInstance()
                .FilmesBemAvaliados("799a1f0649735842ab24e00e80ad2b30", "pt-BR")
                .enqueue(new Callback<FilmesResult>() {
                    @Override
                    public void onResponse(@NotNull Call<FilmesResult> call, @NotNull Response<FilmesResult> response) {
                        if (response.isSuccessful()) {
                            final List<Filme> listaFilmes = FilmeMapper
                                    .responseToDomain(response.body().getResultados());

                            FilmeAdapter3.setFilmes(listaFilmes);

                            configuraAdapter();
                        } else {
                            mostraErro();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<FilmesResult> call, Throwable t) {
                        mostraErro();
                    }
                });

    }

    private void mostraErro() {
        Toast.makeText(this, "Falha na comunicaçao da api", Toast.LENGTH_SHORT)
                .show();
    }
    public void Like(View view){
        TextView id = ((View) view.getParent()).findViewById(R.id.txt_id);
        TextView posterpath = ((View) view.getParent()).findViewById(R.id.txtPosterPath);
        TextView titulo = ((View) view.getParent()).findViewById(R.id.txt_titulo_filme);
        firebaseAuth = FirebaseAuth.getInstance();
        userid = firebaseAuth.getUid();
        String  user = userid;
        String txtId = id.getText().toString();
        String txtPosterPath = posterpath.getText().toString();
        String txtTitulo = titulo.getText().toString();
        if(userid.isEmpty() || txtId.isEmpty()
                || txtPosterPath.isEmpty()||txtTitulo.isEmpty()){return;}
        Map<String, Object> dataToSave = new HashMap<String, Object>();
        dataToSave.put("user",user);
        dataToSave.put("idFilme",txtId);
        dataToSave.put("caminhoPoster",txtPosterPath);
        dataToSave.put("tituloFilme",txtTitulo);

        db.collection("Filmes")
                .add(dataToSave)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("sucess", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("deu ruim", "Error adding document", e);
                    }
                });

    }
    public void Share(View view) {
        String id;

        //TextView tv = (TextView) view.findViewById(R.id.tv_ml);
        //tv.getText().toString();
        TextView idText = ((View) view.getParent()).findViewById(R.id.txt_id);
        ImageView imageView = ((View) view.getParent()).findViewById(R.id.poster_filme);
        id = idText.getText().toString();

        System.out.println(id);
        String mensagem = "Se liga nesse filme http://filmes.uniritter.edu.br/filme?id="+id+"&de=Emerson";
        checarPermissao(mensagem, imageView);



    }
    private void checarPermissao(String msg, ImageView img) {

        // Verifica  o estado da permissão de WRITE_EXTERNAL_STORAGE
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);


        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Se for diferente de PERMISSION_GRANTED, então vamos exibir a tela padrão
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, SOLICITAR_PERMISSAO);
        } else {
            // Senão vamos compartilhar a imagem
            enviarWhatsApp(msg, img);
        }

    }

    public void enviarWhatsApp(String mensagem, ImageView image) {
        PackageManager pm = getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("*/*");
            String text = mensagem;
            BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
            Bitmap b = drawable.getBitmap();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), b, "shareImage", null);
            Uri uri = Uri.parse(path);

            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            waIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(waIntent);

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp não instalado", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
            if(event.sensor == sensor && event.timestamp - TempoEvento>2000){
                String d = "";
                float x,y,z;
                x= event.values[0];
                y= event.values[1];
                z= event.values[2];
                for(float f : event.values){
                    d+=f+", ";
                }
                if(y>2){
                    Log.d("Sensor1", d);
                    rv1.scrollBy(300,0);
                }
                    if(y<2 && y!=0){
                        if(rv1 != null){

                            Log.d("Sensor2", d);
                            rv1.scrollBy(-300,0);
                        }
                    }



                if(z>2){

                    Log.d("Sensor3", d);
                    rv2.scrollBy(300,0);
                }

                    if(z<2 && z!=0){
                            if(rv2!=null){
                            Log.d("Sensor4", d);
                            rv2.scrollBy(-300,0);

                            }
                    }



            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onItemFilmeClicado(Filme filme) {
        Intent intent = new Intent(this, FilmeInfo.class);
        intent.putExtra(FilmeInfo.EXTRA_FILME, filme);
        startActivity(intent);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }

}


