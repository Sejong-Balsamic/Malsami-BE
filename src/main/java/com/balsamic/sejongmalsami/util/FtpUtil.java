package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
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
public class FtpUtil { // 이름 변경: FtpService -> FtpUtil

  @Value("${ftp.server}")
  private String ftpServer;

  @Value("${ftp.port}")
  private int ftpPort;

  @Value("${ftp.user}")
  private String ftpUser;

  @Value("${ftp.pass}")
  private String ftpPass;

  @Value("${ftp.path.document}")
  private String ftpDocumentPath;

  /**
   * FTP를 통해 파일 업로드
   *
   * @param localFile 로컬 파일 객체
   */
  public void uploadFile(File localFile) {
    FTPClient ftpClient = new FTPClient();
    ftpClient.setControlEncoding("UTF-8");
    ftpClient.setAutodetectUTF8(true); // UTF-8 자동 감지 활성화

    try (FileInputStream fis = new FileInputStream(localFile)) {
      ftpClient.connect(ftpServer, ftpPort);

      int replyCode = ftpClient.getReplyCode();
      if (!FTPReply.isPositiveCompletion(replyCode)) {
        log.error("FTP 서버에 연결할 수 없습니다. 응답 코드: {}", replyCode);
        throw new CustomException(ErrorCode.FTP_CONNECTION_ERROR);
      }

      boolean login = ftpClient.login(ftpUser, ftpPass);
      if (!login) {
        log.error("FTP 로그인 실패: 사용자명={}, 비밀번호=***", ftpUser);
        throw new CustomException(ErrorCode.FTP_LOGIN_ERROR);
      }

      // 패시브 모드 설정
      ftpClient.enterLocalPassiveMode();
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
      // UTF-8
      ftpClient.sendCommand("OPTS UTF8", "ON");

      // 파일 업로드 경로 설정
      String remoteFile = ftpDocumentPath + "/" + localFile.getName();
      log.info("FTP 파일 업로드 시작: {} -> {}", localFile.getAbsolutePath(), remoteFile);

      boolean success = ftpClient.storeFile(remoteFile, fis);
      if (success) {
        log.info("FTP 파일 업로드 성공: {}", remoteFile);
      } else {
        log.error("FTP 파일 업로드 실패: {}", remoteFile);
        throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
      }
    } catch (IOException e) {
      log.error("FTP 파일 업로드 중 IOException 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
    } finally {
      if (ftpClient.isConnected()) {
        try {
          ftpClient.logout();
          ftpClient.disconnect();
          log.info("FTP 연결 종료");
        } catch (IOException ex) {
          log.error("FTP 연결 종료 중 오류 발생: {}", ex.getMessage());
        }
      }
    }
  }

  /**
   * 원격 서버에서 파일 삭제
   *
   * @param filename 삭제할 파일의 이름
   */
  public void deleteFile(String filename) {
    FTPClient ftpClient = new FTPClient();
    try {
      ftpClient.connect(ftpServer, ftpPort);

      int replyCode = ftpClient.getReplyCode();
      if (!FTPReply.isPositiveCompletion(replyCode)) {
        log.error("FTP 서버에 연결할 수 없습니다. 응답 코드: {}", replyCode);
        throw new CustomException(ErrorCode.FTP_CONNECTION_ERROR);
      }

      boolean login = ftpClient.login(ftpUser, ftpPass);
      if (!login) {
        log.error("FTP 로그인 실패: 사용자명={}, 비밀번호=***", ftpUser);
        throw new CustomException(ErrorCode.FTP_LOGIN_ERROR);
      }

      ftpClient.enterLocalPassiveMode();

      // 파일 이름을 documentPath와 결합하여 전체 경로 생성
      String remoteFilePath = ftpDocumentPath + "/" + filename;
      log.info("FTP 파일 삭제 시작: {}", remoteFilePath);

      boolean deleted = ftpClient.deleteFile(remoteFilePath);
      if (deleted) {
        log.info("FTP 파일 삭제 성공: {}", remoteFilePath);
      } else {
        log.warn("FTP 파일 삭제 실패: {}", remoteFilePath);
        throw new CustomException(ErrorCode.FTP_FILE_DELETE_ERROR);
      }
    } catch (IOException e) {
      log.error("FTP 파일 삭제 중 IOException 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.FTP_FILE_DELETE_ERROR);
    } finally {
      if (ftpClient.isConnected()) {
        try {
          ftpClient.logout();
          ftpClient.disconnect();
          log.info("FTP 연결 종료");
        } catch (IOException ex) {
          log.error("FTP 연결 종료 중 오류 발생: {}", ex.getMessage());
        }
      }
    }
  }
}
