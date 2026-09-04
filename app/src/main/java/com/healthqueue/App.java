package com.healthqueue;

import com.healthqueue.controllers.AuthController;
// Assuming you will create these controller classes:
import com.healthqueue.controllers.BranchController;
import com.healthqueue.controllers.DoctorController;

import java.nio.file.Paths;

import com.healthqueue.controllers.AppointmentController;
import com.healthqueue.controllers.ProfileController;
import com.healthqueue.controllers.VisitController;
import com.healthqueue.middlewares.Middlewares;
import com.healthqueue.utils.ExceptionHandler;
import com.healthqueue.utils.Utils;

import spark.Spark;

public class App {
    public static void main(String[] args) {
        final String PORT = Utils.getEnv("PORT", "3000");

        Spark.port(Integer.parseInt(PORT));

        Spark.staticFiles.externalLocation(Paths.get("").toAbsolutePath().resolve("./web/dist").normalize().toString());

        // Enable CORS so the React frontend (port 5173 in dev) can reach this API
        Middlewares.enableCORS("http://localhost:5173", "GET,POST,PUT,PATCH,DELETE,OPTIONS",
                "Content-Type,Authorization,Accept");

        Spark.exception(Exception.class, ExceptionHandler::handle);

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
                Spark.post("/patient/refresh", AuthController::RefreshPatient);
                Spark.post("/patient/logout", AuthController::LogoutPatient);
                Spark.post("/doctor/login", AuthController::LoginDoctor);
                Spark.post("/doctor/refresh", AuthController::RefreshDoctor);
                Spark.post("/doctor/logout", AuthController::LogoutDoctor);

                Spark.post("/organization/register", AuthController::RegisterOrganization);
                Spark.post("/organization/login", AuthController::LoginOrganization);
                Spark.post("/organization/refresh", AuthController::RefreshOrganization);
                Spark.post("/organization/logout", AuthController::LogoutOrganization);

                Spark.get("/whoami", AuthController::Whoami);
            });

            // PROFILE ROUTES
            Spark.path("/profile", () -> {
                Spark.get("/patient", ProfileController::GetPatientProfile);
                Spark.get("/organization", ProfileController::GetOrganizationProfile);
                Spark.get("/doctor", ProfileController::GetDoctorProfile);
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
                Spark.delete("", DoctorController::DeleteDoctor);
                Spark.post("/change-branch", DoctorController::UpdateDoctorBranch);
            });

            // APPOINTMENT & QUEUE ROUTES
            Spark.path("/appointments", () -> {
                Spark.post("", AppointmentController::CreateAppointment);
                // PATCH is used here for partial updates to the appointment entity
                Spark.patch("/:id/status", AppointmentController::UpdateStatus);
                Spark.patch("/:id/reassign", AppointmentController::ReassignDoctor);
                Spark.get("/visits/history", VisitController::GetPatientVisitHistory);
            });

            // CLINIC VISIT ROUTES (History)
            Spark.path("/visits", () -> {
                Spark.post("", VisitController::RecordVisit);
                Spark.get("/history", VisitController::GetPatientVisitHistory);
            });
        });

        Spark.notFound((req, res) -> {
            if (!req.pathInfo().startsWith("/api")) {
                res.type("text/html");
                try {
                    java.nio.file.Path indexPath = Paths.get("").toAbsolutePath().resolve("./web/dist/index.html").normalize();
                    if (java.nio.file.Files.exists(indexPath)) {
                        return java.nio.file.Files.readString(indexPath);
                    }
                } catch (Exception ignored) {}
            }
            res.type("application/json");
            return "{\"status\":404,\"message\":\"Not Found\"}";
        });

        Spark.init();
    }
}