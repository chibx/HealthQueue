package com.healthqueue.models.jooq;

import static com.healthqueue.db.Tables.APPOINTMENTS;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.jooq.DSLContext;

import com.healthqueue.models.Types.CreateAppointmentParams;
import com.healthqueue.models.Types.DatabaseException;
import com.healthqueue.models.Types.ReassignDoctorParams;
import com.healthqueue.models.Types.UpdateAppointmentParams;
import com.healthqueue.models.Values.Appointment;

public class AppointmentModel {
    final DSLContext dsl;

    public AppointmentModel(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Appointment createAndReturn(CreateAppointmentParams params) throws DatabaseException {
        try {
            return dsl.insertInto(APPOINTMENTS)
                    .set(APPOINTMENTS.ID, params.id)
                    .set(APPOINTMENTS.USER_ID, params.userId)
                    .set(APPOINTMENTS.BRANCH_ID, params.branchId)
                    .set(APPOINTMENTS.DOCTOR_ID, params.doctorId)
                    .set(APPOINTMENTS.SCHEDULED_START_TIME,
                            OffsetDateTime.ofInstant(params.scheduledStartTime.toInstant(), ZoneId.systemDefault()))
                    .set(APPOINTMENTS.SCHEDULED_END_TIME,
                            OffsetDateTime.ofInstant(params.scheduledEndTime.toInstant(), ZoneId.systemDefault()))
                    .set(APPOINTMENTS.STATUS, params.status)
                    .returning()
                    .fetchOne(r -> {
                        return Appointment.builder()
                                .id(r.getId())
                                .userId(r.getUserId())
                                .branchId(r.getBranchId())
                                .doctorId(r.getDoctorId())
                                .status(r.getStatus())
                                .scheduledStartTime(r.getScheduledStartTime().atZoneSameInstant(ZoneId.systemDefault()))
                                .scheduledEndTime(r.getScheduledEndTime().atZoneSameInstant(ZoneId.systemDefault()))
                                .actualStartTime(r.getActualStartTime() != null
                                        ? r.getActualStartTime().atZoneSameInstant(ZoneId.systemDefault())
                                        : null)
                                .actualEndTime(r.getActualEndTime() != null
                                        ? r.getActualEndTime().atZoneSameInstant(ZoneId.systemDefault())
                                        : null)
                                .build();
                    });
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void updateStatus(UpdateAppointmentParams params) throws DatabaseException {
        try {
            dsl.update(APPOINTMENTS)
                    .set(APPOINTMENTS.STATUS, params.status)
                    .set(APPOINTMENTS.ACTUAL_START_TIME,
                            params.actualStartTime != null
                                    ? OffsetDateTime.ofInstant(params.actualStartTime.toInstant(),
                                            ZoneId.systemDefault())
                                    : null)
                    .set(APPOINTMENTS.ACTUAL_END_TIME,
                            params.actualEndTime != null
                                    ? OffsetDateTime.ofInstant(params.actualEndTime.toInstant(), ZoneId.systemDefault())
                                    : null)
                    .where(APPOINTMENTS.ID.eq(params.appointmentId))
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void reassignDoctor(ReassignDoctorParams params) throws DatabaseException {
        try {
            dsl.update(APPOINTMENTS)
                    .set(APPOINTMENTS.DOCTOR_ID, params.newDoctorId)
                    .where(APPOINTMENTS.ID.eq(params.appointmentId))
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public Appointment getAppointmentDetails(long appointmentId) throws DatabaseException {
        try {
            return dsl.selectFrom(APPOINTMENTS)
                    .where(APPOINTMENTS.ID.eq(appointmentId))
                    .fetchOne(r -> {
                        return Appointment.builder()
                                .id(r.getId())
                                .userId(r.getUserId())
                                .branchId(r.getBranchId())
                                .doctorId(r.getDoctorId())
                                .status(r.getStatus())
                                .scheduledStartTime(r.getScheduledStartTime().atZoneSameInstant(ZoneId.systemDefault()))
                                .scheduledEndTime(r.getScheduledEndTime().atZoneSameInstant(ZoneId.systemDefault()))
                                .actualStartTime(r.getActualStartTime() != null
                                        ? r.getActualStartTime().atZoneSameInstant(ZoneId.systemDefault())
                                        : null)
                                .actualEndTime(r.getActualEndTime() != null
                                        ? r.getActualEndTime().atZoneSameInstant(ZoneId.systemDefault())
                                        : null)
                                .build();
                    });
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public Appointment getAppointmentForOrganization(long appointmentId, UUID organizationId)
            throws DatabaseException {
        try {
            boolean exists = dsl.selectOne().from(APPOINTMENTS).join(com.healthqueue.db.Tables.BRANCHES)
                    .on(APPOINTMENTS.BRANCH_ID.eq(com.healthqueue.db.Tables.BRANCHES.ID))
                    .where(APPOINTMENTS.ID.eq(appointmentId),
                            com.healthqueue.db.Tables.BRANCHES.ORGANIZATION_ID.eq(organizationId))
                    .fetchOne() != null;
            return exists ? getAppointmentDetails(appointmentId) : null;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public int reassignDoctorForOrganization(long appointmentId, long newDoctorId, UUID organizationId)
            throws DatabaseException {
        try {
            return dsl.update(APPOINTMENTS)
                    .set(APPOINTMENTS.DOCTOR_ID, newDoctorId)
                    .where(APPOINTMENTS.ID.eq(appointmentId))
                    .andExists(dsl.selectOne().from(com.healthqueue.db.Tables.BRANCHES)
                            .where(com.healthqueue.db.Tables.BRANCHES.ID.eq(APPOINTMENTS.BRANCH_ID),
                                    com.healthqueue.db.Tables.BRANCHES.ORGANIZATION_ID.eq(organizationId)))
                    .andExists(dsl.selectOne().from(com.healthqueue.db.Tables.DOCTORS)
                            .where(com.healthqueue.db.Tables.DOCTORS.ID.eq(newDoctorId),
                                    com.healthqueue.db.Tables.DOCTORS.BRANCH_ID.eq(APPOINTMENTS.BRANCH_ID)))
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}