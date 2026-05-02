package com.example.lifelist.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.lifelist.R;
import com.example.lifelist.ui.main.MainActivity;
import com.example.lifelist.util.SecurityManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executor;

public class AuthActivity extends AppCompatActivity {

    private SecurityManager securityManager;
    private TextInputEditText etPin;
    private Button btnAction, btnBiometric, btnGuest;
    private TextView tvTitle;
    private boolean isSetupMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        securityManager = SecurityManager.getInstance(this);
        etPin = findViewById(R.id.etPin);
        btnAction = findViewById(R.id.btnAction);
        btnBiometric = findViewById(R.id.btnBiometric);
        btnGuest = findViewById(R.id.btnGuest);
        tvTitle = findViewById(R.id.tvTitle);

        // Режим первичной настройки или входа
        if (!securityManager.hasPin()) {
            isSetupMode = true;
            tvTitle.setText("Создайте PIN-код для защиты дневника");
            btnAction.setText("Сохранить PIN");
            btnBiometric.setVisibility(View.GONE); // Биометрия недоступна до создания PIN
        } else {
            tvTitle.setText("Введите PIN или используйте биометрию");
            btnAction.setText("Войти");
            setupBiometricButton();
        }

        btnAction.setOnClickListener(v -> handlePinAction());
        btnBiometric.setOnClickListener(v -> startBiometricAuth());
        btnGuest.setOnClickListener(v -> loginAsGuest());
    }

    /** Проверяет поддержку биометрии и настройки пользователя */
    private void setupBiometricButton() {
        BiometricManager biometricManager = BiometricManager.from(this);
        // canAuthenticate() без параметров работает на API 23+
        int canAuthenticate = biometricManager.canAuthenticate();

        boolean isHardwareAvailable = (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS);
        boolean isEnabledInSettings = securityManager.isBiometricEnabled();

        if (isHardwareAvailable && isEnabledInSettings) {
            btnBiometric.setVisibility(View.VISIBLE);
            btnBiometric.setText("Войти по отпечатку");
        } else {
            btnBiometric.setVisibility(View.GONE);
        }
    }

    /** Обработка нажатия кнопки PIN */
    private void handlePinAction() {
        String pin = etPin.getText() != null ? etPin.getText().toString() : "";
        if (pin.length() != 4 || !pin.matches("\\d+")) {
            Toast.makeText(this, "PIN должен состоять из 4 цифр", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSetupMode) {
            securityManager.setPin(pin);
            securityManager.setRole(SecurityManager.ROLE_OWNER);
            securityManager.setBiometricEnabled(true); // Включаем биометрию по умолчанию
            Toast.makeText(this, "PIN сохранён. Добро пожаловать!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            if (pin.equals(securityManager.getPin())) {
                securityManager.setRole(SecurityManager.ROLE_OWNER);
                navigateToMain();
            } else {
                Toast.makeText(this, "Неверный PIN", Toast.LENGTH_SHORT).show();
                etPin.setText("");
            }
        }
    }

    /** Запуск системного диалога биометрии */
    private void startBiometricAuth() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(AuthActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        securityManager.setRole(SecurityManager.ROLE_OWNER);
                        Toast.makeText(AuthActivity.this, "Биометрия подтверждена", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(AuthActivity.this, "Распознавание не удалось. Попробуйте ещё раз.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // Игнорируем отмену пользователем, показываем только критические ошибки
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            Toast.makeText(AuthActivity.this, "Ошибка биометрии: " + errString, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Биометрическая аутентификация")
                .setSubtitle("Подтвердите личность для входа в дневник")
                .setNegativeButtonText("Ввести PIN")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    /** Вход в режиме Гостя */
    private void loginAsGuest() {
        securityManager.setRole(SecurityManager.ROLE_GUEST);
        Toast.makeText(this, "Режим Гостя: только чтение", Toast.LENGTH_SHORT).show();
        navigateToMain();
    }

    /** Переход к основному экрану с очисткой стека */
    private void navigateToMain() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}