package services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import model.FunctionViewModel;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoteFunctionStatsService implements FunctionStateService {

    private final String baseUrl;             // למשל: http://localhost:8080
    private final String statsPath;           // למשל: /get-system-functions-dtos או /get-system-functions
    private final HttpClient client;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<FunctionStatsDTO>>(){}.getType();

    public RemoteFunctionStatsService(String baseUrl) {
        this(baseUrl, "/get-system-functions-dtos");
    }

    public RemoteFunctionStatsService(String baseUrl, String statsPath) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.statsPath = statsPath.startsWith("/") ? statsPath : "/" + statsPath;

        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public List<FunctionViewModel> fetchAllFunction() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + statsPath))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                System.err.println("RemoteFunctionStatsService: HTTP " + res.statusCode());
                return Collections.emptyList();
            }

            String body = res.body();
            JsonElement root = JsonParser.parseString(body);

            List<FunctionStatsDTO> dtoList = new ArrayList<>();

            if (root.isJsonArray()) {
                // מערך חשוף: [ {...}, {...} ]
                dtoList = gson.fromJson(root, listType);

            } else if (root.isJsonObject()) {
                // עטיפה: { status, count, functions: [...] } או עטיפות אחרות
                JsonObject obj = root.getAsJsonObject();

                // חפש מפתחות סבירים של אוסף
                String[] candidates = {"functions", "items", "data"};
                for (String key : candidates) {
                    if (obj.has(key) && obj.get(key).isJsonArray()) {
                        dtoList = gson.fromJson(obj.get(key), listType);
                        break;
                    }
                }

                // fallback: אם חזר אובייקט יחיד של פונקציה
                if (dtoList.isEmpty()) {
                    try {
                        FunctionStatsDTO single = gson.fromJson(obj, FunctionStatsDTO.class);
                        if (single != null) dtoList.add(single);
                    } catch (Exception ignore) { /* not a single DTO */ }
                }
            }

            if (dtoList == null || dtoList.isEmpty()) return Collections.emptyList();

            // מיפוי ל-ViewModel עם קואלאציה של שמות שדות שונים
            List<FunctionViewModel> vms = new ArrayList<>(dtoList.size());
            for (FunctionStatsDTO dto : dtoList) {
                String name              = firstNonNull(dto.functionName, dto.name);
                String uploadProgramName = firstNonNull(dto.uploaderProgramName, dto.uploadProgramName);
                String uploaderUsername  = firstNonNull(dto.uploaderUserName, dto.uploaderUsername);
                Integer instrCount       = firstNonNull(dto.numberOfInstructions, dto.instructionCount);
                Integer maxDegree        = dto.maxDegree; // זהה בשני הצדדים

                if (name == null) name = ""; // הגנה קלה

                vms.add(new FunctionViewModel(
                        name,
                        uploadProgramName,
                        uploaderUsername,
                        instrCount != null ? instrCount : 0,
                        maxDegree != null ? maxDegree : 0
                ));
            }
            return vms;

        } catch (IOException | InterruptedException e) {
            System.err.println("RemoteFunctionStatsService: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private static <T> T firstNonNull(T a, T b) {
        return a != null ? a : b;
    }

    // DTO עם תמיכה בשמות שדות חלופיים מהשרת
    static class FunctionStatsDTO {
        // שם הפונקציה
        @SerializedName("functionName")  String functionName;   // גרסת השרת A
        @SerializedName("name")          String name;           // גרסת השרת B

        // שם התוכנית שהעלתה
        @SerializedName("uploaderProgramName") String uploaderProgramName; // A
        @SerializedName("uploadProgramName")   String uploadProgramName;   // B

        // מעלה הפונקציה
        @SerializedName("uploaderUserName") String uploaderUserName; // A
        @SerializedName("uploaderUsername") String uploaderUsername; // B

        // כמות הוראות
        @SerializedName("numberOfInstructions") Integer numberOfInstructions; // A
        @SerializedName("instructionCount")     Integer instructionCount;     // B

        // דרגה מקסימלית
        @SerializedName("maxDegree") Integer maxDegree;
    }
}
