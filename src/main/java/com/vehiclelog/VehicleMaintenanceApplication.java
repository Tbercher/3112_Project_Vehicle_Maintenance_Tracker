package com.vehiclelog;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class VehicleMaintenanceApplication extends Application {
    private ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        springContext = SpringApplication.run(VehicleMaintenanceApplication.class);
    }

    @Override
    public void start(Stage stage) {
        springContext.getBean(DashboardView.class).show(stage);
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }
}
