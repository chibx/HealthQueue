package com.healthqueue.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.ZonedDateTime;
import java.util.UUID;

public class ServerRequest {

        // ==========================================
        // USER (PATIENT) REQUESTS
        // ==========================================
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record PatientSignupRequest(
                        @NotBlank @JsonProperty("firstName") String firstName,
                        @NotBlank @JsonProperty("lastName") String lastName,
                        @Email @JsonProperty("email") String email,
                        @NotBlank @Size(min = 8, max = 15) @JsonProperty("phoneNumber") String phoneNumber,
                        @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) @JsonProperty("password") String password,
                        @NotNull @JsonProperty("latitude") @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0") @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0") Double latitude,
                        @NotNull @JsonProperty("longitude") @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0") @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0") Double longitude) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record PatientLoginRequest(
                        @Email @JsonProperty("email") String email,
                        @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) @JsonProperty("password") String password) {
        }

        // ==========================================
        // ORGANIZATION REQUESTS
        // ==========================================

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record OrganizationSignupRequest(
                        @NotBlank @JsonProperty("name") String name,
                        @NotBlank @JsonProperty("registrationCode") String registrationCode,
                        @Email @JsonProperty("email") String email,
                        @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) @JsonProperty("password") String password) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record OrganizationLoginRequest(
                        @Email @JsonProperty("email") String email,
                        @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) @JsonProperty("password") String password) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record DoctorLoginRequest(
                        @NotBlank @JsonProperty("organizationName") String organizationName,
                        @Email @JsonProperty("email") String email,
                        @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) @JsonProperty("password") String password) {
        }

        // ==========================================
        // BRANCH & DOCTOR MANAGEMENT REQUESTS
        // ==========================================

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record CreateBranchRequest(
                        @NotNull @JsonProperty("organizationId") UUID organizationId,
                        @NotBlank @JsonProperty("name") String name,
                        @NotBlank @JsonProperty("address") String address,
                        @NotNull @JsonProperty("latitude") @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0") @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0") Double latitude,
                        @NotNull @JsonProperty("longitude") @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0") @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0") Double longitude) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record CreateDoctorRequest(
                        @Min(1) @JsonProperty("branchId") long branchId,
                        @NotBlank @JsonProperty("firstName") String firstName,
                        @NotBlank @JsonProperty("lastName") String lastName,
                        @NotBlank @JsonProperty("specialty") String specialty,
                        @Email @JsonProperty("email") String email,
                        @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) @JsonProperty("password") String password) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record DeleteDoctorRequest(
                        @Min(1) @JsonProperty("branchId") long branchId,
                        @NotBlank @JsonProperty("staffId") long doctorId) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record ChangeDoctorBranchRequest(
                        @Min(1) @JsonProperty("branchId") long branchId,
                        @Min(1) @JsonProperty("newBranchId") long newBranchId,
                        @NotBlank @JsonProperty("doctorId") long doctorId) {
        }

        // ==========================================
        // APPOINTMENT & QUEUE REQUESTS
        // ==========================================

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record CreateAppointmentRequest(
                        @NotNull @JsonProperty("userId") UUID userId,
                        @Min(1) @JsonProperty("branchId") long branchId,
                        @Min(1) @JsonProperty("doctorId") long doctorId,
                        @NotNull @JsonProperty("scheduledStartTime") ZonedDateTime scheduledStartTime,
                        @NotNull @JsonProperty("scheduledEndTime") ZonedDateTime scheduledEndTime) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record UpdateAppointmentStatusRequest(
                        @Min(1) @JsonProperty("appointmentId") long appointmentId,
                        @NotBlank @JsonProperty("status") String status,
                        @JsonProperty("actualStartTime") ZonedDateTime actualStartTime,
                        @JsonProperty("actualEndTime") ZonedDateTime actualEndTime) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record ReassignDoctorRequest(
                        @Min(1) @JsonProperty("appointmentId") long appointmentId,
                        @Min(1) @JsonProperty("newDoctorId") long newDoctorId) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record FindClosestBranchesRequest(
                        @NotNull @JsonProperty("latitude") Double latitude,
                        @NotNull @JsonProperty("longitude") Double longitude,
                        @Min(1) @JsonProperty("limit") int limit) {
        }

        // ==========================================
        // CLINIC VISITS (HISTORY & RECORDS)
        // ==========================================

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record RecordClinicVisitRequest(
                        @Min(1) @JsonProperty("appointmentId") long appointmentId,
                        @NotNull @JsonProperty("userId") UUID userId,
                        @Min(1) @JsonProperty("doctorId") long doctorId,
                        @JsonProperty("visitNotes") String visitNotes,
                        @JsonProperty("prescriptionDetails") String prescriptionDetails) {
        }
}