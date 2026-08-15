package com.healthqueue.models.jooq;

import static com.healthqueue.db.Tables.USERS;
import static com.healthqueue.db.Tables.USER_REFRESH_TOKENS;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.jooq.DSLContext;

import com.healthqueue.models.Interfaces;
import com.healthqueue.models.Types.CreatePatientParams;
import com.healthqueue.models.Types.CreatePatientSessionParams;
import com.healthqueue.models.Types.DatabaseException;
import com.healthqueue.models.Values.CreatePatient;
import com.healthqueue.models.Values.GetPatientLogin;

public class PatientModel implements Interfaces.Patients {
    final DSLContext dsl;

    public PatientModel(DSLContext dsl) {
        this.dsl = dsl;
    }

    public CreatePatient createAndReturn(CreatePatientParams params) throws DatabaseException {
        try {
            return dsl.insertInto(USERS)
                    .set(USERS.ID, params.id)
                    .set(USERS.FIRST_NAME, params.firstName)
                    .set(USERS.LAST_NAME, params.lastName)
                    .set(USERS.EMAIL, params.email)
                    .set(USERS.PASSWORD_HASH, params.passwordHash)
                    .returningResult(USERS.ID)
                    .fetchOne((r) -> {
                        return new CreatePatient(r.value1());
                    });
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void createSession(CreatePatientSessionParams params) throws DatabaseException {
        try {
            dsl.insertInto(USER_REFRESH_TOKENS,
                    USER_REFRESH_TOKENS.USER_ID,
                    USER_REFRESH_TOKENS.TOKEN,
                    USER_REFRESH_TOKENS.IP,
                    USER_REFRESH_TOKENS.EXPIRES_AT)
                    .values(params.id, params.token, params.ipAddr,
                            OffsetDateTime.ofInstant(params.expiryDate, ZoneId.systemDefault()))
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public GetPatientLogin getPatientLogin(String email) throws DatabaseException {
        try {
            return dsl.select(USERS.ID, USERS.PASSWORD_HASH)
                    .from(USERS)
                    .where(USERS.EMAIL.eq(email))
                    .fetchOne(r -> {
                        return new GetPatientLogin(r.value1(), r.value2());
                    });
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void deleteSession(UUID id, String token) throws DatabaseException {
        try {
            dsl.deleteFrom(USER_REFRESH_TOKENS)
                    .where(USER_REFRESH_TOKENS.USER_ID.eq(id),
                            USER_REFRESH_TOKENS.TOKEN.eq(token));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean exists(UUID id) throws DatabaseException {
        return dsl.fetchExists(dsl.selectOne().from(USERS).where(USERS.ID.eq(id)));
    }
}
