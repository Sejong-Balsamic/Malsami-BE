package com.balsamic.sejongmalsami.web.config;

import com.amazonaws.services.s3.AmazonS3Client;
import com.balsamic.sejongmalsami.util.ImageThumbnailGenerator;
import com.balsamic.sejongmalsami.util.properties.FtpProperties;
import com.balsamic.sejongmalsami.util.storage.FtpStorageService;
import com.balsamic.sejongmalsami.util.storage.StorageService;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {
  @Value("${spring.profiles.active:dev}")
  private String activeProfile;

  @Bean
  public StorageService storageService(
      FtpProperties ftpConfig,
      @Autowired(required = false) GenericObjectPool<FTPClient> ftpClientPool,
      @Autowired(required = false) ImageThumbnailGenerator imageThumbnailGenerator,
      @Autowired(required = false) AmazonS3Client amazonS3Client
  ) {
    switch (activeProfile) {
      case "prod" -> {
        //FIXME: 아직 DirectStorageService 에 대한 로직 미완성
        return new FtpStorageService(ftpClientPool, ftpConfig, imageThumbnailGenerator);
//        return new DirectStorageService(ftpConfig);
      }
      case "dev" -> {
        return new FtpStorageService(ftpClientPool, ftpConfig, imageThumbnailGenerator);
//        return new S3StorageService(amazonS3Client);
      }
      default -> {
        return new FtpStorageService(ftpClientPool, ftpConfig, imageThumbnailGenerator);
      }
    }
  }
}
