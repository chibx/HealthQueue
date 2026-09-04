package com.healthqueue.models.jooq;

import static com.healthqueue.db.Tables.APPOINTMENTS;
import static com.healthqueue.db.Tables.BRANCHES;
import static com.healthqueue.db.Tables.DOCTORS;
import static com.healthqueue.db.Tables.DOCTOR_REFRESH_TOKENS;
import static com.healthqueue.db.Tables.ORGANIZATIONS;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jspecify.annotations.Nullable;

import com.healthqueue.db.enums.AppointmentStatus;
import com.healthqueue.models.Types.CreateStaffParams;
import com.healthqueue.models.Types.CreateDoctorSessionParams;
import com.healthqueue.models.Types.DatabaseException;
import com.healthqueue.models.Values.Doctor;
import com.healthqueue.models.Values.DoctorProfile;
import com.healthqueue.models.Values.GetDoctorLogin;

public class DoctorModel {
    final DSLContext dsl;

    public DoctorModel(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void create(CreateStaffParams params) throws DatabaseException {
        try {
            dsl.insertInto(DOCTORS)
                    .columns(DOCTORS.ID, DOCTORS.BRANCH_ID, DOCTORS.FIRST_NAME, DOCTORS.LAST_NAME, DOCTORS.SPECIALTY,
                            DOCTORS.EMAIL, DOCTORS.PASSWORD_HASH)
                    .values(params.id, params.branchId, params.firstName, params.lastName, params.specialty,
                            params.email, params.passwordHash)
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public GetDoctorLogin getDoctorLogin(String organizationName, String email) throws DatabaseException {
        try {
            return dsl.select(DOCTORS.ID, DOCTORS.PASSWORD_HASH).from(DOCTORS)
                    .join(BRANCHES).on(DOCTORS.BRANCH_ID.eq(BRANCHES.ID))
                    .join(ORGANIZATIONS).on(BRANCHES.ORGANIZATION_ID.eq(ORGANIZATIONS.ID))
                    .where(ORGANIZATIONS.NAME.eq(organizationName), DOCTORS.EMAIL.eq(email))
                    .fetchOne(r -> new GetDoctorLogin(r.get(DOCTORS.ID), r.get(DOCTORS.PASSWORD_HASH)));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void createSession(CreateDoctorSessionParams params) throws DatabaseException {
        try {
            dsl.insertInto(DOCTOR_REFRESH_TOKENS)
                    .columns(DOCTOR_REFRESH_TOKENS.DOCTOR_ID, DOCTOR_REFRESH_TOKENS.TOKEN, DOCTOR_REFRESH_TOKENS.IP,
                            DOCTOR_REFRESH_TOKENS.EXPIRES_AT)
                    .values(params.id, params.token, params.ipAddr,
                            OffsetDateTime.ofInstant(params.expiryDate, ZoneId.systemDefault()))
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public long getSessionDoctorId(String token) throws DatabaseException {
        try {
            @Nullable
            Long id = dsl.select(DOCTOR_REFRESH_TOKENS.DOCTOR_ID).from(DOCTOR_REFRESH_TOKENS)
                    .where(DOCTOR_REFRESH_TOKENS.TOKEN.eq(token)
                            .and(DOCTOR_REFRESH_TOKENS.EXPIRES_AT.gt(OffsetDateTime.now())))
                    .fetchOneInto(Long.class);
            return id == null ? 0 : id;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void deleteSession(long id, String token) throws DatabaseException {
        try {
            dsl.deleteFrom(DOCTOR_REFRESH_TOKENS)
                    .where(DOCTOR_REFRESH_TOKENS.DOCTOR_ID.eq(id), DOCTOR_REFRESH_TOKENS.TOKEN.eq(token)).execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean belongsToOrganization(long doctorId, UUID organizationId) throws DatabaseException {
        try {
            return dsl.fetchExists(dsl.selectOne().from(DOCTORS).join(BRANCHES)
                    .on(DOCTORS.BRANCH_ID.eq(BRANCHES.ID))
                    .where(DOCTORS.ID.eq(doctorId), BRANCHES.ORGANIZATION_ID.eq(organizationId)));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean belongsToBranch(long doctorId, long branchId) throws DatabaseException {
        try {
            return dsl.fetchExists(dsl.selectOne().from(DOCTORS)
                    .where(DOCTORS.ID.eq(doctorId), DOCTORS.BRANCH_ID.eq(branchId)));
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

    public DoctorProfile getProfile(long id) throws DatabaseException {
        try {
            return dsl.select(DOCTORS.ID, DOCTORS.FIRST_NAME, DOCTORS.LAST_NAME, DOCTORS.SPECIALTY, DOCTORS.EMAIL, ORGANIZATIONS.NAME)
                    .from(DOCTORS)
                    .join(BRANCHES).on(DOCTORS.BRANCH_ID.eq(BRANCHES.ID))
                    .join(ORGANIZATIONS).on(BRANCHES.ORGANIZATION_ID.eq(ORGANIZATIONS.ID))
                    .where(DOCTORS.ID.eq(id))
                    .fetchOne(r -> new DoctorProfile(
                            r.get(DOCTORS.ID),
                            r.get(DOCTORS.FIRST_NAME),
                            r.get(DOCTORS.LAST_NAME),
                            r.get(DOCTORS.SPECIALTY),
                            r.get(DOCTORS.EMAIL),
                            r.get(ORGANIZATIONS.NAME)));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
