package com.healthqueue.models;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.healthqueue.db.enums.AppointmentStatus;
import com.healthqueue.models.Types.*;
import com.healthqueue.models.Values.*;
import com.healthqueue.models.jooq.HealthQueueDB;

public class Interfaces {
    @FunctionalInterface
    public interface TransactionFn {
        void run(HealthQueueDB db) throws Exception;
    }

    public interface Patients {
        CreatePatient createAndReturn(CreatePatientParams params) throws DatabaseException;

        void createSession(CreatePatientSessionParams params) throws DatabaseException;

        GetPatientLogin getPatientLogin(String email) throws DatabaseException;

        void deleteSession(UUID id, String token) throws DatabaseException;

        boolean exists(UUID id) throws DatabaseException;
    };

    public interface Organizations {
        CreateOrg createAndReturn(CreateOrgParams params) throws DatabaseException;

        void createSession(CreateOrgSessionParams params) throws DatabaseException;

        boolean hasRegistrationCode(String email) throws DatabaseException;

        GetOrgLogin getOrgLogin(String email) throws DatabaseException;

        void deleteSession(UUID id, String token) throws DatabaseException;

        boolean exists(UUID id) throws DatabaseException;

    }

    public interface Doctors {
        void create(CreateStaffParams params) throws DatabaseException;

        void delete(UUID orgId, long branchId, long doctorId) throws DatabaseException;

        void updateBranch(UUID orgId, long newBranchId, long oldBranchId, long doctorId) throws DatabaseException;

        boolean checkDoctorAvailability(long doctorId, ZonedDateTime startTime, ZonedDateTime endTime)
                throws DatabaseException;

        Doctor getDoctorDetails(long doctorId) throws DatabaseException;
    }

    public interface Appointments {
        Appointment createAndReturn(CreateAppointmentParams params) throws DatabaseException;

        void updateStatus(UpdateAppointmentParams params) throws DatabaseException;

        void reassignDoctor(ReassignDoctorParams params) throws DatabaseException;

        Appointment getById(long appointmentId) throws DatabaseException;
    }

    public interface ClinicVisits {
        void recordVisit(RecordClinicVisitParams params) throws DatabaseException;
    }

    public interface HealthQueueDatabase {
        public Patients patients();

        public Organizations organizations();

        public Doctors doctors();

        public Appointments appointments();

        public ClinicVisits clinicVisits();

        public void runTx(TransactionFn fn) throws DatabaseException;
    }

    @lombok.Builder
    public static class Appointment {
        public long id;
        public UUID userId;
        public long branchId;
        public long doctorId;
        public AppointmentStatus status;
        public ZonedDateTime scheduledStartTime;
        public ZonedDateTime scheduledEndTime;
        public ZonedDateTime actualStartTime;
        public ZonedDateTime actualEndTime;
    }

    @lombok.Builder
    public static class Doctor {
        public long id;
        public long branchId;
        public String firstName;
        public String lastName;
        public String specialty;
        public boolean isAvailable;
    }
}
