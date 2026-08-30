package com.healthqueue.utils;

import com.fasterxml.jackson.databind.DatabindException;

import spark.Request;
import spark.Response;

public class ExceptionHandler {
    public static void handle(Exception exception, Request req, Response res) {
        try {
            res.type("application/json");
            if (exception instanceof DatabindException) {
                String errorBody = Utils.structuredResponse(
                        Constants.STATUS_BAD_REQUEST,
                        Constants.BAD_REQUEST);

                res.status(Constants.STATUS_BAD_REQUEST);
                res.body(errorBody);

                return;
            }
            if (exception instanceof ServerError) {
                res.status(((ServerError) exception).getStatusCode());
                res.body(Utils.structuredResponse(
                        ((ServerError) exception).getStatusCode(),
                        exception.getMessage()));

                return;
            }

            Utils.Logger.error("ExceptionHandler caught:", exception.getStackTrace().toString());
            res.status(Constants.STATUS_INTERNAL_ERROR);
            res.body(Utils.DEFAULT_500_RESP);
        } catch (Exception e) {
            res.status(Constants.STATUS_INTERNAL_ERROR);
            res.body(Utils.DEFAULT_500_RESP);
        }
    }
}
