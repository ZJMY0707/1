package studentknowledge.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }
    public static <T> ApiResponse<T> ok(String msg, T data) {
        return new ApiResponse<>(200, msg, data);
    }
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(200, "操作成功", null);
    }
    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>(400, msg, null);
    }
    public static <T> ApiResponse<T> fail(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}
