package com.server.sample.serversample;

public class TestRequest {
    private final String _status;
    private final Integer _code;

    public TestRequest(String status, Integer code) {
        this._status = status;
        this._code = code;
    }

    public String get_status() {
        return _status;
    }

    public Integer get_code() {
        return _code;
    }
}
