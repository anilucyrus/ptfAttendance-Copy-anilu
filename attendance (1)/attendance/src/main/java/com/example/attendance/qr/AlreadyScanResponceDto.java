package com.example.attendance.qr;

import lombok.Data;

@Data
public class AlreadyScanResponceDto {
    private String message;

    public AlreadyScanResponceDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
