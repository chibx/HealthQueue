package com.healthqueue;

import com.healthqueue.controllers.AuthController;
// Assuming you will create these controller classes:
import com.healthqueue.controllers.BranchController;
import com.healthqueue.controllers.DoctorController;
import com.fasterxml.jackson.databind.DatabindException;
import com.healthqueue.controllers.AppointmentController;
import com.healthqueue.controllers.VisitController;
import com.healthqueue.middlewares.Middlewares;
import com.healthqueue.utils.Constants;
import com.healthqueue.utils.Utils;

import spark.Spark;

public class App {
    public static void main(String[] args) {
        final String PORT = System.getenv("PORT");

        Spark.port(3000);

        Spark.exception(Exception.class, (exception, req, res) -> {
            try {
                res.type("application/json");
                if (exception instanceof DatabindException) {
                    String errorBody = Utils.structuredResponse(
                            Constants.STATUS_BAD_REQUEST,
                            Constants.BAD_REQUEST);
                    Spark.halt(Constants.STATUS_BAD_REQUEST, errorBody);
                }
            } catch (Exception e) {
                Spark.halt(Constants.STATUS_INTERNAL_ERROR, Constants.INTERNAL_ERROR);
            }
        });

        if (PORT != null) {
            int port = Integer.parseInt(PORT);
            Spark.port(port);
        }

        Spark.get("/healthz", (req, res) -> {
            res.type("text/plain");
            return "OK";
        });

        Spark.path("/api", () -> {
            Spark.before("/*", (request, response) -> {
                // Note: this may or may not be necessary in your particular application
                response.type("application/json");
                Middlewares.InjectAuthCtx(request, response);
            });

            // AUTHENTICATION ROUTES
            Spark.path("/auth", () -> {
                // Separated patient and organization auth flows
                Spark.post("/patient/register", AuthController::RegisterPatient);
                Spark.post("/patient/login", AuthController::LoginPatient);

                Spark.post("/organization/register", AuthController::RegisterOrganization);
                Spark.post("/organization/login", AuthController::LoginOrganization);

                Spark.post("/logout", AuthController::Logout);
                Spark.post("/refresh", AuthController::Refresh);
                Spark.get("/whoami", AuthController::Whoami);
            });

            // BRANCH ROUTES
            Spark.path("/branches", () -> {
                // Changed from "/branch/create" to RESTful POST on the collection
                Spark.post("", BranchController::CreateBranch);
                Spark.get("/closest", BranchController::FindClosest);
            });

            // DOCTOR ROUTES
            Spark.path("/doctors", () -> {
                Spark.post("", DoctorController::CreateDoctor);
            });

            // APPOINTMENT & QUEUE ROUTES
            Spark.path("/appointments", () -> {
                Spark.post("", AppointmentController::CreateAppointment);
                // PATCH is used here for partial updates to the appointment entity
                Spark.patch("/:id/status", AppointmentController::UpdateStatus);
                Spark.patch("/:id/reassign", AppointmentController::ReassignDoctor);
            });

            // CLINIC VISIT ROUTES (History)
            Spark.path("/visits", () -> {
                Spark.post("", VisitController::RecordVisit);
            });
        });

        Spark.init();
    }
}