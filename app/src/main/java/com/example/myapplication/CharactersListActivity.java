package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.CharacterDbHelper;
import com.example.myapplication.data.CharacterDbHelper.IdName;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CharactersListActivity extends AppCompatActivity {

    private RecyclerView rv;
    private CharacterDbHelper dbHelper;
    private CharactersAdapter adapter;
    private ExecutorService exec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_characters_list);

        dbHelper = new CharacterDbHelper(this);
        exec = Executors.newSingleThreadExecutor();

        rv = findViewById(R.id.rvCharacters);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CharactersAdapter(new CharactersAdapter.Callback() {
            @Override
            public void onEdit(IdName item) {
                // открыть SaveActivity в режиме редактирования: передаём CHAR_ID
                Intent i = new Intent(CharactersListActivity.this, SaveActivity.class);
                i.putExtra("CHAR_ID", item.id);
                startActivity(i);
            }

            @Override
            public void onDelete(IdName item) {
                confirmAndDelete(item);
            }

            @Override
            public void onOpen(IdName item) {
                Intent i = new Intent(CharactersListActivity.this, CharacterDetailActivity.class);
                i.putExtra("CHAR_ID", item.id);
                startActivity(i);
            }
        });
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновим HP старых записей (если нужно) и загрузим список
        exec.execute(() -> {
            dbHelper.initializeHpDefaultsForExisting(); // безопасно, если метод есть
            runOnUiThread(this::loadList);
        });
    }

    private void loadList() {
        List<IdName> list = dbHelper.getAllIdNames();
        adapter.setItems(list);
    }

    private void confirmAndDelete(IdName item) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить персонажа")
                .setMessage("Удалить " + (item.name == null || item.name.isEmpty() ? ("#" + item.id) : item.name) + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    exec.execute(() -> {
                        dbHelper.deleteCharacter(item.id);
                        runOnUiThread(this::loadList);
                    });
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exec.shutdown();
    }
}
