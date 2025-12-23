package com.sincronizador.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;

public final class DriveConfig {

    private static final String APPLICATION_NAME = "Sincronizador Catalogo";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // salva o token aqui (pra não logar toda vez)
    private static final java.io.File TOKENS_DIR = Paths.get("tokens").toFile();

    private DriveConfig() {}

    public static Drive criarDrive() {
        try {
            var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY,
                    new InputStreamReader(new FileInputStream("credentials.json"))
            );

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    clientSecrets,
                    List.of(DriveScopes.DRIVE) // precisa escrever (upload/update)
            )
                    .setDataStoreFactory(new FileDataStoreFactory(TOKENS_DIR))
                    .setAccessType("offline")
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8888)
                    .build();

            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                    .authorize("user");

            return new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } catch (GeneralSecurityException | java.io.IOException e) {
            throw new RuntimeException("Erro ao autenticar no Google Drive via OAuth", e);
        }
    }
}
