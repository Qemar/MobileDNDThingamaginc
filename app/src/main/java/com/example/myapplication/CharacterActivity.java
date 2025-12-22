package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CharacterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character);

        // Кнопка назад (внутри activity) — просто finish()
        Button btnBack = findViewById(R.id.btnBackToMain);
        btnBack.setOnClickListener(v -> finish());

        // Динамически добавляем фрагмент, если ещё не добавлен
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_character, new CharacterFragment())
                    .commit();
        }
    }
}
