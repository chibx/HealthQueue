package com.healthqueue.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public class ServerResponse {

        // ==========================================
        // BASE STRUCTURES & ERRORS
        // ==========================================

        public record ValidationError(
                        @JsonProperty("field") String field,
                        @JsonProperty("message") String message) {
        }

        public record StructuredResponse<T>(
                        @JsonProperty("status") int status,
                        @JsonProperty("message") String message,
                        @JsonProperty("data") @Nullable T data) {
        }

        public record StructuredError400(
                        @JsonProperty("status") int status,
                        @JsonProperty("message") String message,
                        @JsonProperty("errors") ValidationError[] errors) {
        }

        public record StructuredErrorAny(
                        @JsonProperty("status") int status,
                        @JsonProperty("message") String message) {
        }

        // ==========================================
        // AUTHENTICATION RESPONSES
        // ==========================================

        public record AuthResponse(
                        @JsonProperty("token") String token,
                        @JsonProperty("id") String id, // Can be UUID for user/org
                        @JsonProperty("role") String role) {
        }

        // ==========================================
        // ENTITY RESPONSES
        // ==========================================

        public record UserResponse(
                        @JsonProperty("id") UUID id,
                        @JsonProperty("firstName") String firstName,
                        @JsonProperty("lastName") String lastName,
                        @JsonProperty("email") String email) {
        }

        public record OrganizationResponse(
                        @JsonProperty("id") UUID id,
                        @JsonProperty("name") String name,
                        @JsonProperty("registrationCode") String registrationCode) {
        }

        public record BranchResponse(
                        @JsonProperty("id") long id,
                        @JsonProperty("organizationId") UUID organizationId,
                        @JsonProperty("name") String name,
                        @JsonProperty("address") String address,
                        @JsonProperty("latitude") Double latitude,
                        @JsonProperty("longitude") Double longitude) {
        }

        public record ClosestBranchResponse(
                        @JsonProperty("branch") BranchResponse branch,
                        @JsonProperty("distanceInKilometers") Double distanceInKilometers) {
        }

        public record DoctorResponse(
                        @JsonProperty("id") long id,
                        @JsonProperty("branchId") long branchId,
                        @JsonProperty("firstName") String firstName,
                        @JsonProperty("lastName") String lastName,
                        @JsonProperty("specialty") String specialty,
                        @JsonProperty("isAvailable") boolean isAvailable) {
        }

        // ==========================================
        // APPOINTMENT & QUEUE RESPONSES
        // ==========================================

        public record AppointmentResponse(
                        @JsonProperty("id") long id,
                        @JsonProperty("userId") UUID userId,
                        @JsonProperty("branchId") long branchId,
                        @JsonProperty("doctorId") long doctorId,
                        @JsonProperty("status") String status,
                        @JsonProperty("scheduledStartTime") ZonedDateTime scheduledStartTime,
                        @JsonProperty("scheduledEndTime") ZonedDateTime scheduledEndTime,
                        @JsonProperty("actualStartTime") ZonedDateTime actualStartTime,
                        @JsonProperty("actualEndTime") ZonedDateTime actualEndTime) {
        }

        public record ClinicVisitResponse(
                        @JsonProperty("id") long id,
                        @JsonProperty("appointmentId") long appointmentId,
                        @JsonProperty("userId") UUID userId,
                        @JsonProperty("doctorId") long doctorId,
                        @JsonProperty("visitNotes") String visitNotes,
                        @JsonProperty("prescriptionDetails") String prescriptionDetails,
                        @JsonProperty("recordedAt") ZonedDateTime recordedAt) {
        }
}