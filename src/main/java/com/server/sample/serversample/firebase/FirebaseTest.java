package com.server.sample.serversample.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FirebaseTest {
    private Logger _logger = LoggerFactory.getLogger("FirebaseTest");

    @Autowired
    public FirebaseTest(JobScheduler jobScheduler) {
        jobScheduler.enqueue(this::Run);
    }

    @Job(name = "FirebaseTest")
    public void Run() {
        String dbUrl;
        try {
            Path path = Paths.get("secrets/dburl.txt");
            byte[] bytes = Files.readAllBytes(path);
            List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
            dbUrl = allLines.get(0);

        } catch (FileNotFoundException e) {
            _logger.error("DB URL FileNotFoundException", e);
            return;
        } catch (IOException e) {
            _logger.error("DB URL IOException", e);
            return;
        }

        GoogleCredentials credentials;
        try {
            var acc = new FileInputStream("secrets/key.json");
            credentials = GoogleCredentials.fromStream(acc);
        } catch (FileNotFoundException e) {
            _logger.error("Key FileNotFoundException", e);
            return;
        } catch (IOException e) {
            _logger.error("Key IOException", e);
            return;
        }

        _logger.info(String.format("Initializing App. dbUrl: %s", dbUrl));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setDatabaseUrl(dbUrl)
                .build();

        FirebaseApp.initializeApp(options);

        Subscribe();
    }

    private void Subscribe() {
        var ref = FirebaseDatabase.getInstance().getReference("test/list");

        _logger.info("Subscribing to test/list");

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                _logger.info(String.format("onChildAdded: %s", dataSnapshot.getValue().toString()));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                _logger.info(String.format("onChildChanged: %s", dataSnapshot.getValue().toString()));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                _logger.info(String.format("onChildRemoved: %s", dataSnapshot.getValue().toString()));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                _logger.info(String.format("onChildMoved: %s", dataSnapshot.getValue().toString()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                _logger.info(String.format("onCancelled: %s", databaseError.getMessage()));
            }
        });
    }
}
