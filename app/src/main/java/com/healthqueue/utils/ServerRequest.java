package com.healthqueue.utils;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ServerRequest {
    public static record PatientSignupRequest(
            @NotBlank String name,
            @Email String email,
            @NotBlank @Size(min = 8, max = 15) String phoneNumber,
            @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) String password) {
    }

    public static record OrganizationSignupRequest(
            @NotBlank String name,
            @Email String email,
            @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) String password) {
    }

    public static record PatientLoginRequest(
            @Email String email,
            @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) String password) {
    }

    public static record OrganizationLoginRequest(
            @Email String email,
            @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) String password) {
    }

    public static record CreateAppointmentRequest(
            @NotBlank String patientName,
            @Email String contactEmail,
            @Min(0) long branchId) {
    }
}
