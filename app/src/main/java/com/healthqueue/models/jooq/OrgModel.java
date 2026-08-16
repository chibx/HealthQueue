package com.healthqueue.models.jooq;

import static com.healthqueue.db.Tables.DOCTORS;
import static com.healthqueue.db.Tables.ORGANIZATIONS;
import static com.healthqueue.db.Tables.ORGANIZATION_REQUESTS;
import static com.healthqueue.db.Tables.ORG_REFRESH_TOKENS;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.jooq.DSLContext;

import com.healthqueue.models.Types.*;
import com.healthqueue.models.Interfaces.Organizations;
import com.healthqueue.models.Values.CreateOrg;
import com.healthqueue.models.Values.GetOrgLogin;

public class OrgModel implements Organizations {
    final DSLContext dsl;

    public OrgModel(DSLContext dsl) {
        this.dsl = dsl;
    }

    public CreateOrg createAndReturn(CreateOrgParams param) throws DatabaseException {
        try {
            return dsl.insertInto(ORGANIZATIONS)
                    .set(ORGANIZATIONS.ID, param.id)
                    .set(ORGANIZATIONS.NAME, param.name)
                    .set(ORGANIZATIONS.EMAIL, param.email)
                    .set(ORGANIZATIONS.PASSWORD_HASH, param.passwordHash)
                    .returningResult(ORGANIZATIONS.ID)
                    .fetchOne((r) -> {
                        return new CreateOrg(r.value1());
                    });
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void createSession(CreateOrgSessionParams params) throws DatabaseException {
        try {
            dsl.insertInto(ORG_REFRESH_TOKENS)
                    .columns(ORG_REFRESH_TOKENS.ORGANIZATION_ID, ORG_REFRESH_TOKENS.TOKEN,
                            ORG_REFRESH_TOKENS.IP, ORG_REFRESH_TOKENS.EXPIRES_AT)
                    .values(params.id, params.token, params.ipAddr,
                            OffsetDateTime.ofInstant(params.expiryDate, ZoneId.systemDefault()))
                    .execute();

        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean hasRegistrationCode(String email) throws DatabaseException {
        try {
            return dsl.fetchExists(dsl.select(ORGANIZATION_REQUESTS.REGISTRATION_CODE)
                    .from(ORGANIZATION_REQUESTS)
                    .where(ORGANIZATION_REQUESTS.EMAIL.eq(email)));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public GetOrgLogin getOrgLogin(String email) throws DatabaseException {
        try {
            return dsl.select(ORGANIZATIONS.ID, ORGANIZATIONS.PASSWORD_HASH)
                    .from(ORGANIZATIONS)
                    .where(ORGANIZATIONS.EMAIL.eq(email))
                    .fetchOne(r -> {
                        return new GetOrgLogin(r.value1(), r.value2());
                    });
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void deleteSession(UUID id, String token) throws DatabaseException {
        try {
            dsl.deleteFrom(ORG_REFRESH_TOKENS)
                    .where(ORG_REFRESH_TOKENS.ORGANIZATION_ID.eq(id), ORG_REFRESH_TOKENS.TOKEN.eq(token));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean exists(UUID id) throws DatabaseException {
        return dsl.fetchExists(dsl.selectOne().from(ORGANIZATIONS).where(ORGANIZATIONS.ID.eq(id)));
    }
}
