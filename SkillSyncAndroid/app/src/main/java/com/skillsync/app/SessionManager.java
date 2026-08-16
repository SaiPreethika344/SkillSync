package com.skillsync.app;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences("skill_sync_prefs", Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        preferences.edit().putString("jwt_token", token).apply();
    }

    public String getToken() {
        return preferences.getString("jwt_token", "");
    }

    public boolean isLoggedIn() {
        return !getToken().isEmpty();
    }

    public void saveUser(String name, String email) {
        preferences.edit()
                .putString("user_name", name)
                .putString("user_email", email)
                .apply();
    }

    public String getUserName() {
        return preferences.getString("user_name", "SkillSync User");
    }

    public String getUserEmail() {
        return preferences.getString("user_email", "");
    }

    public void saveLastAnalysis(String analysis) {
        preferences.edit().putString("last_analysis", analysis).apply();
    }

    public String getLastAnalysis() {
        return preferences.getString("last_analysis", "");
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}
