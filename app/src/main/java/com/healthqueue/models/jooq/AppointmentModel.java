package com.healthqueue.models.jooq;

import static com.healthqueue.db.Tables.APPOINTMENTS;
import static com.healthqueue.db.Tables.DOCTORS;
import static com.healthqueue.db.Tables.BRANCHES;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.jooq.DSLContext;

import com.healthqueue.db.enums.AppointmentStatus;
import com.healthqueue.models.Interfaces;
import com.healthqueue.models.Types.CreateAppointmentParams;
import com.healthqueue.models.Types.DatabaseException;
import com.healthqueue.models.Types.ReassignDoctorParams;
import com.healthqueue.models.Types.UpdateAppointmentParams;
import com.healthqueue.models.Interfaces.Appointment;

public class AppointmentModel implements Interfaces.Appointments {
    final DSLContext dsl;

    public AppointmentModel(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
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

    @Override
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

    @Override
    public void reassignDoctor(ReassignDoctorParams params) throws DatabaseException {
        try {
            dsl.update(APPOINTMENTS)
                    .set(APPOINTMENTS.DOCTOR_ID, params.newDoctorId)
                    .set(APPOINTMENTS.STATUS, AppointmentStatus.SCHEDULED)
                    .where(APPOINTMENTS.ID.eq(params.appointmentId))
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Appointment getById(long appointmentId) throws DatabaseException {
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
}
