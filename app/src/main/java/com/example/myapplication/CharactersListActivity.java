// app/src/main/java/com/example/myapplication/CharactersListActivity.java
package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.CharacterDbHelper;
import com.example.myapplication.data.CharacterDbHelper.IdName;

import java.util.List;

public class CharactersListActivity extends AppCompatActivity {

    private RecyclerView rv;
    private CharacterDbHelper dbHelper;
    private CharactersAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_characters_list);

        dbHelper = new CharacterDbHelper(this);

        rv = findViewById(R.id.rvCharacters);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CharactersAdapter(idName -> {
            // open detail
            Intent i = new Intent(CharactersListActivity.this, CharacterDetailActivity.class);
            i.putExtra("CHAR_ID", idName.id);
            startActivity(i);
        });
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // инициализация HP у старых записей
        new Thread(() -> {
            dbHelper.initializeHpDefaultsForExisting();
            runOnUiThread(this::loadList);
        }).start();
    }


    private void loadList() {
        List<IdName> list = dbHelper.getAllIdNames();
        adapter.setItems(list);
    }
}
