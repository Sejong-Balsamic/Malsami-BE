package com.balsamic.sejongmalsami.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@Slf4j
public class FtpService {

  @Value("${ftp.server}")
  private String ftpServer;

  @Value("${ftp.port}")
  private int ftpPort;

  @Value("${ftp.user}")
  private String ftpUser;

  @Value("${ftp.pass}")
  private String ftpPass;

  /**
   * FTP를 통해 파일 업로드
   *
   * @param remotePath 원격 서버의 업로드 경로
   * @param localFile  로컬 파일 객체
   * @throws IOException 파일 업로드 중 예외 발생 시
   */
  public void uploadFile(String remotePath, File localFile) throws IOException {
    FTPClient ftpClient = new FTPClient();
    ftpClient.setControlEncoding("UTF-8");
    ftpClient.setAutodetectUTF8(true); // UTF-8 자동 감지 활성화

    try (FileInputStream fis = new FileInputStream(localFile)) {
      ftpClient.connect(ftpServer, ftpPort);

      int replyCode = ftpClient.getReplyCode();
      if (!FTPReply.isPositiveCompletion(replyCode)) {
        throw new IOException("FTP 서버에 연결할 수 없습니다. 응답 코드: " + replyCode);
      }

      boolean login = ftpClient.login(ftpUser, ftpPass);
      if (!login) {
        throw new IOException("FTP 로그인 실패");
      }

      // 패시브 모드 설정
      ftpClient.enterLocalPassiveMode();
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

      // UTF-8 지원 활성화
      ftpClient.sendCommand("OPTS UTF8", "ON");

      // 파일 업로드 경로 설정
      String remoteFile = remotePath + "/" + localFile.getName();
      log.info("FTP 파일 업로드 시작: {} -> {}", localFile.getAbsolutePath(), remoteFile);

      boolean success = ftpClient.storeFile(remoteFile, fis);
      if (success) {
        log.info("FTP 파일 업로드 성공: {}", remoteFile);
      } else {
        throw new IOException("FTP 파일 업로드 실패: " + remoteFile);
      }
    } catch (IOException e) {
      log.error("FTP 파일 업로드 중 오류 발생: {}", e.getMessage());
      throw e;
    } finally {
      if (ftpClient.isConnected()) {
        try {
          ftpClient.logout();
          ftpClient.disconnect();
        } catch (IOException ex) {
          log.error("FTP 연결 종료 중 오류 발생: {}", ex.getMessage());
        }
      }
    }
  }

  /**
   * 원격 서버에서 파일 삭제
   *
   * @param remoteFilePath 삭제할 파일의 원격 경로
   * @throws IOException 파일 삭제 중 예외 발생 시
   */
  public void deleteFile(String remoteFilePath) throws IOException {
    FTPClient ftpClient = new FTPClient();
    try {
      ftpClient.connect(ftpServer, ftpPort);
      ftpClient.login(ftpUser, ftpPass);
      ftpClient.enterLocalPassiveMode();

      boolean deleted = ftpClient.deleteFile(remoteFilePath);
      if (deleted) {
        log.info("FTP 파일 삭제 성공: {}", remoteFilePath);
      } else {
        log.warn("FTP 파일 삭제 실패: {}", remoteFilePath);
      }
    } catch (IOException e) {
      log.error("FTP 파일 삭제 중 오류 발생: {}", e.getMessage());
      throw e;
    } finally {
      ftpClient.logout();
      ftpClient.disconnect();
    }
  }

}
