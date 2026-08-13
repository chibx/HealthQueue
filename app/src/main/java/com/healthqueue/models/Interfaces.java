package com.healthqueue.models;

import com.healthqueue.models.Types.*;
import com.healthqueue.models.Values.*;
import com.healthqueue.models.jooq.HealthQueueDB;

public class Interfaces {
    @FunctionalInterface
    public interface TransactionFn {
        void run(HealthQueueDB db);
    }

    public interface Patients {
        CreatePatient createAndReturn(CreatePatientParams params) throws DatabaseException;

        void createSession(CreatePatientSessionParams params) throws DatabaseException;

        GetPatientLogin getPatientLogin(String email) throws DatabaseException;
    };

    public interface Organizations {
        CreateOrg createAndReturn(CreateOrgParams params) throws DatabaseException;

        void createSession(CreateOrgSessionParams params) throws DatabaseException;

        boolean hasRegistrationCode(String email) throws DatabaseException;

        GetOrgLogin getOrgLogin(String email) throws DatabaseException;
    }

    public interface HealthQueueDatabase {
        public Patients patients();

        public Organizations organizations();

        public void runTx(TransactionFn fn);
    }
}
