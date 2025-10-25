package services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.UserViewModel;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class RemoteUserStatsService implements UserStatsService {

    private final String baseUrl;             // למשל: http://localhost:8080
    private final String statsPath;           // למשל: /api/users/stats
    private final HttpClient client;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<UserStatsDTO>>(){}.getType();

    public RemoteUserStatsService(String baseUrl) {
        this(baseUrl, "/api/connected-users");
    }

    public RemoteUserStatsService(String baseUrl, String statsPath) {
        // הסר סלאש סופי, והשאר את הנתיב בנפרד
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.statsPath = statsPath.startsWith("/") ? statsPath : "/" + statsPath;

        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public List<UserViewModel> fetchConnectedUsers() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + statsPath))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                System.err.println("RemoteUserStatsService: HTTP " + res.statusCode());
                return Collections.emptyList();
            }

            List<UserStatsDTO> dtoList = gson.fromJson(res.body(), listType);
            return dtoList.stream()
                    .map(dto -> new UserViewModel(
                            dto.name,
                            dto.mainPrograms,
                            dto.functions,
                            dto.creditsCurrent,
                            dto.creditsUsed,
                            dto.runs
                    ))
                    .toList();

        } catch (IOException | InterruptedException e) {
            System.err.println("RemoteUserStatsService: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    static class UserStatsDTO {
        String name;
        int mainPrograms;
        int functions;
        int creditsCurrent;
        int creditsUsed;
        int runs;
    }
}
