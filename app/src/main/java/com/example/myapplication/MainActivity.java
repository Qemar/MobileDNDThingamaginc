package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity implements PointBuyFragment.OnSelectionChangedListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate layout (это может инфлейтить фрагмент внутри макета)
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // по умолчанию отключим FAB — пока фрагмент не подтвердит, что всё выбрано
        if (binding.fab != null) {
            binding.fab.setEnabled(false);
            binding.fab.setAlpha(0.5f);
        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(view -> {
            // Получаем текущий экран из NavHost
            Fragment navHost = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
            if (navHost != null && navHost.getChildFragmentManager().getFragments().size() > 0) {
                Fragment current = navHost.getChildFragmentManager().getFragments().get(0);
                if (current instanceof com.example.myapplication.PointBuyFragment) {
                    // Просим фрагмент собрать Intent для CharacterActivity
                    Intent maybe = ((com.example.myapplication.PointBuyFragment) current).buildCharacterIntent(MainActivity.this);
                    if (maybe != null) {
                        startActivity(maybe);
                    } else {
                        // Не прошли валидацию — попросим выбрать расу/подрасу
                        Snackbar.make(binding.getRoot(), "Выберите расу и подрасу (если требуется) перед продолжением", Snackbar.LENGTH_LONG)
                                .setAnchorView(R.id.fab)
                                .show();
                    }
                    return;
                }
            }

            // Если текущий экран не PointBuyFragment — просто открываем CharacterActivity пустой (без данных)
            Intent intent = new Intent(MainActivity.this, CharacterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onSelectionChanged(final boolean isComplete) {
        // safety: binding может быть ещё null если callback пришёл во время inflate
        if (binding == null) return;

        // всегда выполняем изменение UI в UI-потоке
        runOnUiThread(() -> {
            if (binding.fab != null) {
                binding.fab.setEnabled(isComplete);
                binding.fab.setAlpha(isComplete ? 1f : 0.5f);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // Open Characters list
            Intent i = new Intent(this, CharactersListActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // попытаемся найти PointBuyFragment и узнать состояние
        Fragment navHost = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHost != null && navHost.getChildFragmentManager().getFragments().size() > 0) {
            Fragment current = navHost.getChildFragmentManager().getFragments().get(0);
            if (current instanceof PointBuyFragment) {
                boolean ok = ((PointBuyFragment) current).isSelectionComplete();
                if (binding != null && binding.fab != null) {
                    binding.fab.setEnabled(ok);
                    binding.fab.setAlpha(ok ? 1f : 0.5f);
                }
            }
        }
    }
}
