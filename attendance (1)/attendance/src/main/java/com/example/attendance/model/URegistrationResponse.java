package com.example.attendance.model;

import lombok.Data;

@Data
public class URegistrationResponse {
    private Long id;
    private String name;
    private String email;
    private String batch;
    private String phoneNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public URegistrationResponse(Long id, String name, String email, String batch, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.batch = batch;
        this.phoneNumber = phoneNumber;
    }
}
