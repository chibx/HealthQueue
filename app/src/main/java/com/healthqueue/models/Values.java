package com.healthqueue.models;

import java.util.UUID;

public class Values {
    public static class CreatePatient {
        public final UUID id;

        public CreatePatient(UUID uuid) {
            this.id = uuid;
        }
    }

    public static class CreateOrg {
        public final UUID id;

        public CreateOrg(UUID uuid) {
            this.id = uuid;
        }
    }

    public static class GetPatientLogin {
        public final UUID id;
        public final String passwordHash;

        public GetPatientLogin(UUID id, String password) {
            this.id = id;
            this.passwordHash = password;
        }
    }

    public static class GetOrgLogin {
        public final UUID id;
        public final String passwordHash;

        public GetOrgLogin(UUID id, String password) {
            this.id = id;
            this.passwordHash = password;
        }
    }

    public static class GetNearestBranches {
        public final UUID id;
        public final String name;
        public final String address;
        public final double distanceInMeters;

        public GetNearestBranches(UUID id, String name, String address, double distance) {
            this.id = id;
            this.name = this.address = address;
        }
    }
}
