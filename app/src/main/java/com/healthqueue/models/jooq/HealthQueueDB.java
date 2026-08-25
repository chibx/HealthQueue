package com.healthqueue.models.jooq;

import org.jooq.DSLContext;

import com.healthqueue.models.Interfaces.TransactionFn;
import com.healthqueue.models.Types.DatabaseException;

public class HealthQueueDB {
    private final DSLContext dsl;
    private DoctorModel _doctors;
    private PatientModel _patients;
    private OrgModel _organizations;
    private AppointmentModel _appointments;
    private ClinicVisitModel _clinicVisits;

    public HealthQueueDB(DSLContext dsl) {
        this.dsl = dsl;
    }

    public PatientModel patients() {
        if (this._patients == null) {
            this._patients = new PatientModel(dsl);
        }
        return this._patients;
    }

    public DoctorModel doctors() {
        if (this._doctors == null) {
            this._doctors = new DoctorModel(dsl);
        }
        return this._doctors;
    }

    public OrgModel organizations() {
        if (this._organizations == null) {
            this._organizations = new OrgModel(dsl);
        }
        return this._organizations;
    }

    public AppointmentModel appointments() {
        if (this._appointments == null) {
            this._appointments = new AppointmentModel(this.dsl);
        }
        return this._appointments;
    }

    public ClinicVisitModel clinicVisits() {
        if (this._clinicVisits == null) {
            this._clinicVisits = new ClinicVisitModel(this.dsl);
        }
        return this._clinicVisits;
    }

    public void runTx(TransactionFn fn) throws DatabaseException {
        dsl.transaction((ctx) -> {
            DSLContext _dsl = ctx.dsl();
            fn.run(new HealthQueueDB(_dsl));
        });
    }
}
