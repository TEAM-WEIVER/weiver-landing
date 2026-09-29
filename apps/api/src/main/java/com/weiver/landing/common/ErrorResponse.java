package com.weiver.landing.common;

public record ErrorResponse(String error) {
    public static ErrorResponse of(String code) { return new ErrorResponse(code); }
}
