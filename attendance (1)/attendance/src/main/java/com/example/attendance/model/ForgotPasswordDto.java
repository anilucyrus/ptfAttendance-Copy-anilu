package com.example.attendance.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordDto {
    private String email; // User's email to identify the account
    private String newPassword; // The new password for the account
    private String confirmPassword; // To confirm the new password
}
