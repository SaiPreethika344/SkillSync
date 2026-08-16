package com.skillsync.app;

import org.json.JSONObject;
import org.json.JSONArray;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.EventListener;

public class ApiClient {
    private static final String TAG = "SkillSync.ApiClient";
    private static final String BASE_URL = normalizeBaseUrl(BuildConfig.BASE_URL);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public ApiClient() {
        Log.d(TAG, "Creating OkHttp client; configuredBaseUrl=" + BuildConfig.BASE_URL
            + ", normalizedBaseUrl=" + BASE_URL);
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .eventListenerFactory(call -> new EventListener() {
                    @Override
                    public void callStart(Call call) {
                        Log.d(TAG, "HTTP call started; url=" + call.request().url());
                    }

                    @Override
                    public void dnsStart(Call call, String domainName) {
                        Log.d(TAG, "DNS lookup started; host=" + domainName);
                    }

                    @Override
                    public void dnsEnd(Call call, String domainName, java.util.List<java.net.InetAddress> inetAddressList) {
                        Log.d(TAG, "DNS lookup completed; host=" + domainName + ", addresses=" + inetAddressList);
                    }

                    @Override
                    public void connectStart(Call call, java.net.InetSocketAddress address, java.net.Proxy proxy) {
                        Log.d(TAG, "Connection started; address=" + address);
                    }

                    @Override
                    public void connectEnd(Call call, java.net.InetSocketAddress address, java.net.Proxy proxy, okhttp3.Protocol protocol) {
                        Log.d(TAG, "Connection completed; address=" + address + ", protocol=" + protocol);
                    }

                    @Override
                    public void callEnd(Call call) {
                        Log.d(TAG, "HTTP call completed; url=" + call.request().url());
                    }

                    @Override
                    public void callFailed(Call call, IOException ioe) {
                        Log.e(TAG, "HTTP call failed; url=" + call.request().url(), ioe);
                    }
                })
                .build();
    }

    public void login(String email, String password, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);
            postJson("/auth/login", body, null, callback);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void signup(String name, String email, String password, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("email", email);
            body.put("password", password);
            postJson("/auth/register", body, null, callback);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void forgotPassword(String email, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            postJson("/auth/forgot-password", body, null, callback);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void resetPassword(String email, String otp, String newPassword, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("otp", otp);
            body.put("newPassword", newPassword);
            postJson("/auth/reset-password", body, null, callback);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void analyzeSkills(String token, String input, Callback callback) {
        try {
            Log.d(TAG, "Creating analysis request; inputLength=" + (input == null ? 0 : input.length()));
            postJson("/analysis/run", buildAnalysisRequest(input), token, callback);
        } catch (Exception e) {
            Log.e(TAG, "Exception while creating analysis request; no network call was enqueued", e);
            throw new RuntimeException("Unable to create analysis request", e);
        }
    }

    public void getDashboard(String token, Callback callback) {
        getJson("/dashboard", token, callback);
    }

    private JSONObject buildAnalysisRequest(String input) throws Exception {
        String normalized = input == null ? "" : input.trim();
        String field = "";
        List<String> skills = new ArrayList<>();

        String[] lines = normalized.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            String lowerLine = line.toLowerCase();
            if (lowerLine.startsWith("field:")) {
                field = line.substring(line.indexOf(':') + 1).trim();
                continue;
            }

            if (lowerLine.startsWith("skills:")) {
                addSkills(skills, line.substring(line.indexOf(':') + 1));
                continue;
            }

            if (field.isEmpty()) {
                field = line;
            } else {
                addSkills(skills, line);
            }
        }

        if (skills.isEmpty()) {
            addSkills(skills, normalized.replace(field, ""));
        }

        if (field.isEmpty()) {
            field = "General";
        }

        if (skills.isEmpty()) {
            skills.add(field);
        }

        JSONArray skillsArray = new JSONArray();
        for (String skill : skills) {
            skillsArray.put(skill);
        }

        JSONObject body = new JSONObject();
        body.put("field", field);
        body.put("skills", skillsArray);
        return body;
    }

    private void addSkills(List<String> skills, String rawSkills) {
        if (rawSkills == null || rawSkills.isBlank()) {
            return;
        }

        String normalizedSkills = rawSkills
                .replace("[", " ")
                .replace("]", " ")
                .trim();

        String[] parts = normalizedSkills.split("[,;|]");
        for (String part : parts) {
            String skill = part.trim();
            if (!skill.isEmpty()) {
                skills.add(skill);
            }
        }
    }

    private void postJson(String path, JSONObject body, String token, Callback callback) {
        String url = buildUrl(path);
        Log.d(TAG, "Building POST request; url=" + url + ", body=" + body);
        Request.Builder builder = new Request.Builder().url(url);
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        RequestBody requestBody = RequestBody.create(body.toString(), JSON);
        Request request = builder.post(requestBody).build();
        Log.d(TAG, "Executing POST request with OkHttp enqueue; url=" + request.url());
        client.newCall(request).enqueue(callback);
    }

    private static String buildUrl(String path) {
        String normalizedPath = path == null ? "" : path.trim();
        if (normalizedPath.isEmpty()) {
            throw new IllegalArgumentException("API path is empty");
        }
        String url = BASE_URL + (normalizedPath.startsWith("/") ? normalizedPath : "/" + normalizedPath);
        Log.d(TAG, "Resolved API URL; base=" + BASE_URL + ", path=" + normalizedPath + ", url=" + url);
        return url;
    }

    private static String normalizeBaseUrl(String value) {
        String normalized = value == null ? "" : value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isEmpty()) {
            throw new IllegalStateException("BuildConfig.BASE_URL is empty");
        }
        return normalized;
    }

    private void getJson(String path, String token, Callback callback) {
        Request.Builder builder = new Request.Builder().url(buildUrl(path));
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        Request request = builder.get().build();
        client.newCall(request).enqueue(callback);
    }
}
