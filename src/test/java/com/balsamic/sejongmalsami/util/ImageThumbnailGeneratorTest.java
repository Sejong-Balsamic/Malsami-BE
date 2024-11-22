package com.balsamic.sejongmalsami.util;

import static com.balsamic.sejongmalsami.util.LogUtils.lineLog;

import com.balsamic.sejongmalsami.object.constants.ImageQuality;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class ImageThumbnailGeneratorTest {

  @Autowired
  ImageThumbnailGenerator imageThumbnailGenerator;

  private byte[] fileContent;
  private String inputPath = "src/test/resources/document/테스트_이미지_PNG.png";
  private String outputDir = "src/test/resources/temp";

  private MockMultipartFile mockMultipartFile;

  @BeforeEach
  void setUp() throws IOException {
    // 입력 파일 경로 (하드코딩)
    File inputFile = new File(inputPath);

    // 파일이 존재하는지 확인
    if (!inputFile.exists()) {
      log.error("입력 파일이 존재하지 않습니다: {}", inputPath);
      throw new IOException("입력 파일이 존재하지 않습니다.");
    } else {
      log.info("입력 파일을 찾았습니다: {}", inputPath);
    }

    // 파일 읽기
    fileContent = Files.readAllBytes(inputFile.toPath());
    log.info("입력 파일을 성공적으로 읽었습니다: {} (크기: {} bytes)", inputPath, fileContent.length);

    // MockMultipartFile 생성
    mockMultipartFile = new MockMultipartFile(
        "file", // 필드 이름
        inputFile.getName(), // 원본 파일 이름
        MimeType.PNG.getMimeType(), // 콘텐츠 타입
        fileContent // 파일 내용
    );
  }

  @Test
  void mainTest() {
    lineLog("TEST 시작");
    try {
      lineLog(null);
//      generateImageCompress_원본파일생성();
//      generateImageCompress_고용량압축파일생성();
//      generateImageCompress_중간용량압축파일생성();
//      generateImageCompress_저용량압축파일생성();

//      generateImageCompress_TestWithJPGInput();
//      generateImageCompress_TestWithWebPInput();
//      generateImageCompress_TestWithPNGMacEnvironment();
//      generateImageCompress_TestWithWindowsOrLinuxEnvironment();

      lineLog("TEST 종료");
    } catch (Exception e) {
      log.error("테스트 중 예외 발생:", e);
      throw new RuntimeException(e);
    }
  }

  void generateImageCompress_원본파일생성() throws IOException {
    // 이미지 압축 생성
    log.info("이미지 압축 생성 시작: {}, 품질: {}", mockMultipartFile.getOriginalFilename(), ImageQuality.ORIGINAL.name());
    byte[] compressedImageBytes = imageThumbnailGenerator.generateImageCompress(mockMultipartFile, ImageQuality.ORIGINAL);
    log.info("이미지 압축 생성 완료: 압축된 이미지 크기 = {} bytes", compressedImageBytes.length);

    // 출력 파일명 설정
    String outputFileName = mockMultipartFile.getOriginalFilename()
        .replace(".png", "_compressed_original." + imageThumbnailGenerator.getOutputThumbnailFormat());
    Path outputPath = Paths.get(outputDir, outputFileName);

    // 압축된 이미지 바이트를 파일로 저장
    Files.write(outputPath, compressedImageBytes);
    log.info("압축된 이미지가 저장되었습니다: {}", outputPath.toAbsolutePath());
  }

  void generateImageCompress_고용량압축파일생성() throws IOException {
    // 이미지 압축 생성
    log.info("이미지 압축 생성 시작: {}, 품질: {}", mockMultipartFile.getOriginalFilename(), ImageQuality.ORIGINAL.name());
    byte[] compressedImageBytes = imageThumbnailGenerator.generateImageCompress(mockMultipartFile, ImageQuality.HIGH);
    log.info("이미지 압축 생성 완료: 압축된 이미지 크기 = {} bytes", compressedImageBytes.length);

    // 출력 파일명 설정
    String outputFileName = mockMultipartFile.getOriginalFilename()
        .replace(".png", "_compressed_high." + imageThumbnailGenerator.getOutputThumbnailFormat());
    Path outputPath = Paths.get(outputDir, outputFileName);

    // 압축된 이미지 바이트를 파일로 저장
    Files.write(outputPath, compressedImageBytes);
    log.info("압축된 이미지가 저장되었습니다: {}", outputPath.toAbsolutePath());
  }

  void generateImageCompress_중간용량압축파일생성() throws IOException {
    // 이미지 압축 생성
    log.info("이미지 압축 생성 시작: {}, 품질: {}", mockMultipartFile.getOriginalFilename(), ImageQuality.ORIGINAL.name());
    byte[] compressedImageBytes = imageThumbnailGenerator.generateImageCompress(mockMultipartFile, ImageQuality.MEDIUM);
    log.info("이미지 압축 생성 완료: 압축된 이미지 크기 = {} bytes", compressedImageBytes.length);

    // 출력 파일명 설정
    String outputFileName = mockMultipartFile.getOriginalFilename()
        .replace(".png", "_compressed_middle." + imageThumbnailGenerator.getOutputThumbnailFormat());
    Path outputPath = Paths.get(outputDir, outputFileName);

    // 압축된 이미지 바이트를 파일로 저장
    Files.write(outputPath, compressedImageBytes);
    log.info("압축된 이미지가 저장되었습니다: {}", outputPath.toAbsolutePath());
  }

  void generateImageCompress_저용량압축파일생성() throws IOException {
    // 이미지 압축 생성
    log.info("이미지 압축 생성 시작: {}, 품질: {}", mockMultipartFile.getOriginalFilename(), ImageQuality.ORIGINAL.name());
    byte[] compressedImageBytes = imageThumbnailGenerator.generateImageCompress(mockMultipartFile, ImageQuality.LOW);
    log.info("이미지 압축 생성 완료: 압축된 이미지 크기 = {} bytes", compressedImageBytes.length);

    // 출력 파일명 설정
    String outputFileName = mockMultipartFile.getOriginalFilename()
        .replace(".png", "_compressed_low." + imageThumbnailGenerator.getOutputThumbnailFormat());
    Path outputPath = Paths.get(outputDir, outputFileName);

    // 압축된 이미지 바이트를 파일로 저장
    Files.write(outputPath, compressedImageBytes);
    log.info("압축된 이미지가 저장되었습니다: {}", outputPath.toAbsolutePath());
  }

  void generateImageCompress_TestWithJPGInput() throws IOException {
    String jpgInputPath = "src/test/resources/document/테스트_이미지_JPG.jpg";
    MockMultipartFile jpgMockFile = createMockMultipartFile(jpgInputPath, MimeType.JPEG.getMimeType());
    log.info("JPG 파일 테스트 시작");

    byte[] compressedImageBytes = imageThumbnailGenerator.generateImageCompress(jpgMockFile, ImageQuality.MEDIUM);
    log.info("JPG 파일 압축 완료: 압축된 이미지 크기 = {} bytes", compressedImageBytes.length);

    String outputFileName = jpgMockFile.getOriginalFilename()
        .replace(".jpg", "_compressed." + imageThumbnailGenerator.getOutputThumbnailFormat());
    Path outputPath = Paths.get(outputDir, outputFileName);

    Files.write(outputPath, compressedImageBytes);
    log.info("압축된 JPG 이미지가 저장되었습니다: {}", outputPath.toAbsolutePath());
  }

  void generateImageCompress_TestWithWebPInput() throws IOException {
    String webpInputPath = "src/test/resources/document/테스트_이미지_WEBP.webp";
    MockMultipartFile webpMockFile = createMockMultipartFile(webpInputPath, MimeType.WEBP.getMimeType());
    log.info("WebP 파일 테스트 시작");

    byte[] compressedImageBytes = imageThumbnailGenerator.generateImageCompress(webpMockFile, ImageQuality.MEDIUM);
    log.info("WebP 파일은 압축 없이 반환: 반환된 이미지 크기 = {} bytes", compressedImageBytes.length);

    String outputFileName = webpMockFile.getOriginalFilename();
    Path outputPath = Paths.get(outputDir, outputFileName);

    Files.write(outputPath, compressedImageBytes);
    log.info("WebP 이미지가 저장되었습니다 (변경 없음): {}", outputPath.toAbsolutePath());
  }

  void generateImageCompress_TestWithPNGMacEnvironment() throws IOException {
    String pngInputPath = "src/test/resources/document/테스트_이미지_PNG.png";
    MockMultipartFile pngMockFile = createMockMultipartFile(pngInputPath, MimeType.PNG.getMimeType());
    log.info("PNG 파일 테스트 (Mac 환경) 시작");

    // Mac OS에서 JPG로 변환
    if (!imageThumbnailGenerator.isWebPSupported()) {
      byte[] compressedImageBytes = imageThumbnailGenerator.generateImageCompress(pngMockFile, ImageQuality.HIGH);
      log.info("PNG 파일 압축 완료 (JPG로 변환): 압축된 이미지 크기 = {} bytes", compressedImageBytes.length);

      String outputFileName = pngMockFile.getOriginalFilename()
          .replace(".png", "_compressed_mac." + imageThumbnailGenerator.getOutputThumbnailFormat());
      Path outputPath = Paths.get(outputDir, outputFileName);

      Files.write(outputPath, compressedImageBytes);
      log.info("Mac 환경에서 변환된 PNG 이미지가 저장되었습니다: {}", outputPath.toAbsolutePath());
    } else {
      log.warn("Mac 환경에서 WebP 변환이 지원되지 않음");
    }
  }

  void generateImageCompress_TestWithWindowsOrLinuxEnvironment() throws IOException {
    String pngInputPath = "src/test/resources/document/테스트_이미지_PNG.png";
    MockMultipartFile pngMockFile = createMockMultipartFile(pngInputPath, MimeType.PNG.getMimeType());
    log.info("PNG 파일 테스트 (Windows/Linux 환경) 시작");

    // Windows/Linux에서 WebP로 변환
    if (imageThumbnailGenerator.isWebPSupported()) {
      byte[] compressedImageBytes = imageThumbnailGenerator.generateImageCompress(pngMockFile, ImageQuality.HIGH);
      log.info("PNG 파일 압축 완료 (WebP로 변환): 압축된 이미지 크기 = {} bytes", compressedImageBytes.length);

      String outputFileName = pngMockFile.getOriginalFilename()
          .replace(".png", "_compressed_webp." + imageThumbnailGenerator.getOutputThumbnailFormat());
      Path outputPath = Paths.get(outputDir, outputFileName);

      Files.write(outputPath, compressedImageBytes);
      log.info("Windows/Linux 환경에서 변환된 PNG 이미지가 저장되었습니다: {}", outputPath.toAbsolutePath());
    } else {
      log.warn("Windows/Linux 환경에서 WebP 변환이 지원되지 않음");
    }
  }

  private MockMultipartFile createMockMultipartFile(String filePath, String mimeType) throws IOException {
    File inputFile = new File(filePath);

    if (!inputFile.exists()) {
      log.error("테스트 입력 파일이 존재하지 않습니다: {}", filePath);
      throw new IOException("테스트 입력 파일이 존재하지 않습니다.");
    }
    byte[] fileContent = Files.readAllBytes(inputFile.toPath());
    log.info("테스트 입력 파일 읽기 완료: {} (크기: {} bytes)", filePath, fileContent.length);

    return new MockMultipartFile(
        "file",
        inputFile.getName(),
        mimeType,
        fileContent
    );
  }

}
