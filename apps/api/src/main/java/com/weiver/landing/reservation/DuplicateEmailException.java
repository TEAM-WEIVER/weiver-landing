package com.weiver.landing.reservation;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() { super("duplicate email"); }
}
