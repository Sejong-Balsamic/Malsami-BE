package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.ImageQuality;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.object.constants.SystemType;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
@Getter
@RequiredArgsConstructor
public class ImageThumbnailGenerator {

  public static final int DEFAULT_WIDTH = 600;
  public static final int DEFAULT_HEIGHT = 600;

  private final String outputThumbnailFormat; // JPG, WEBP

  public ImageThumbnailGenerator() {
    if (isWebPSupported()) {
      this.outputThumbnailFormat = "webp";
    } else {
      this.outputThumbnailFormat = "jpg";
    }
    log.info("선택된 썸네일 이미지 형식: {}", outputThumbnailFormat);
  }

  /**
   * WebP 지원 여부를 확인
   *
   * @return WebP 지원 시 true, 아니면 false
   */
  public boolean isWebPSupported() {
    SystemType os = FileUtil.getCurrentSystem();
    log.debug("현재 운영체제: {}", os);
    if (os == SystemType.WINDOWS || os == SystemType.LINUX) {
      log.debug("WebP 지원 운영체제 감지: {}", os);
      return true;
    }
    if (os == SystemType.MAC || os == SystemType.OTHER) {
      log.debug("WebP 비지원 운영체제 감지: {}", os);
      return false;
    }
    log.warn("알 수 없는 운영체제입니다. 기본 출력 형식을 'jpg'로 설정합니다.");
    return false;
  }

  /**
   * 출력 형식에 따른 MimeType 반환
   *
   * @return 출력 형식에 해당하는 MimeType
   */
  public MimeType getOutputThumbnailMimeType() {
    log.debug("출력 형식에 따른 MimeType 반환 시작: {}", outputThumbnailFormat);
    switch (outputThumbnailFormat.toLowerCase()) {
      case "jpg":
      case "jpeg":
        return MimeType.JPEG;
      case "webp":
        return MimeType.WEBP;
      case "png":
        return MimeType.PNG;
      case "gif":
        return MimeType.GIF;
      case "bmp":
        return MimeType.BMP;
      case "tiff":
        return MimeType.TIFF;
      case "svg":
        return MimeType.SVG;
      default:
        log.error("지원되지 않는 출력 형식: {}", outputThumbnailFormat);
        throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }
  }

