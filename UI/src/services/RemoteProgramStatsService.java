package services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.ProgramViewModel;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class RemoteProgramStatsService implements ProgramStatsService {

    private final String baseUrl;             // למשל: http://localhost:8080
    private final String statsPath;           // למשל: /get-all-programs-dtos
    private final HttpClient client;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<ProgramStatsDTO>>(){}.getType();

    public RemoteProgramStatsService(String baseUrl) {
        this(baseUrl, "/get-all-programs-dtos");
    }

    public RemoteProgramStatsService(String baseUrl, String statsPath) {
        // הסר סלאש סופי, והשאר את הנתיב בנפרד
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.statsPath = statsPath.startsWith("/") ? statsPath : "/" + statsPath;

        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public List<ProgramViewModel> fetchAllPrograms() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + statsPath))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                System.err.println("RemoteProgramStatsService: HTTP " + res.statusCode());
                return Collections.emptyList();
            }

            List<ProgramStatsDTO> dtoList = gson.fromJson(res.body(), listType);
            return dtoList.stream()
                    .map(dto -> new ProgramViewModel(
                            dto.name,
                            dto.uploaderUsername,
                            dto.instructionCount,
                            dto.maxDegree,
                            dto.runCount,
                            dto.averageCost
                    ))
                    .toList();

        } catch (IOException | InterruptedException e) {
            System.err.println("RemoteProgramStatsService: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    static class ProgramStatsDTO {
        String name;
        String uploaderUsername;
        int instructionCount;
        int maxDegree;
        int runCount;
        double totalCreditsCost;
        double averageCost;
    }
}