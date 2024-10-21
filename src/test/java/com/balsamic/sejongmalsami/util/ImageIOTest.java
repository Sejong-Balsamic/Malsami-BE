package com.balsamic.sejongmalsami.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

@Slf4j
@SpringBootTest
public class ImageIOTest {

  @Test
  public void mainTest() {
    webPSupportTest();
//    webPThumbnailCreationTest(); // 라이브러리가 MAC 환경에서는 WEBP 압축이 불가능함
//    webPConversionTest();
  }


  public void webPSupportTest() {
    String[] formats = ImageIO.getWriterFormatNames();
    log.info("Available ImageIO writer formats:");
    for (String format : formats) {
      log.info(format);
    }
    boolean isWebPSupported = java.util.Arrays.asList(formats).contains("webp");
    log.info("WebP 지원 여부: {}", isWebPSupported ? "지원됨" : "지원되지 않음");
  }

  public void webPThumbnailCreationTest() {
    File outputFile = new File("test_thumbnail.webp");
    try {
      // 테스트용 이미지 생성
      BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
      java.awt.Graphics2D g2d = img.createGraphics();
      g2d.fillRect(0, 0, 100, 100);
      g2d.dispose();

      // 썸네일 생성
      Thumbnails.of(img)
          .size(50, 50)
          .outputFormat("webp")
          .outputQuality(0.8)
          .toFile(outputFile);

      log.info("WebP 썸네일 생성 성공");
      log.info("파일 존재 여부: {}", outputFile.exists() ? "파일이 존재합니다." : "파일이 존재하지 않습니다.");
      log.info("파일 크기: {} bytes", outputFile.length());

      // WebP 파일 읽기 테스트
      BufferedImage readImage = ImageIO.read(outputFile);
      log.info("WebP 이미지 읽기 성공 여부: {}", readImage != null ? "성공" : "실패");
      if (readImage != null) {
        log.info("이미지 가로 크기: {}", readImage.getWidth());
        log.info("이미지 세로 크기: {}", readImage.getHeight());
      }

    } catch (IOException e) {
      log.error("WebP 썸네일 생성 실패: {}", e.getMessage(), e);
    } finally {
      // 테스트 후 파일 삭제
      if (outputFile.exists()) {
        outputFile.delete();
        log.info("테스트 후 파일 삭제 완료");
      }
    }
  }

  public void webPConversionTest() {
    File inputFile = new File("test_input.png");
    File outputFile = new File("test_output.webp");
    try {
      // PNG 이미지 생성
      BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
      java.awt.Graphics2D g2d = img.createGraphics();
      g2d.fillRect(0, 0, 100, 100);
      g2d.dispose();
      ImageIO.write(img, "png", inputFile);
      log.info("PNG 테스트 이미지 생성 완료");

      // PNG에서 WebP로 변환
      Thumbnails.of(inputFile)
          .size(100, 100)
          .outputFormat("webp")
          .toFile(outputFile);

      log.info("PNG에서 WebP로 변환 성공");
      log.info("WebP 파일 존재 여부: {}", outputFile.exists() ? "파일이 존재합니다." : "파일이 존재하지 않습니다.");
      log.info("WebP 파일 크기: {} bytes", outputFile.length());

      // WebP 파일 읽기 테스트
      BufferedImage readImage = ImageIO.read(outputFile);
      log.info("WebP 이미지 읽기 성공 여부: {}", readImage != null ? "성공" : "실패");
      if (readImage != null) {
        log.info("이미지 가로 크기: {}", readImage.getWidth());
        log.info("이미지 세로 크기: {}", readImage.getHeight());
      }

    } catch (IOException e) {
      log.error("PNG에서 WebP로 변환 실패: {}", e.getMessage(), e);
    } finally {
      // 테스트 후 파일 삭제
      if (inputFile.exists()) {
        inputFile.delete();
        log.info("입력 파일 삭제 완료");
      }
      if (outputFile.exists()) {
        outputFile.delete();
        log.info("출력 파일 삭제 완료");
      }
    }
  }
}
