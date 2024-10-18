package com.balsamic.sejongmalsami.util.config;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.BasePooledObjectFactory;
import java.io.IOException;

@RequiredArgsConstructor
public class FtpClientFactory extends BasePooledObjectFactory<FTPClient>{
  private final FtpConfig ftpConfig;

  @Override
  public FTPClient create() throws Exception {
    FTPClient ftpClient = new FTPClient();
    ftpClient.setControlEncoding("UTF-8");
    ftpClient.setAutodetectUTF8(true);

    try {
      ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
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
    return ftpClient.isConnected() && ftpClient.isAvailable();
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
