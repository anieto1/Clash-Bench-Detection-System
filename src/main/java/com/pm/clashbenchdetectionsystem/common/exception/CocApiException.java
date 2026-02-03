package com.pm.clashbenchdetectionsystem.common.exception;

import lombok.Getter;

@Getter
public class CocApiException extends RuntimeException {

    private final int statusCode;
    private final String reason;

    private CocApiException(int statusCode, String reason) {
        super("CoC API error [" + statusCode + "]: " + reason);
        this.statusCode = statusCode;
        this.reason = reason;
    }

    public static CocApiException notFound(String resource) {
        return new CocApiException(404, resource + " not found");
    }

    public static CocApiException rateLimited() {
        return new CocApiException(429, "Rate limit exceeded");
    }

    public static CocApiException forbidden() {
        return new CocApiException(403, "Invalid API key or IP not whitelisted");
    }

    public static CocApiException privateWarLog() {
        return new CocApiException(403, "Clan war log is private");
    }

    public static CocApiException badRequest(String message) {
        return new CocApiException(400, message);
    }

    public static CocApiException serverError() {
        return new CocApiException(503, "CoC API unavailable");
    }
}