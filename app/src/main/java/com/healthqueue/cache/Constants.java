package com.healthqueue.cache;

import java.time.ZonedDateTime;
import java.util.UUID;

class Constants {
    static final int MINUTES_10 = 10 * 60;
    static final int MINUTES_15 = 15 * 60;
    static final int MINUTES_30 = 30 * 60;
    static final int HOUR_1 = 60 * 60;

    static String GET_NEAREST_BRANCHES(double longitude, double latitude) {
        return String.format("get-n-b:%f:%f", longitude, latitude);
    }

    static String GET_PATIENT_LOGIN(String email) {
        return "get-patient-login:" + email;
    }

    static String PATIENT_EXISTS(UUID id) {
        return "patient-exists:" + id;
    }

    static String HAS_REGISTRATION_CODE(String email) {
        return "has-registration-code:" + email;
    }

    static String GET_ORG_LOGIN(String email) {
        return "get-org-login:" + email;
    }

    static String ORG_EXISTS(UUID id) {
        return "org-exists:" + id;
    }

    static String CHECK_DOCTOR_AVAILABILITY(long doctorId, ZonedDateTime startTime, ZonedDateTime endTime) {
        return String.format("check-doctor-availability:%d:%s:%s", doctorId,
                startTime.toInstant(), endTime.toInstant());
    }

    static String GET_DOCTOR_DETAILS(long doctorId) {
        return "get-doctor-details:" + doctorId;
    }

    static String GET_APPOINTMENT_DETAILS(long appointmentId) {
        return "get-appointment-details:" + appointmentId;
    }

    static String GET_PATIENT_VISIT_HISTORY(UUID userId) {
        return "get-patient-visit-history:" + userId;
    }

}
