package application;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class LoginScreen extends Application {

    private static final String DARK = "#1B3A6B";
    private static final String MID = "#2E6DA4";
    private static final String LIGHT = "#E8F2FF";
    private static final String RED = "#DC2626";

    @Override
    public void start(Stage stage) {
        stage.setTitle("Health Plus (; Login");

        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(40, 20, 30, 20));
        header.setStyle("-fx-background-color: " + DARK + ";");

        Label logo = new Label("⛨");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        logo.setTextFill(Color.WHITE);

        Label title = new Label("Health Plus");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Warehouse Management System");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#A8C4E0"));

        Label welcome = new Label("Please log in to continue");
        welcome.setFont(Font.font("Arial", 12));
        welcome.setTextFill(Color.web("#7AABCF"));

        header.getChildren().addAll(logo, title, subtitle, welcome);

        VBox form = new VBox(14);
        form.setPadding(new Insets(32, 40, 32, 40));
        form.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 4);");
        form.setMaxWidth(420);

        Label formTitle = new Label("Sign In");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        formTitle.setTextFill(Color.web(DARK));

        Label idLabel = new Label("User ID");
        idLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        idLabel.setTextFill(Color.web("#64748B"));

        TextField userIDField = new TextField();
        userIDField.setPromptText("Enter your User ID");
        userIDField.setStyle(fieldStyle());
        userIDField.setPrefHeight(44);

        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        passLabel.setTextFill(Color.web("#64748B"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle(fieldStyle());
        passwordField.setPrefHeight(44);

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.web(RED));
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        Button loginBtn = new Button("Login  ->");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(46);
        loginBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        loginBtn.setStyle(
                "-fx-background-color: " + DARK + "; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");

        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
                "-fx-background-color: " + MID + "; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"));

        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
                "-fx-background-color: " + DARK + "; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"));

        VBox hintBox = new VBox(6);
        hintBox.setPadding(new Insets(12));
        hintBox.setStyle(
                "-fx-background-color: " + LIGHT + "; " +
                        "-fx-background-radius: 8;");

        Label hintTitle = new Label("[i] Login Help");
        hintTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        hintTitle.setTextFill(Color.web(MID));

        Label hints = new Label("If you forgot your password\n" +
                                   " contact the system admin.");

        hints.setFont(Font.font("Courier New", 11));
        hints.setTextFill(Color.web("#334155"));

        hintBox.getChildren().addAll(hintTitle, hints);

        form.getChildren().addAll(formTitle, idLabel, userIDField,
                passLabel, passwordField, errorLabel, loginBtn,
                hintBox);

        //login
        Runnable doLogin = () -> {
            String idText = userIDField.getText().trim();
            String pass = passwordField.getText().trim();

            if (idText.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Please enter your User ID and password.");
                errorLabel.setVisible(true);
                return;
            }

            int userID;

            try {
                userID = Integer.parseInt(idText);
            } catch (NumberFormatException ex) {
                errorLabel.setText("User ID must be a number.");
                errorLabel.setVisible(true);
                return;
            }

            UserAccount account = UserAccountDAO.login(userID, pass);

            if (account == null) {
                errorLabel.setText("Invalid User ID or password. Please try again.");
                errorLabel.setVisible(true);
                passwordField.clear();
                return;
            }

            stage.close();

            //redierct to the right portal based onuser role
            try {
                switch (account.getRole()) {
                    case "Admin":
                        new ProductApp().start(new Stage());
                        break;
                    case "Employee":
                        new EmployeePortal(account).start(new Stage());
                        break;
                    case "Client":
                        new ClientPortal(account).start(new Stage());
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }};

        loginBtn.setOnAction(e -> doLogin.run());
        passwordField.setOnAction(e -> doLogin.run());
        userIDField.setOnAction(e -> passwordField.requestFocus());

        VBox center = new VBox(form);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(30));
        center.setStyle("-fx-background-color: " + LIGHT + ";");
        VBox.setVgrow(center, Priority.ALWAYS);

        VBox root = new VBox(0, header, center);
        VBox.setVgrow(center, Priority.ALWAYS);

        Scene scene = new Scene(root, 480, 700);
        stage.setMinWidth(480);
        stage.setMinHeight(700);
        stage.setScene(scene);

        stage.setResizable(true);

        stage.show();
    }

    private String fieldStyle() {
        return "-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #CBD5E1; -fx-padding: 8 12; " +
                "-fx-font-size: 13px; -fx-background-color: #F8FAFC;";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
