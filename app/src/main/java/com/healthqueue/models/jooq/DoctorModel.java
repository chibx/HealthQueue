package com.healthqueue.models.jooq;

import static com.healthqueue.db.Tables.BRANCHES;
import static com.healthqueue.db.Tables.DOCTORS;
import static com.healthqueue.db.Tables.ORGANIZATIONS;

import java.util.UUID;

import org.jooq.DSLContext;

import com.healthqueue.models.Interfaces;
import com.healthqueue.models.Types.CreateStaffParams;
import com.healthqueue.models.Types.DatabaseException;

public class DoctorModel implements Interfaces.Doctors {
    final DSLContext dsl;

    public DoctorModel(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void create(CreateStaffParams params) throws DatabaseException {
        try {
            dsl.insertInto(DOCTORS)
                    .columns(DOCTORS.BRANCH_ID, DOCTORS.FIRST_NAME, DOCTORS.LAST_NAME, DOCTORS.SPECIALTY)
                    .values(params.branchId, params.firstName, params.lastName, params.specialty);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void delete(UUID orgId, long branchId, long doctorId) throws DatabaseException {
        dsl.deleteFrom(DOCTORS).where(DOCTORS.ID.eq(doctorId)).andExists(
                dsl.selectOne().from(BRANCHES).where(BRANCHES.ID.eq(branchId).and(BRANCHES.ORGANIZATION_ID.eq(orgId))));
    }

    public void updateBranch(UUID orgId, long newBranchId, long oldBranchId, long doctorId) throws DatabaseException {

    }
}
