package com.healthqueue.models.jooq;

import static com.healthqueue.db.Tables.APPOINTMENTS;
import static com.healthqueue.db.Tables.BRANCHES;
import static com.healthqueue.db.Tables.DOCTORS;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jspecify.annotations.Nullable;

import com.healthqueue.db.enums.AppointmentStatus;
import com.healthqueue.models.Interfaces;
import com.healthqueue.models.Types.CreateStaffParams;
import com.healthqueue.models.Types.DatabaseException;
import com.healthqueue.models.Interfaces.Doctor;

public class DoctorModel implements Interfaces.Doctors {
    final DSLContext dsl;

    public DoctorModel(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void create(CreateStaffParams params) throws DatabaseException {
        try {
            dsl.insertInto(DOCTORS)
                    .columns(DOCTORS.ID, DOCTORS.BRANCH_ID, DOCTORS.FIRST_NAME, DOCTORS.LAST_NAME, DOCTORS.SPECIALTY)
                    .values(params.id, params.branchId, params.firstName, params.lastName, params.specialty)
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void delete(UUID orgId, long branchId, long doctorId) throws DatabaseException {
        try {
            dsl.deleteFrom(DOCTORS).where(DOCTORS.ID.eq(doctorId)).andExists(
                    dsl.selectOne().from(BRANCHES)
                            .where(BRANCHES.ID.eq(branchId).and(BRANCHES.ORGANIZATION_ID.eq(orgId))))
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void updateBranch(UUID orgId, long newBranchId, long oldBranchId, long doctorId) throws DatabaseException {
        try {
            dsl.update(DOCTORS)
                    .set(DOCTORS.BRANCH_ID, newBranchId)
                    .where(DOCTORS.ID.eq(doctorId))
                    .andExists(dsl.selectOne().from(BRANCHES)
                            .where(BRANCHES.ID.eq(oldBranchId).and(BRANCHES.ORGANIZATION_ID.eq(orgId))))
                    .andExists(dsl.selectOne().from(BRANCHES)
                            .where(BRANCHES.ID.eq(newBranchId).and(BRANCHES.ORGANIZATION_ID.eq(orgId))))
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean checkDoctorAvailability(long doctorId, ZonedDateTime startTime, ZonedDateTime endTime)
            throws DatabaseException {
        try {
            // Check for overlapping appointments for the given doctor within the specified
            // time range
            @Nullable
            Integer count = dsl.selectCount()
                    .from(APPOINTMENTS)
                    .where(APPOINTMENTS.DOCTOR_ID.eq(doctorId))
                    .and(APPOINTMENTS.STATUS.ne(AppointmentStatus.CANCELLED))
                    .and(APPOINTMENTS.SCHEDULED_START_TIME
                            .lessThan(OffsetDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault())))
                    .and(APPOINTMENTS.SCHEDULED_END_TIME
                            .greaterThan(OffsetDateTime.ofInstant(startTime.toInstant(), ZoneId.systemDefault())))
                    .fetchOneInto(Integer.class);

            return count == null || count == 0;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Doctor getDoctorDetails(long doctorId) throws DatabaseException {
        try {
            return dsl.selectFrom(DOCTORS)
                    .where(DOCTORS.ID.eq(doctorId))
                    .fetchOne(r -> {
                        return Doctor.builder()
                                .id(r.getId())
                                .branchId(r.getBranchId())
                                .firstName(r.getFirstName())
                                .lastName(r.getLastName())
                                .specialty(r.getSpecialty())
                                .isAvailable(r.getIsAvailable())
                                .build();
                    });
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
