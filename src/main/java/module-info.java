module com.final_app {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires de.saxsys.mvvmfx;
    requires java.desktop;
    requires okhttp3;
    requires com.google.gson;
    requires javafx.media;
    requires vosk;
    requires java.prefs;
    requires org.apache.commons.codec;
    requires org.checkerframework.checker.qual;
    requires com.google.auth.oauth2;
    requires google.cloud.firestore;
    requires com.google.common;
    requires com.google.api.apicommon;
    requires firebase.admin;
    requires google.cloud.core;
    requires com.google.auth;
    requires annotations;
    requires kotlin.stdlib;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires org.slf4j;
    requires java.dotenv;
    requires com.h2database;
    requires google.cloud.translate;
    requires jave.core;
    requires org.mockito;
    requires org.junit.jupiter.api;

    opens com.final_app to de.saxsys.mvvmfx,javafx.fxml;
    exports com.final_app;
    exports com.final_app.views;
    opens com.final_app.views to de.saxsys.mvvmfx, javafx.fxml;
    exports com.final_app.views.components.custom.table;
    opens com.final_app.views.components.custom.table to javafx.fxml;
    exports com.final_app.viewmodels;
    opens com.final_app.viewmodels to de.saxsys.mvvmfx, javafx.fxml;
    exports com.final_app.views.components;
    opens com.final_app.views.components to de.saxsys.mvvmfx, javafx.fxml;
    exports com.final_app.views.pages;
    opens com.final_app.views.pages to de.saxsys.mvvmfx, javafx.fxml;
    opens com.final_app.views.components.forms to javafx.fxml;
    opens com.final_app.models to google.cloud.firestore, firebase.admin, javafx.base;
    exports com.final_app.models to firebase.admin, google.cloud.firestore, javafx.base;
    exports com.final_app.views.components.custom.list;
    exports com.final_app.views.components.custom.general;
    opens com.final_app.views.components.custom.list to de.saxsys.mvvmfx, javafx.fxml;
    exports com.final_app.tests;
    requires org.mockito.junit.jupiter;
    requires batik.transcoder;
    requires javafx.graphics;
    opens com.final_app.tests to org.junit.platform.commons;
    //because module com.final_app does not open com.final_app.models to javafx.base
}