  /**
   * 이미지 압축 생성 (퀄리티별)
   * 1. 이미지 파일 검증
   * 2. WebP 특별 처리
   * 3. OS별 출력 포맷 결정
   * 4. 이미지 압축 처리
   *
   * @param file          압축할 이미지 파일
   * @param imageQuality  이미지 품질 설정
   * @return 압축된 이미지 바이트 배열
   */
  public byte[] generateImageCompress(MultipartFile file, ImageQuality imageQuality) {
    log.info("이미지 압축 생성 시작: {}, 품질: {}", file.getOriginalFilename(), imageQuality.name());

    // 1. 이미지 파일 검증
    String mimeType = file.getContentType();
    if (mimeType == null || !MimeType.isValidImageMimeType(mimeType)) {
      log.warn("지원되지 않는 이미지 MIME 타입: {}", mimeType);
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }
    log.debug("이미지 MIME 타입 검증 완료: {}", mimeType);

    // 2. WebP 포맷 특별 처리
    if (mimeType.equalsIgnoreCase(MimeType.WEBP.getMimeType())) {
      log.info("WebP 형식의 이미지입니다. 원본 데이터를 반환합니다: {}", file.getOriginalFilename());
      try {
        return file.getBytes(); // 원본 데이터 반환
      } catch (IOException e) {
        log.error("WebP 이미지 파일 읽기 중 오류 발생: {}", e.getMessage());
        throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
      }
    }

    // 3. OS별 출력 포맷 결정
    String targetFormat = isWebPSupported() ? "webp" : "jpg";
    log.info("운영체제에 따라 변환할 포맷 결정: {}", targetFormat);

    // 4. 이미지 압축 처리
    ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();
    try {
      log.debug("압축 처리 시작: 포맷={}, 품질={}", targetFormat, imageQuality.getOutputQuality());
      Thumbnails.of(file.getInputStream())
          .scale(imageQuality.getScale())
          .outputFormat(targetFormat) // OS별 포맷 적용
          .outputQuality(imageQuality.getOutputQuality())
          .allowOverwrite(true)
          .toOutputStream(compressedOutputStream);
      log.info("이미지 압축 생성 완료: {}, 품질: {}", file.getOriginalFilename(), imageQuality.name());
    } catch (Exception e) {
      log.error("이미지 압축 생성 중 오류 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
    }
    return compressedOutputStream.toByteArray();
  }

  /**
   * 이미지 썸네일 생성
   * 1. 이미지 파일 검증
   * 2. WebP 특별 처리
   * 3. 썸네일 크기 조정 (600x600)
   *
   * @param file 썸네일을 생성할 이미지 파일
   * @return 썸네일 이미지 바이트 배열
   */
  public byte[] generateImageThumbnail(MultipartFile file) {
    log.info("이미지 썸네일 생성 시작: {}", file.getOriginalFilename());

    // 1. 이미지 파일 검증
    String mimeType = file.getContentType();
    if (mimeType == null || !MimeType.isValidImageMimeType(mimeType)) {
      log.warn("지원되지 않는 이미지 MIME 타입: {}", mimeType);
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }
    log.debug("이미지 MIME 타입 검증 완료: {}", mimeType);

    // 2. WebP 포맷 특별 처리
    if (mimeType.equalsIgnoreCase(MimeType.WEBP.getMimeType())) {
      log.info("WebP 형식의 이미지입니다. 원본 데이터를 반환합니다: {}", file.getOriginalFilename());
      try {
        return file.getBytes(); // 원본 데이터 반환
      } catch (IOException e) {
        log.error("WebP 이미지 파일 읽기 중 오류 발생: {}", e.getMessage());
        throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
      }
    }

    // 3. 썸네일 크기 조정 (600x600)
    ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
    try {
      log.debug("썸네일 생성: 출력 형식={}, 파일 이름={}", outputThumbnailFormat, file.getOriginalFilename());
      Thumbnails.of(file.getInputStream())
          .size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
          .outputFormat(outputThumbnailFormat) // OS별 포맷 적용
          .outputQuality(0.7)
          .allowOverwrite(true)
          .toOutputStream(thumbnailOutputStream);
      log.info("이미지 썸네일 생성 완료: {}", file.getOriginalFilename());
    } catch (Exception e) {
      log.error("이미지 썸네일 생성 중 오류 발생: 파일 이름={}, MIME 타입={}, 오류 메시지={}",
          file.getOriginalFilename(), mimeType, e.getMessage(), e);
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
    }
    return thumbnailOutputStream.toByteArray();
  }

  /**
   * 문서 썸네일 생성
   * 1. PDF: 첫 페이지 렌더링
   * 2. Word: 첫 문단 텍스트
   * 3. Excel: 시트명
   * 4. PPT: 첫 슬라이드
   *
   * @param file 썸네일을 생성할 문서 파일
   * @return 문서 썸네일 이미지 바이트 배열
   */
  public byte[] generateDocumentThumbnail(MultipartFile file) {
    String mimeType = file.getContentType();
    log.info("파일 {} 의 썸네일 생성 시작. MimeType: {}", file.getOriginalFilename(), mimeType);

    if (mimeType == null) {
      log.warn("파일 {} 의 MimeType을 확인할 수 없습니다.", file.getOriginalFilename());
      return new byte[0];
    }

    byte[] thumbnailBytes;

    try {
      // PDF 파일 처리
      if (mimeType.equalsIgnoreCase(MimeType.PDF.getMimeType())) {
        log.info("PDF 파일 감지: {}", file.getOriginalFilename());
        thumbnailBytes = generatePdfThumbnail(file.getInputStream());
      }
      // Word 문서 처리
      else if (mimeType.equalsIgnoreCase(MimeType.DOCX.getMimeType()) || mimeType.equalsIgnoreCase(MimeType.DOC.getMimeType())) {
        log.info("Word 문서 감지: {}", file.getOriginalFilename());
        thumbnailBytes = generateWordThumbnail(file.getInputStream());
      }
      // Excel 파일 처리
      else if (mimeType.equalsIgnoreCase(MimeType.XLSX.getMimeType()) || mimeType.equalsIgnoreCase(MimeType.XLS.getMimeType())) {
        log.info("Excel 파일 감지: {}", file.getOriginalFilename());
        thumbnailBytes = generateExcelThumbnail(file.getInputStream());
      }
      // PowerPoint 파일 처리
      else if (mimeType.equalsIgnoreCase(MimeType.PPTX.getMimeType()) || mimeType.equalsIgnoreCase(MimeType.PPT.getMimeType())) {
        log.info("PowerPoint 파일 감지: {}", file.getOriginalFilename());
        thumbnailBytes = generatePowerPointThumbnail(file.getInputStream());
      } else {
        log.warn("지원되지 않는 파일 형식: {}", mimeType);
        return new byte[0];
      }

      log.info("파일 {} 의 썸네일 생성 완료. 생성된 썸네일 크기: {} bytes", file.getOriginalFilename(), thumbnailBytes.length);
      return thumbnailBytes;
    } catch (IOException e) {
      log.error("파일 {} 썸네일 생성 중 오류 발생: {}", file.getOriginalFilename(), e.getMessage());
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
    }
  }

  /**
   * 비디오 썸네일 생성
   * 1. 임시 파일 생성 및 복사
   * 2. FFmpeg으로 프레임 추출 (5초 지점)
   * 3. 이미지 변환 및 크기 조정
   * 4. 리소스 정리
   *
   * @param file 썸네일을 생성할 비디오 파일
   * @return 비디오 썸네일 이미지 바이트 배열
   */
  public byte[] generateVideoThumbnail(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      log.error("비디오 파일이 비어있습니다.");
      throw new CustomException(ErrorCode.FILE_EMPTY);
    }

    String mimeType = file.getContentType();
    if (mimeType == null || !mimeType.startsWith("video/")) {
      log.error("올바르지 않은 비디오 형식: {}", mimeType);
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }

    File tempFile = null;
    FFmpegFrameGrabber grabber = null;
    Java2DFrameConverter converter = null;
    ByteArrayOutputStream outputStream = null;

    try {
      // 임시 파일 생성 및 복사
      String extension = FileUtil.getExtension(file.getOriginalFilename());
      Path tempPath = Files.createTempFile("video_thumbnail_", "." + extension);
      tempFile = tempPath.toFile();
      tempFile.deleteOnExit();
      Files.copy(file.getInputStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);
      log.debug("임시 비디오 파일 생성 완료: {}", tempPath.toString());

      // FFmpeg으로 프레임 추출 (5초 지점)
      grabber = new FFmpegFrameGrabber(tempFile);
      grabber.start();
      log.debug("FFmpegFrameGrabber 시작");
      grabber.setTimestamp(5000000); // 5초 지점
      Frame frame = grabber.grab();

      if (frame == null) {
        log.error("비디오 프레임 추출 실패: {}", file.getOriginalFilename());
        throw new CustomException(ErrorCode.VIDEO_FRAME_EXTRACTION_ERROR);
      }
      log.debug("비디오 프레임 추출 성공");

      // 이미지 변환 및 크기 조정
      converter = new Java2DFrameConverter();
      BufferedImage bufferedImage = converter.convert(frame);
      log.debug("프레임을 BufferedImage로 변환 완료");

      BufferedImage thumbnail = Thumbnails.of(bufferedImage)
          .size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
          .outputQuality(1.0)
          .asBufferedImage();
      log.debug("썸네일 크기 조정 완료");

      // 바이트 배열로 변환
      outputStream = new ByteArrayOutputStream();
      ImageIO.write(thumbnail, outputThumbnailFormat, outputStream);
      log.info("비디오 썸네일 생성 완료: {}", file.getOriginalFilename());

      return outputStream.toByteArray();

    } catch (Exception e) {
      log.error("비디오 썸네일 생성 실패 - 파일: {}, 에러: {}",
          file.getOriginalFilename(), e.getMessage());
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);

    } finally {
      // 리소스 정리
      try {
        if (grabber != null) {
          grabber.stop();
          grabber.release();
          log.debug("FFmpegFrameGrabber 리소스 정리 완료");
        }
        if (tempFile != null && tempFile.exists()) {
          tempFile.delete();
          log.debug("임시 파일 삭제 완료: {}", tempFile.getAbsolutePath());
        }
        if (outputStream != null) {
          outputStream.close();
          log.debug("ByteArrayOutputStream 닫기 완료");
        }
      } catch (Exception e) {
        log.error("리소스 정리 중 오류: {}", e.getMessage());
      }
    }
  }

  /**
   * PDF 썸네일 생성
   *
   * @param pdfInputStream PDF 파일의 InputStream
   * @return PDF 썸네일 이미지 바이트 배열
   * @throws IOException PDF 처리 중 발생한 예외
   */
  private byte[] generatePdfThumbnail(InputStream pdfInputStream) throws IOException {
    log.info("PDF 썸네일 생성 시작");
    try (PDDocument document = PDDocument.load(pdfInputStream)) {
      log.debug("PDF 문서 로드 완료, 페이지 수: {}", document.getNumberOfPages());
      PDFRenderer pdfRenderer = new PDFRenderer(document);
      BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300); // 높은 DPI로 렌더링
      log.debug("PDF 첫 페이지 렌더링 완료");

      // 자동 크기 조정
      int[] scaledDimensions = getScaledDimensions(bim);
      log.debug("PDF 렌더링 완료, 조정된 크기: width={}, height={}", scaledDimensions[0], scaledDimensions[1]);

      BufferedImage thumbnail = Thumbnails.of(bim)
          .size(scaledDimensions[0], scaledDimensions[1])
          .outputQuality(1.0)
          .asBufferedImage();
      log.debug("PDF 썸네일 크기 조정 완료");

      ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
      ImageIO.write(thumbnail, outputThumbnailFormat, thumbnailOutputStream);
      log.info("PDF 썸네일 생성 완료");
      return thumbnailOutputStream.toByteArray();
    } catch (Exception e) {
      log.error("PDF 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
    }
  }

  /**
   * Word 썸네일 생성
   *
   * @param docInputStream Word 파일의 InputStream
   * @return Word 썸네일 이미지 바이트 배열
   * @throws IOException Word 처리 중 발생한 예외
   */
  private byte[] generateWordThumbnail(InputStream docInputStream) throws IOException {
    log.info("Word 썸네일 생성 시작");
    try (XWPFDocument document = new XWPFDocument(docInputStream)) {
      BufferedImage img = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = img.createGraphics();
      graphics.setFont(new Font("Arial", Font.BOLD, 24));
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      // 첫 문단 텍스트 추출
      String firstPageText = document.getParagraphs().stream()
          .map(paragraph -> paragraph.getText())
          .findFirst()
          .orElse("내용 없음");
      log.debug("Word 썸네일에 포함된 첫 페이지 텍스트: {}", firstPageText);

      graphics.drawString(firstPageText, 50, 100); // 텍스트 위치 조정
      graphics.dispose();
      log.debug("Word 썸네일 이미지 생성 완료");

      ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
      Thumbnails.of(img)
          .size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
          .outputFormat(outputThumbnailFormat)
          .outputQuality(1.0)
          .toOutputStream(thumbnailOutputStream);
      log.info("Word 썸네일 생성 완료");
      return thumbnailOutputStream.toByteArray();
    } catch (Exception e) {
      log.error("Word 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
    }
  }

  /**
   * Excel 썸네일 생성
   *
   * @param xlsInputStream Excel 파일의 InputStream
   * @return Excel 썸네일 이미지 바이트 배열
   * @throws IOException Excel 처리 중 발생한 예외
   */
  private byte[] generateExcelThumbnail(InputStream xlsInputStream) throws IOException {
    log.info("Excel 썸네일 생성 시작");
    try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(xlsInputStream)) {
      String sheetName = workbook.getSheetAt(0).getSheetName();
      log.debug("Excel 시트 이름: {}", sheetName);

      BufferedImage img = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = img.createGraphics();
      graphics.setFont(new Font("Arial", Font.BOLD, 24));
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.drawString("Sheet: " + sheetName, 50, 100);
      graphics.dispose();
      log.debug("Excel 썸네일 이미지 생성 완료");

      ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
      Thumbnails.of(img)
          .size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
          .outputFormat(outputThumbnailFormat)
          .outputQuality(1.0) // 화질을 최대로 설정
          .toOutputStream(thumbnailOutputStream);
      log.info("Excel 썸네일 생성 완료");
      return thumbnailOutputStream.toByteArray();
    } catch (Exception e) {
      log.error("Excel 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
    }
  }

  /**
   * PowerPoint 썸네일 생성
   *
   * @param pptInputStream PowerPoint 파일의 InputStream
   * @return PowerPoint 썸네일 이미지 바이트 배열
   * @throws IOException PowerPoint 처리 중 발생한 예외
   */
  private byte[] generatePowerPointThumbnail(InputStream pptInputStream) throws IOException {
    log.info("PowerPoint 썸네일 생성 시작");
    try (XMLSlideShow ppt = new XMLSlideShow(pptInputStream)) {
      if (ppt.getSlides().isEmpty()) {
        log.warn("PowerPoint 파일에 슬라이드가 없습니다.");
        return new byte[0];
      }
      XSLFSlide slide = ppt.getSlides().get(0);
      Dimension pageSize = ppt.getPageSize();
      log.debug("PowerPoint 슬라이드 크기: width={}, height={}", pageSize.width, pageSize.height);

      BufferedImage img = new BufferedImage(pageSize.width, pageSize.height, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = img.createGraphics();
      slide.draw(graphics);
      graphics.dispose();
      log.debug("PowerPoint 첫 슬라이드 이미지로 변환 완료");

      // 자동 크기 조정
      int[] scaledDimensions = getScaledDimensions(img);
      log.debug("PowerPoint 슬라이드 조정된 크기: width={}, height={}", scaledDimensions[0], scaledDimensions[1]);

      BufferedImage thumbnail = Thumbnails.of(img)
          .size(scaledDimensions[0], scaledDimensions[1])
          .outputFormat(outputThumbnailFormat)
          .outputQuality(1.0)
          .asBufferedImage();
      log.debug("PowerPoint 썸네일 크기 조정 완료");

      ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
      ImageIO.write(thumbnail, outputThumbnailFormat, thumbnailOutputStream);
      log.info("PowerPoint 썸네일 생성 완료");
      return thumbnailOutputStream.toByteArray();
    } catch (Exception e) {
      log.error("PowerPoint 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
    }
  }

  /**
   * 자동 크기 조정 로직
   * 이미지의 원본 크기를 유지하면서 지정된 최대 크기 내로 조정
   *
   * @param image 원본 이미지
   * @return 조정된 너비와 높이의 배열 [width, height]
   */
  private int[] getScaledDimensions(BufferedImage image) {
    int originalWidth = image.getWidth();
    int originalHeight = image.getHeight();
    double widthRatio = (double) DEFAULT_WIDTH / originalWidth;
    double heightRatio = (double) DEFAULT_HEIGHT / originalHeight;
    double scaleFactor = Math.min(widthRatio, heightRatio);

    int newWidth = (int) (originalWidth * scaleFactor);
    int newHeight = (int) (originalHeight * scaleFactor);

    log.debug("이미지 크기 조정: originalWidth={}, originalHeight={}, newWidth={}, newHeight={}",
        originalWidth, originalHeight, newWidth, newHeight);
    return new int[]{newWidth, newHeight};
  }

  /**
   * 썸네일 파일명 생성
   *
   * @param contentType      ContentType
   * @param originalFileName 원본 파일명
   * @return 생성된 썸네일 파일명
   */
  public String generateThumbnailFileName(ContentType contentType, String originalFileName) {
    log.debug("썸네일 파일명 생성 시작: originalFileName={}, contentType={}", originalFileName, contentType);
    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
    String baseName = FileUtil.getBaseName(originalFileName);
    // 출력 형식에 따라 동적으로 확장자 결정
    String thumbnailExtension = getOutputThumbnailMimeType().getMimeType().split("/")[1];
    String thumbnailFileName = String.format("%s_%s_%s.%s", baseName, curTimeStr, "thumbnail", thumbnailExtension);
    log.debug("생성된 썸네일 파일명: {}", thumbnailFileName);
    return thumbnailFileName;
  }
}
