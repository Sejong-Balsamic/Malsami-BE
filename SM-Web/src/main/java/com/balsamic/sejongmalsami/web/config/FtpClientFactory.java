package com.balsamic.sejongmalsami.web.config;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.properties.FtpProperties;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

@RequiredArgsConstructor
@Slf4j
public class FtpClientFactory extends BasePooledObjectFactory<FTPClient>{
  private final FtpProperties ftpConfig;

  @Override
  public FTPClient create() throws Exception {
    FTPClient ftpClient = new FTPClient();
    ftpClient.setControlEncoding("UTF-8");
    ftpClient.setAutodetectUTF8(true);

    // 타임아웃 설정 (밀리초 단위)
    ftpClient.setConnectTimeout(10000); // 연결 타임아웃 10초
    ftpClient.setDataTimeout(30000);    // 데이터 타임아웃 30초

    try {
      ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());

      // 연결 후 소켓 타임아웃 설정
      ftpClient.setSoTimeout(30000);      // 소켓 타임아웃 30초

      int replyCode = ftpClient.getReplyCode();
      if (!FTPReply.isPositiveCompletion(replyCode)) {
        throw new CustomException(ErrorCode.FTP_CONNECTION_ERROR);
      }

      // FTP 로그인 시도
      if (!ftpClient.login(ftpConfig.getUser(), ftpConfig.getPass())) {
        throw new CustomException(ErrorCode.FTP_LOGIN_ERROR);
      }

      // 패시브 모드 설정
      ftpClient.enterLocalPassiveMode();
      ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
      // UTF-8 설정
      ftpClient.sendCommand("OPTS UTF8", "ON");

      return ftpClient;
    } catch (IOException e) {
      throw new CustomException(ErrorCode.FTP_CONNECTION_ERROR);
    }
  }

  @Override
  public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
    return new DefaultPooledObject<>(ftpClient);
  }

  @Override
  public boolean validateObject(PooledObject<FTPClient> p) {
    FTPClient ftpClient = p.getObject();
    if (ftpClient.isConnected()) {
      try {
        boolean noopResult = ftpClient.sendNoOp();
        log.debug("FTPClient 연결 상태 확인: {}, NOOP 결과: {}", ftpClient, noopResult);
        return noopResult;
      } catch (IOException e) {
        log.warn("FTPClient 연결 상태 확인 중 예외 발생: {}", e.getMessage(), e);
        return false;
      }
    } else {
      log.warn("FTPClient 연결 상태 확인 실패: FTPClient가 연결되어 있지 않습니다. {}", ftpClient);
      return false;
    }
  }


  @Override
  public void destroyObject(PooledObject<FTPClient> p) throws Exception {
    FTPClient ftpClient = p.getObject();
    if (ftpClient.isConnected()) {
      try {
        ftpClient.logout();
        ftpClient.disconnect();
      } catch (IOException e) {
        // 로그만 남기고 무시
      }
    }
  }
}
