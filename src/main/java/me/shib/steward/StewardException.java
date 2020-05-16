package me.shib.steward;

public final class StewardException extends Exception {
    public StewardException(String message) {
        super(message);
    }

    public StewardException(Exception e) {
        super(e.getMessage(), e.getCause());
    }
}
