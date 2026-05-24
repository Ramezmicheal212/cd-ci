package com.example.demo.ExceptionHandler;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API error response")
public class ApiError {

    @Schema(example = "2026-03-05T15:25:17.123")
    private final String timestamp;
    @Schema(example = "404")
    private final int status;
    @Schema(example = "Not Found")
    private final String error;
    @Schema(example = "Hotel not found with id: 99")
    private final String message;
    @Schema(example = "/api/hotels/99")
    private final String path;

    public ApiError(String timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
