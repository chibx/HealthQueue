package com.healthqueue.cache;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthqueue.models.Values.Appointment;
import com.healthqueue.models.Values.Doctor;
import com.healthqueue.models.Values.GetOrgLogin;
import com.healthqueue.models.Values.GetPatientLogin;
import com.healthqueue.models.Values.GetNearestBranches;
import com.healthqueue.models.jooq.HealthQueueDB;
import com.healthqueue.utils.Utils;
import com.healthqueue.utils.Utils.GoReturn;
import com.healthqueue.utils.ServerResponse.ClinicVisitResponse;

import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.SetParams;

public class CacheManager {
    private final RedisClient client;
    private final HealthQueueDB db = Utils.getDB();
    private final SingleFlight<String, GoReturn<Object>> singleFlight = new SingleFlight<>();
    private final ObjectMapper mapper = Utils.MAPPER;

    public static class CacheException extends Exception {
        public CacheException(Throwable cause) {
            super(cause);
        }
    }

    public CacheManager(RedisClient client) {
        this.client = client;
    }

    private <T> T getCached(String cacheKey, JavaType type, java.util.concurrent.Callable<T> fetch)
            throws CacheException {
        return getCached(cacheKey, type, fetch, com.healthqueue.utils.Constants.MINUTES_10);
    }

    private <T> T getCached(String cacheKey, JavaType type, java.util.concurrent.Callable<T> fetch,
            Integer expiryMs)
            throws CacheException {
        try {
            GoReturn<?> flightResult = singleFlight.doCall(cacheKey, () -> Utils.tryGo(() -> {
                String value = client.get(cacheKey);
                if (value == null) {
                    T fetched = fetch.call();
                    Utils.executeAsync(() -> client.set(cacheKey, mapper.writeValueAsString(fetched),
                            SetParams.setParams().px(expiryMs)));
                    return fetched;
                }
                return mapper.readValue(value, type);
            }));

            if (flightResult.error != null) {
                throw new CacheException(flightResult.error);
            }

            T result = (T) flightResult.value;
            return result;
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    public List<GetNearestBranches> getNearestBranches(double userLongitude, double userLatitude)
            throws CacheException {
        return getNearestBranches(userLongitude, userLatitude, com.healthqueue.utils.Constants.DEFAULT_LIMIT);
    }

    public List<GetNearestBranches> getNearestBranches(double userLongitude, double userLatitude, Integer limit)
            throws CacheException {
        return getCached(
                Constants.GET_NEAREST_BRANCHES(userLongitude, userLatitude, limit),
                mapper.getTypeFactory().constructCollectionType(List.class, GetNearestBranches.class),
                () -> db.organizations().getNearestBranches(userLongitude, userLatitude));
    }

    public boolean patientExists(UUID id) throws CacheException {
        return getCached(Constants.PATIENT_EXISTS(id), mapper.constructType(Boolean.class),
                () -> db.patients().exists(id));
    }

    public boolean organizationExists(UUID id) throws CacheException {
        return getCached(Constants.ORG_EXISTS(id), mapper.constructType(Boolean.class),
                () -> db.organizations().exists(id));
    }

    public boolean checkDoctorAvailability(long doctorId, ZonedDateTime startTime, ZonedDateTime endTime)
            throws CacheException {
        return getCached(Constants.CHECK_DOCTOR_AVAILABILITY(doctorId, startTime, endTime),
                mapper.constructType(Boolean.class),
                () -> db.doctors().checkDoctorAvailability(doctorId, startTime, endTime));
    }

    public Doctor getDoctorDetails(long doctorId) throws CacheException {
        return getCached(Constants.GET_DOCTOR_DETAILS(doctorId), mapper.constructType(Doctor.class),
                () -> db.doctors().getDoctorDetails(doctorId));
    }

    public Appointment getAppointmentDetails(long appointmentId) throws CacheException {
        return getCached(Constants.GET_APPOINTMENT_DETAILS(appointmentId), mapper.constructType(Appointment.class),
                () -> db.appointments().getAppointmentDetails(appointmentId));
    }

    public List<@NonNull ClinicVisitResponse> getPatientVisitHistory(UUID userId) throws CacheException {
        return getCached(Constants.GET_PATIENT_VISIT_HISTORY(userId),
                mapper.getTypeFactory().constructCollectionType(List.class, ClinicVisitResponse.class),
                () -> db.clinicVisits().getPatientVisitHistory(userId));
    }
}
