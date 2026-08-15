package com.healthqueue.models.jooq;

import org.jooq.DSLContext;

import com.healthqueue.models.Interfaces.*;
import com.healthqueue.models.Types.DatabaseException;

public class HealthQueueDB implements HealthQueueDatabase {
    private final DSLContext dsl;
    private PatientModel _patients = null;
    private OrgModel _organizations = null;

    public HealthQueueDB(DSLContext dsl) {
        this.dsl = dsl;
    }

    public PatientModel patients() {
        if (this._patients == null) {
            this._patients = new PatientModel(dsl);
        }
        return this._patients;
    }

    public OrgModel organizations() {
        if (this._organizations == null) {
            this._organizations = new OrgModel(dsl);
        }
        return this._organizations;
    }

    public void runTx(TransactionFn fn) throws DatabaseException {
        dsl.transaction((ctx) -> {
            DSLContext _dsl = ctx.dsl();
            fn.run(new HealthQueueDB(_dsl));
        });
    }
}
