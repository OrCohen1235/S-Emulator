package services;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.reflect.TypeToken;

import static services.Constants.GSON;
import static services.Constants.HTTP_CLIENT;
import static services.Constants.SERVER_URL;

public class UserService {

    private String currentUsername;

    public boolean login(String username) {
        try {
            String url = SERVER_URL + "login";

            JsonObject payload = new JsonObject();
            payload.addProperty("username", username);
            String jsonBody = GSON.toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type type = new TypeToken<ApiResponse<LoginData>>(){}.getType();
                ApiResponse<LoginData> resp = GSON.fromJson(response.body(), type);

                if (resp != null && "ok".equalsIgnoreCase(resp.status)) {
                    this.currentUsername = (resp.data != null && resp.data.username != null)
                            ? resp.data.username
                            : username;
                    return true;
                } else {
                    System.err.println("Server error: " + (resp != null ? resp.error : "unknown") + " from: login");
                    return false;
                }

            } else {
                try {
                    Type errType = new TypeToken<ApiResponse<Object>>(){}.getType();
                    ApiResponse<Object> err = GSON.fromJson(response.body(), errType);
                    if (err != null && "error".equalsIgnoreCase(err.status)) {
                        System.err.println("HTTP " + response.statusCode() + " (" + err.code + "): " + err.error);
                    } else {
                        System.err.println("HTTP error: " + response.statusCode() + " from: login");
                    }
                } catch (Exception ignore) {
                    System.err.println("HTTP error: " + response.statusCode() + " from: login");
                }
                return false;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean logout() {
        if (currentUsername == null) {
            return false;
        }

        try {
            String url = SERVER_URL + "logout";

            JsonObject payload = new JsonObject();
            payload.addProperty("username", currentUsername);
            String jsonBody = GSON.toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type type = new TypeToken<ApiResponse<Object>>(){}.getType();
                ApiResponse<Object> resp = GSON.fromJson(response.body(), type);

                if (resp != null && "ok".equalsIgnoreCase(resp.status)) {
                    System.out.println("Logged out successfully: " + currentUsername);
                    this.currentUsername = null;
                    return true;
                } else {
                    System.err.println("Server error: " + (resp != null ? resp.error : "unknown") + " from: logout");
                    return false;
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: logout");
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Logout failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            this.currentUsername = null;
        }
    }

    // ← הוסף את זה ←
    public boolean updateCredits(int credits) {
        if (credits < 0) {
            credits = 0;
        }
        return updateUserField("/api/update-credits", "credits", credits);
    }

    public boolean updateMainPrograms(int mainPrograms) {
        return updateUserField("api/update-main-programs", "mainPrograms", mainPrograms);
    }

    public boolean updateFunctions(int functions) {
        return updateUserField("api/update-functions", "functions", functions);
    }

    public boolean updateCreditsUsed(int creditsUsed) {
        return updateUserField("api/update-credits-used", "creditsUsed", creditsUsed);
    }

    public boolean updateRuns(int runs) {
        return updateUserField("api/update-runs", "runs", runs);
    }

    private boolean updateUserField(String endpoint, String fieldName, int value) {
        if (currentUsername == null) {
            System.err.println("Cannot update " + fieldName + " - user not logged in");
            return false;
        }

        try {
            String url = SERVER_URL + endpoint;

            JsonObject payload = new JsonObject();
            payload.addProperty("username", currentUsername);
            payload.addProperty(fieldName, value);
            String jsonBody = GSON.toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type type = new TypeToken<ApiResponse<Object>>(){}.getType();
                ApiResponse<Object> resp = GSON.fromJson(response.body(), type);

                if (resp != null && "ok".equalsIgnoreCase(resp.status)) {
                    System.out.println(fieldName + " updated successfully for: " + currentUsername);
                    return true;
                } else {
                    System.err.println("Server error: " + (resp != null ? resp.error : "unknown"));
                    return false;
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode());
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to update " + fieldName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getCurrentUsername() {
        return currentUsername;
    }


    public boolean isLoggedIn() {
        return currentUsername != null;
    }

    private static class ApiResponse<T> {
        String status;
        String code;
        String error;
        T data;
    }

    private static class LoginData {
        String username;
    }
}