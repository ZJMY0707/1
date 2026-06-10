package studentknowledge.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * HTTP客户端 - 封装与后端REST API的通信
 */
public class HttpClient {

    private static final String BASE_URL = "http://localhost:8088/api";
    private static final java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                (src, t, ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                (json, t, ctx) -> {
                    String s = json.getAsString();
                    if (s == null || s.isEmpty()) return null;
                    return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                })
            .create();

    public static Gson getGson() { return gson; }

    /** GET请求 */
    public static String get(String path) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .header("Content-Type", "application/json");
        if (SessionManager.getToken() != null) {
            builder.header("Authorization", "Bearer " + SessionManager.getToken());
        }
        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkStatus(resp);
        return resp.body();
    }

    /** POST请求 */
    public static String post(String path, Object body) throws Exception {
        String json = gson.toJson(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json");
        if (SessionManager.getToken() != null) {
            builder.header("Authorization", "Bearer " + SessionManager.getToken());
        }
        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkStatus(resp);
        return resp.body();
    }

    /** PUT请求 */
    public static String put(String path, Object body) throws Exception {
        String json = gson.toJson(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json");
        if (SessionManager.getToken() != null) {
            builder.header("Authorization", "Bearer " + SessionManager.getToken());
        }
        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkStatus(resp);
        return resp.body();
    }

    /** DELETE请求 */
    public static String delete(String path) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE()
                .header("Content-Type", "application/json");
        if (SessionManager.getToken() != null) {
            builder.header("Authorization", "Bearer " + SessionManager.getToken());
        }
        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkStatus(resp);
        return resp.body();
    }

    /** 解析API响应中的data字段 */
    public static <T> T parseData(String json, Type type) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        if (obj.get("code").getAsInt() != 200) {
            throw new RuntimeException(obj.get("message").getAsString());
        }
        JsonElement data = obj.get("data");
        if (data == null || data.isJsonNull()) return null;
        return gson.fromJson(data, type);
    }

    public static String parseMessage(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        return obj.get("message").getAsString();
    }

    public static int parseCode(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        return obj.get("code").getAsInt();
    }

    private static void checkStatus(HttpResponse<String> resp) {
        if (resp.statusCode() == 403 || resp.statusCode() == 401) {
            throw new RuntimeException("登录已过期，请重新登录");
        }
    }
}
