package edu.drexel.trainsim;

import com.google.gson.GsonBuilder;
import com.google.inject.Guice;
import com.zaxxer.hikari.HikariConfig;

import edu.drexel.trainsim.db.DatabaseModule;
import edu.drexel.trainsim.web.UserLoginController;
import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJson;

public class App {
    public static void main(String[] args) throws Exception {

        // Database
        var hikari = new HikariConfig();
        hikari.setJdbcUrl(getEnv("DB_URL"));
        hikari.setUsername(getEnv("DB_USER"));
        hikari.setPassword(getEnv("DB_PASSWORD"));

        // Dependency injection
        var injector = Guice.createInjector(
            new DatabaseModule(hikari)
        );

        // Web server
        var gson = new GsonBuilder().create();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);
        var app = Javalin.create(config -> {
            config.enableDevLogging();
            config.enableCorsForAllOrigins();
        });

        injector.getInstance(UserLoginController.class).bindRoutes(app);

        // Start the web server
        app.start(80);
    }

    private static String getEnv(String name) {
        var value = System.getenv(name);

        if (value == null) {
            final String message = "Environment variable `%s` is required.";
            throw new RuntimeException(String.format(message, name));
        }

        return value;
    }
}
