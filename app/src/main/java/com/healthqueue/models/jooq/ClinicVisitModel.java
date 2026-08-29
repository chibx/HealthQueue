package com.healthqueue.models.jooq;

import static com.healthqueue.db.Tables.CLINIC_VISITS;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jspecify.annotations.NonNull;

import com.healthqueue.models.Types.DatabaseException;
import com.healthqueue.models.Types.RecordClinicVisitParams;
import com.healthqueue.utils.ServerResponse.ClinicVisitResponse;

public class ClinicVisitModel {
    final DSLContext dsl;

    public ClinicVisitModel(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void recordVisit(RecordClinicVisitParams params) throws DatabaseException {
        try {
            dsl.insertInto(CLINIC_VISITS)
                    .columns(CLINIC_VISITS.ID, CLINIC_VISITS.APPOINTMENT_ID, CLINIC_VISITS.USER_ID,
                            CLINIC_VISITS.DOCTOR_ID, CLINIC_VISITS.VISIT_NOTES, CLINIC_VISITS.PRESCRIPTION_DETAILS)
                    .values(params.id, params.appointmentId, params.userId, params.doctorId, params.visitNotes,
                            params.prescriptionDetails)
                    .execute();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public List<@NonNull ClinicVisitResponse> getPatientVisitHistory(UUID userId) throws DatabaseException {
        try {
            return dsl.selectFrom(CLINIC_VISITS)
                    .where(CLINIC_VISITS.USER_ID.eq(userId))
                    .orderBy(CLINIC_VISITS.RECORDED_AT.desc())
                    .fetch(r -> {
                        return new ClinicVisitResponse(
                                r.getId(),
                                r.getAppointmentId(),
                                r.getUserId(),
                                r.getDoctorId(),
                                r.getVisitNotes(),
                                r.getPrescriptionDetails(),
                                r.getRecordedAt().atZoneSameInstant(ZoneId.systemDefault()));
                    });
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
