package com.example.lifelist.util;

import android.content.Context;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SecurityManager {
    private static final String PREFS_NAME = "diary_security_prefs";
    private static final String KEY_PIN = "owner_pin";
    private static final String KEY_ROLE = "current_role";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";

    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_GUEST = "GUEST";

    private static SecurityManager instance;
    private final EncryptedSharedPreferences prefs;

    private SecurityManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            prefs = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации SecurityManager", e);
        }
    }

    public static synchronized SecurityManager getInstance(Context context) {
        if (instance == null) {
            instance = new SecurityManager(context.getApplicationContext());
        }
        return instance;
    }

    // --- Работа с PIN ---
    public boolean hasPin() {
        return prefs.contains(KEY_PIN);
    }

    public String getPin() {
        return prefs.getString(KEY_PIN, "");
    }

    public void setPin(String pin) {
        prefs.edit().putString(KEY_PIN, pin).apply();
    }

    // --- Работа с ролями ---
    public void setRole(String role) {
        prefs.edit().putString(KEY_ROLE, role).apply();
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, ROLE_OWNER); // По умолчанию владелец
    }

    public boolean isOwner() {
        return ROLE_OWNER.equals(getRole());
    }

    public boolean isGuest() {
        return ROLE_GUEST.equals(getRole());
    }

    // --- Биометрия ---
    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }
}