package com.healthqueue.cache;

class Constants {
    static String GET_NEAREST_BRANCHES(double longitude, double latitude) {
        return String.format("get-n-b:%f:%f", longitude, latitude);
    }

}
