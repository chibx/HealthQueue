package com.healthqueue;

import com.healthqueue.controllers.Auth;

import spark.Spark;

public class App {

    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {

        Spark.path("/api", () -> {
            Spark.before("", ((request, response) -> {
            }));
            Spark.path("/auth", () -> {
                Spark.get("/login", Auth::Login);
                Spark.get("/register", Auth::Register);
                Spark.get("/logout", Auth::Logout);
                Spark.get("/refresh", Auth::Refresh);
                Spark.get("/whoami", Auth::Whoami);
            });

            Spark.path("", () -> {
            });
        });

        Spark.init();
    };
}
