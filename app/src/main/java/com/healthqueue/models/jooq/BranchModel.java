package com.healthqueue.models.jooq;

import static com.healthqueue.db.Tables.BRANCHES;

import java.util.UUID;
import java.math.BigDecimal;

import org.jooq.DSLContext;

import com.healthqueue.models.Types.DatabaseException;
import com.healthqueue.utils.ServerRequest.CreateBranchRequest;

public class BranchModel {
    private final DSLContext dsl;

    public BranchModel(DSLContext dsl) {
        this.dsl = dsl;
    }

    public long create(UUID organizationId, CreateBranchRequest request, long id) throws DatabaseException {
        try {
            return dsl.insertInto(BRANCHES)
                    .columns(BRANCHES.ID, BRANCHES.ORGANIZATION_ID, BRANCHES.NAME, BRANCHES.ADDRESS,
                            BRANCHES.LATITUDE, BRANCHES.LONGITUDE)
                    .values(id, organizationId, request.name(), request.address(),
                            BigDecimal.valueOf(request.latitude()), BigDecimal.valueOf(request.longitude()))
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean belongsToOrganization(long branchId, UUID organizationId) throws DatabaseException {
        try {
            return dsl.fetchExists(dsl.selectOne().from(BRANCHES)
                    .where(BRANCHES.ID.eq(branchId), BRANCHES.ORGANIZATION_ID.eq(organizationId)));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}