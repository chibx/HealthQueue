package com.healthqueue.utils;

import com.fasterxml.jackson.databind.DatabindException;

import spark.Request;
import spark.Response;
import spark.Spark;

public class ExceptionHandler {
    public static void handle(Exception exception, Request req, Response res) {
        try {
            res.type("application/json");
            if (exception instanceof DatabindException) {
                String errorBody = Utils.structuredResponse(
                        Constants.STATUS_BAD_REQUEST,
                        Constants.BAD_REQUEST);

                Spark.halt(Constants.STATUS_BAD_REQUEST, errorBody);
            }
            if (exception instanceof ServerError) {
                Spark.halt(
                        ((ServerError) exception).status,
                        Utils.structuredResponse(
                                ((ServerError) exception).status,
                                exception.getMessage()));
            }
        } catch (Exception e) {
            Spark.halt(Constants.STATUS_INTERNAL_ERROR, Constants.INTERNAL_ERROR);
        }
    }
}
