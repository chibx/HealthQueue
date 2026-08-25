package com.healthqueue.models.jooq;

import static com.healthqueue.db.Tables.CLINIC_VISITS;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.jooq.DSLContext;

import com.healthqueue.models.Interfaces;
import com.healthqueue.models.Types.DatabaseException;
import com.healthqueue.models.Types.RecordClinicVisitParams;

public class ClinicVisitModel implements Interfaces.ClinicVisits {
    final DSLContext dsl;

    public ClinicVisitModel(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void recordVisit(RecordClinicVisitParams params) throws DatabaseException {
        try {
            dsl.insertInto(CLINIC_VISITS)
                    .set(CLINIC_VISITS.ID, params.id)
                    .set(CLINIC_VISITS.APPOINTMENT_ID, params.appointmentId)
                    .set(CLINIC_VISITS.USER_ID, params.userId)
                    .set(CLINIC_VISITS.DOCTOR_ID, params.doctorId)
                    .set(CLINIC_VISITS.VISIT_NOTES, params.visitNotes)
                    .set(CLINIC_VISITS.PRESCRIPTION_DETAILS, params.prescriptionDetails)
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
