package com.healthqueue.controllers;

import static com.healthqueue.utils.Constants.*;

import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.healthqueue.utils.AuthContext.UserCtx;
import com.healthqueue.utils.ServerError;
import com.healthqueue.utils.ServerRequest.CreateBranchRequest;
import com.healthqueue.utils.ServerRequest.FindClosestBranchesRequest;
import com.healthqueue.utils.Utils;

import jakarta.validation.ConstraintViolation;
import spark.Request;
import spark.Response;

public class BranchController {
    public static Object CreateBranch(Request request, Response response) throws Exception {
        @Nullable
        UserCtx orgCtx = request.attribute(ORG_CTX);
        if (orgCtx == null)
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        CreateBranchRequest body = Utils.fromJSON(request.body(), CreateBranchRequest.class);
        Set<ConstraintViolation<CreateBranchRequest>> violations = Utils.validate(body);

        if (!violations.isEmpty())
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));

        if (!orgCtx.uuid().equals(body.organizationId()))
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);

        Utils.getDB().branches().create(orgCtx.uuid(), body, Utils.nextSnowflakeId());

        return Utils.structuredResponse(STATUS_OK, "Branch created successfully");
    }

    public static Object FindClosest(Request request, Response response) throws Exception {
        String latitude = request.queryParams("latitude");
        String longitude = request.queryParams("longitude");
        String limit = request.queryParams("limit");
        if (latitude == null || longitude == null || limit == null) {
            throw new ServerError(STATUS_BAD_REQUEST, "latitude, longitude, and limit are required.");
        }

        final FindClosestBranchesRequest body;
        try {
            body = new FindClosestBranchesRequest(Double.valueOf(latitude), Double.valueOf(longitude),
                    Integer.parseInt(limit));
        } catch (NumberFormatException e) {
            throw new ServerError(STATUS_BAD_REQUEST, "latitude, longitude, and limit must be valid numbers.");
        }
        Set<ConstraintViolation<FindClosestBranchesRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty())
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));

        if (body.limit() < 5) {
            throw new ServerError(STATUS_BAD_REQUEST, "Payload limit cannot be less than 5.");
        }

        if (body.limit() > 30) {
            throw new ServerError(STATUS_BAD_REQUEST, "Payload limit cannot be greater than 30.");
        }
        var branches = Utils.cache().getNearestBranches(body.longitude(), body.latitude(), body.limit());
        return Utils.structuredResponse(STATUS_OK, "Closest branches retrieved successfully",
                branches.stream().limit(body.limit()).toList());
    }
}
