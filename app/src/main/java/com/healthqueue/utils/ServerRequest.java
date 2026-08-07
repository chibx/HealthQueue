package com.healthqueue.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
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

        public static record PatientSignupRequest(
                        @NotBlank @JsonProperty("firstName") String firstName,
                        @NotBlank @JsonProperty("lastName") String lastName,
                        @Email @JsonProperty("email") String email,
                        @NotBlank @Size(min = 8, max = 15) @JsonProperty("phoneNumber") String phoneNumber,
                        @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) @JsonProperty("password") String password,
                        @NotNull @JsonProperty("latitude") Double latitude,
                        @NotNull @JsonProperty("longitude") Double longitude) {
        }

        public static record PatientLoginRequest(
                        @Email @JsonProperty("email") String email,
                        @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) @JsonProperty("password") String password) {
        }

        // ==========================================
        // ORGANIZATION REQUESTS
        // ==========================================

        public static record OrganizationSignupRequest(
                        @NotBlank @JsonProperty("name") String name,
                        @NotBlank @JsonProperty("registrationCode") String registrationCode,
                        @Email @JsonProperty("email") String email,
                        @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) @JsonProperty("password") String password) {
        }

        public static record OrganizationLoginRequest(
                        @Email @JsonProperty("email") String email,
                        @NotBlank @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH) @JsonProperty("password") String password) {
        }

        // ==========================================
        // BRANCH & DOCTOR MANAGEMENT REQUESTS
        // ==========================================

        public static record CreateBranchRequest(
                        @NotNull @JsonProperty("organizationId") UUID organizationId,
                        @NotBlank @JsonProperty("name") String name,
                        @NotBlank @JsonProperty("address") String address,
                        @NotNull @JsonProperty("latitude") Double latitude,
                        @NotNull @JsonProperty("longitude") Double longitude) {
        }

        public static record CreateDoctorRequest(
                        @Min(1) @JsonProperty("branchId") long branchId,
                        @NotBlank @JsonProperty("firstName") String firstName,
                        @NotBlank @JsonProperty("lastName") String lastName,
                        @NotBlank @JsonProperty("specialty") String specialty) {
        }

        // ==========================================
        // APPOINTMENT & QUEUE REQUESTS
        // ==========================================

        public static record CreateAppointmentRequest(
                        @NotNull @JsonProperty("userId") UUID userId,
                        @Min(1) @JsonProperty("branchId") long branchId,
                        @Min(1) @JsonProperty("doctorId") long doctorId,
                        @NotNull @JsonProperty("scheduledStartTime") ZonedDateTime scheduledStartTime,
                        @NotNull @JsonProperty("scheduledEndTime") ZonedDateTime scheduledEndTime) {
        }

        public static record UpdateAppointmentStatusRequest(
                        @Min(1) @JsonProperty("appointmentId") long appointmentId,
                        @NotBlank @JsonProperty("status") String status,
                        @JsonProperty("actualStartTime") ZonedDateTime actualStartTime,
                        @JsonProperty("actualEndTime") ZonedDateTime actualEndTime) {
        }

        public static record ReassignDoctorRequest(
                        @Min(1) @JsonProperty("appointmentId") long appointmentId,
                        @Min(1) @JsonProperty("newDoctorId") long newDoctorId) {
        }

        public static record FindClosestBranchesRequest(
                        @NotNull @JsonProperty("latitude") Double latitude,
                        @NotNull @JsonProperty("longitude") Double longitude,
                        @Min(1) @JsonProperty("limit") int limit) {
        }

        // ==========================================
        // CLINIC VISITS (HISTORY & RECORDS)
        // ==========================================

        public static record RecordClinicVisitRequest(
                        @Min(1) @JsonProperty("appointmentId") long appointmentId,
                        @NotNull @JsonProperty("userId") UUID userId,
                        @Min(1) @JsonProperty("doctorId") long doctorId,
                        @JsonProperty("visitNotes") String visitNotes,
                        @JsonProperty("prescriptionDetails") String prescriptionDetails) {
        }
}