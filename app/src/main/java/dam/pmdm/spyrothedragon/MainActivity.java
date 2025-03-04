package dam.pmdm.spyrothedragon;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import dam.pmdm.spyrothedragon.databinding.ActivityMainBinding;
import dam.pmdm.spyrothedragon.databinding.GuideActivityBinding;
import dam.pmdm.spyrothedragon.databinding.WelcomeBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private WelcomeBinding welcomeBinding;
    private GuideActivityBinding guideBinding;
    NavController navController = null;
    boolean showGuide = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        welcomeBinding = binding.welcomeInclude;
        guideBinding = binding.guideInclude;

        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        if (navHostFragment != null) {
            navController = NavHostFragment.findNavController(navHostFragment);
            NavigationUI.setupWithNavController(binding.navView, navController);
            NavigationUI.setupActionBarWithNavController(this, navController);
        }

        binding.navView.setOnItemSelectedListener(this::selectedBottomMenu);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_characters ||
                    destination.getId() == R.id.navigation_worlds ||
                    destination.getId() == R.id.navigation_collectibles) {
                // Para las pantallas de los tabs, no queremos que aparezca la flecha de atrás
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            else {
                // Si se navega a una pantalla donde se desea mostrar la flecha de atrás, habilítala
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        });

        welcomeBinding.getRoot().setVisibility(View.VISIBLE);
        welcomeBinding.buttonBegin.setBackgroundColor(getResources().getColor(R.color.purpleComplementary));

        welcomeBinding.buttonBegin.setOnClickListener(v -> {
            welcomeBinding.getRoot().setVisibility(v.GONE);
            guideBinding.getRoot().setVisibility(View.VISIBLE);
            animateView(guideBinding.speachBubble);
        });

        if (showGuide) {
            binding.welcomeInclude.welcomeLayout.setVisibility(View.VISIBLE);
        } else {
            binding.welcomeInclude.welcomeLayout.setVisibility(View.GONE);
        }

        guideBinding.closeGuideButton.setOnClickListener(this::closeGuide);
        guideBinding.speachBubble.setOnClickListener(this::nextStep);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (showGuide) {
            ImageView imageView = binding.welcomeInclude.imageView;
            if (imageView != null) {
                new Handler(Looper.getMainLooper()).postDelayed((Runnable) () -> {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
                    ObjectAnimator animator2 = ObjectAnimator.ofFloat(imageView, "translationY", -360f, 0f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(animator, animator2);
                    animatorSet.setDuration(2000);
                    animatorSet.start();
                }, 900);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el menú
        getMenuInflater().inflate(R.menu.about_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Gestiona el clic en el ítem de información
        if (item.getItemId() == R.id.action_info) {
            showInfoDialog();  // Muestra el diálogo
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInfoDialog() {
        // Crear un diálogo de información
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_about)
                .setMessage(R.string.text_about)
                .setPositiveButton(R.string.accept, null)
                .show();
    }

    private boolean selectedBottomMenu(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_characters)
            navController.navigate(R.id.navigation_characters);
        else
        if (menuItem.getItemId() == R.id.nav_worlds)
            navController.navigate(R.id.navigation_worlds);
        else
            navController.navigate(R.id.navigation_collectibles);
        return true;

    }

    // Metodo para cerrar la guia
    private void closeGuide(View view) {
        showGuide = false;
        welcomeBinding.getRoot().setVisibility(View.GONE);
        guideBinding.getRoot().setVisibility(View.GONE);
    }

    // Variable para rastrear el índice actual del paso
    private int currentStepIndex = 0;

    // Funcion para avanzar al siguiente paso en la guía
    private void nextStep(View view) {
        Menu menu = binding.navView.getMenu();
        int menuSize = menu.size();
        // Incrementar el índice del paso actual
        currentStepIndex = (currentStepIndex + 1); // Ciclar si llega al final
        if (currentStepIndex < menuSize) {

            // Obtener el ítem correspondiente al índice actual
            MenuItem menuItem = menu.getItem(currentStepIndex);

            int itemViewId = menuItem.getItemId();
            // Intentar encontrar la vista asociada al ítem
            View itemView = binding.navView.findViewById(itemViewId);

            if (itemViewId == R.id.nav_characters) guideBinding.guideText.setText(R.string.characterTabExplanation);
            if (itemViewId == R.id.nav_worlds) guideBinding.guideText.setText(R.string.worldsTabExplanation);
            if (itemViewId == R.id.nav_collectibles) guideBinding.guideText.setText(R.string.collectiblesTabExplanation);

            if (itemView != null) {
                int[] itemLocation = new int[2];
                itemView.getLocationOnScreen(itemLocation);

                int itemX = itemLocation[0];

                // Calcular el centro del ítem
                int itemWidth = itemView.getWidth();
                float centerX = itemX + (itemWidth / 2f); // Centro horizontal

                // Mover la vista a la posición del ítem seleccionado
                view.animate()
                        .x(centerX - (view.getWidth() / 2f))
                        .setDuration(500) // Duración de la animación en milisegundos
                        .start();

                animateView(view);

                binding.navView.setSelectedItemId(menuItem.getItemId());

            }
            return;
        };

        guideBinding.aboutGuideView.setVisibility(View.VISIBLE);
        animateView(guideBinding.aboutGuideView);
        guideBinding.guideText.setVisibility(View.GONE);
        guideBinding.speachBubble.setVisibility(View.GONE);

    }

    private void animateView(View view){
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.5f);
        scaleX.setRepeatCount(3);
        scaleY.setRepeatCount(3);
        AnimatorSet as = new AnimatorSet();
        as.playTogether(scaleX, scaleY);
        as.setDuration(1000);
        as.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {}

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                view.setScaleX(1f);
                view.setScaleY(1f);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        as.start();
    }
}
