package com.healthqueue.utils;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ServerRequest {
    public static record CreateAppointmentRequest(
            @NotBlank String patientName,
            @Email String contactEmail,
            @Min(0) int branchId) {
    }
}
