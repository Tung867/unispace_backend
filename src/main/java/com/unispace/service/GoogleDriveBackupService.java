package com.unispace.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.unispace.domain.reservation.Reservation;
import com.unispace.domain.reservation.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 예약 내역을 텍스트(.txt)로 변환해 Google Drive 에 백업.
 * (Part C - DB 사용 불가 상황 대비)
 * google.drive.enabled=false 이면 안전하게 비활성화(no-op)된다.
 */
@Service
public class GoogleDriveBackupService {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveBackupService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ReservationRepository reservationRepository;

    private final boolean enabled;
    private final String credentialsPath;
    private final String folderId;
    private final String applicationName;

    public GoogleDriveBackupService(ReservationRepository reservationRepository,
                                    @Value("${google.drive.enabled:false}") boolean enabled,
                                    @Value("${google.drive.credentials-path:}") String credentialsPath,
                                    @Value("${google.drive.folder-id:}") String folderId,
                                    @Value("${google.drive.application-name:UniSpace-Backup}") String applicationName) {
        this.reservationRepository = reservationRepository;
        this.enabled = enabled;
        this.credentialsPath = credentialsPath;
        this.folderId = folderId;
        this.applicationName = applicationName;
    }

    /** 현재 모든 예약 내역을 .txt 로 만들어 Drive 에 업로드. 결과 메시지 반환. */
    public String backupReservations() {
        String content = buildLogText(reservationRepository.findAll());
        String fileName = "unispace-reservations-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".txt";

        if (!enabled) {
            log.warn("[Drive] 비활성화 상태 - 업로드 생략. (google.drive.enabled=false)");
            return "Google Drive 백업이 비활성화되어 있습니다. 로컬 미리보기만 생성됨: " + fileName;
        }

        try {
            Drive drive = buildDriveService();
            com.google.api.services.drive.model.File metadata =
                    new com.google.api.services.drive.model.File();
            metadata.setName(fileName);
            if (folderId != null && !folderId.isBlank()) {
                metadata.setParents(Collections.singletonList(folderId));
            }

            InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            InputStreamContent mediaContent = new InputStreamContent("text/plain", stream);

            com.google.api.services.drive.model.File uploaded =
                    drive.files().create(metadata, mediaContent).setFields("id, name").execute();

            log.info("[Drive] 백업 업로드 완료: {} (id={})", uploaded.getName(), uploaded.getId());
            return "Google Drive 업로드 완료: " + uploaded.getName() + " (id=" + uploaded.getId() + ")";
        } catch (Exception e) {
            log.error("[Drive] 백업 실패", e);
            return "Google Drive 백업 실패: " + e.getMessage();
        }
    }

    private Drive buildDriveService() throws Exception {
        try (FileInputStream credStream = new FileInputStream(credentialsPath)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credStream)
                    .createScoped(Collections.singletonList(DriveScopes.DRIVE_FILE));
            return new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(applicationName)
                    .build();
        }
    }

    private String buildLogText(List<Reservation> reservations) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== UniSpace 예약 내역 백업 =====\n");
        sb.append("생성 시각: ").append(LocalDateTime.now().format(FMT)).append("\n");
        sb.append("총 ").append(reservations.size()).append("건\n");
        sb.append("----------------------------------\n");
        for (Reservation r : reservations) {
            sb.append("[#").append(r.getId()).append("] ")
              .append("공간=").append(r.getRoom().getName())
              .append(" | 사용자=").append(r.getUser().getName())
              .append(" | ").append(r.getStartTime().format(FMT))
              .append(" ~ ").append(r.getEndTime().format(FMT))
              .append(" | 상태=").append(r.getStatus())
              .append("\n");
        }
        return sb.toString();
    }
}
