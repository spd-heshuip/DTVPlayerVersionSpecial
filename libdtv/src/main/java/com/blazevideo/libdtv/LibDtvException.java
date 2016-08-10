package com.blazevideo.libdtv;

public class LibDtvException extends Exception {
    public LibDtvException() {
        super();
    }

    public LibDtvException(String detailMessage) {
        super(detailMessage);
    }

    public LibDtvException(Throwable throwable) {
        super(throwable);
    }

    public LibDtvException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
