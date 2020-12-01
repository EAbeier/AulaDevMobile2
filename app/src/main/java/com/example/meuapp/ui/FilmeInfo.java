package com.example.meuapp.ui;

        import androidx.appcompat.app.AppCompatActivity;

        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.drawable.Drawable;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Handler;
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
        import com.example.meuapp.ui.ListaFilmesActivity;
        import com.squareup.picasso.Picasso;

        import org.jetbrains.annotations.NotNull;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.util.List;

        import retrofit2.Call;
        import retrofit2.Callback;
        import retrofit2.Response;

public class FilmeInfo extends AppCompatActivity {
    public static  final String EXTRA_FILME = "EXTRA_FILME";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Conecta();
        final Filme filme = (Filme) getIntent().getSerializableExtra(EXTRA_FILME);
        TextView txtTitulo = findViewById(R.id.id_nomefilme);
        TextView txtDesc = findViewById(R.id.descricao_filme);
        ImageView imgFilme = findViewById(R.id.poster_filme_info);
        String caminhoPoster;
        caminhoPoster = filme.getCaminhoPoster();
        txtTitulo.setText(filme.getTitulo());
        txtDesc.setText(filme.getDescricao());
        Picasso.get().load("https://image.tmdb.org/t/p/w500/"+filme.getCaminhoPoster())
                .into(imgFilme);



        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                startActivity(new Intent(getBaseContext(), ListaFilmesActivity.class));
                finish();
            }
        },5000);


    }




}