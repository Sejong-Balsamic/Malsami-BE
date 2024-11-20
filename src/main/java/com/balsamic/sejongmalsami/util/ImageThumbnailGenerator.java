package com.balsamic.sejongmalsami.util;

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
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
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
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
@Getter
@RequiredArgsConstructor
public class ImageThumbnailGenerator {

  public static final int DEFAULT_WIDTH = 600; // 해상도 향상을 위해 크기 증가
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

  private boolean isWebPSupported() {
    SystemType os = FileUtil.getCurrentSystem();
    if (os == SystemType.WINDOWS || os == SystemType.LINUX) {
      return true;
    }
    if (os == SystemType.MAC || os == SystemType.OTHER) {
      return false;
    }
    log.warn("알 수 없는 운영체제입니다. 기본 출력 형식을 'jpg'로 설정합니다.");
    return false;
  }

  /**
   * 이미지 썸네일 생성
   */
  public byte[] generateImageThumbnail(MultipartFile file) {
    log.info("이미지 썸네일 생성 시작: {}", file.getOriginalFilename());

    if (Objects.requireNonNull(file.getContentType()).equalsIgnoreCase(MimeType.WEBP.getMimeType())) {
      log.info("WebP 형식의 이미지입니다. 썸네일 생성을 건너뜁니다: {}", file.getOriginalFilename());
      try {
        return file.getBytes();
      } catch (IOException e) {
        log.error("WebP 이미지 파일 읽기 중 오류 발생: {}", e.getMessage());
        throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
      }
    }

    ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
    try {
      Thumbnails.of(file.getInputStream())
          .size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
          .outputFormat(outputThumbnailFormat)
          .outputQuality(1.0) // 화질을 최대로 설정
          .allowOverwrite(true)
          .toOutputStream(thumbnailOutputStream);
      log.info("이미지 썸네일 생성 완료: {}", file.getOriginalFilename());
    } catch (Exception e) {
      log.error("이미지 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
    }
    return thumbnailOutputStream.toByteArray();
  }

  /**
   * 문서 썸네일 생성
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
      if (mimeType.equalsIgnoreCase(MimeType.PDF.getMimeType())) {
        log.info("PDF 파일 감지: {}", file.getOriginalFilename());
        thumbnailBytes = generatePdfThumbnail(file.getInputStream());
      } else if (mimeType.equalsIgnoreCase(MimeType.DOCX.getMimeType()) || mimeType.equalsIgnoreCase(MimeType.DOC.getMimeType())) {
        log.info("Word 문서 감지: {}", file.getOriginalFilename());
        thumbnailBytes = generateWordThumbnail(file.getInputStream());
      } else if (mimeType.equalsIgnoreCase(MimeType.XLSX.getMimeType()) || mimeType.equalsIgnoreCase(MimeType.XLS.getMimeType())) {
        log.info("Excel 파일 감지: {}", file.getOriginalFilename());
        thumbnailBytes = generateExcelThumbnail(file.getInputStream());
      } else if (mimeType.equalsIgnoreCase(MimeType.PPTX.getMimeType()) || mimeType.equalsIgnoreCase(MimeType.PPT.getMimeType())) {
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
   * VIDEO 썸네일 생성 (FFmpeg 사용 예정)
   */
  public byte[] generateVideoThumbnail(MultipartFile file) {
    String mimeType = file.getContentType();
    log.info("동영상 썸네일 생성 시작: {}", file.getOriginalFilename());

    if (mimeType == null || !mimeType.startsWith("video/")) {
      log.warn("지원되지 않는 동영상 타입: {}", mimeType);
      return new byte[0];
    }

    try {
      // TODO: FFmpeg 등을 사용하여 실제 썸네일 생성
      // 현재는 더미 이미지 반환
      BufferedImage img = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = img.createGraphics();
      graphics.setFont(new Font("Arial", Font.BOLD, 24));
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.drawString("Video Thumbnail", 50, 100);
      graphics.dispose();

      ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
      ImageIO.write(img, outputThumbnailFormat, thumbnailOutputStream);
      log.info("동영상 썸네일 생성 완료");
      return thumbnailOutputStream.toByteArray();
    } catch (IOException e) {
      log.error("동영상 썸네일 생성 중 오류 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
    }
  }

  /**
   * PDF 썸네일 생성
   */
  private byte[] generatePdfThumbnail(InputStream pdfInputStream) throws IOException {
    log.info("PDF 썸네일 생성 시작");
    try (PDDocument document = PDDocument.load(pdfInputStream)) {
      log.info("PDF 문서 로드 완료, 페이지 수: {}", document.getNumberOfPages());
      PDFRenderer pdfRenderer = new PDFRenderer(document);
      BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300); // 높은 DPI로 렌더링
      int[] scaledDimensions = getScaledDimensions(bim);
      log.info("PDF 렌더링 완료, 조정된 크기: width={}, height={}", scaledDimensions[0], scaledDimensions[1]);

      BufferedImage thumbnail = Thumbnails.of(bim)
          .size(scaledDimensions[0], scaledDimensions[1])
          .outputQuality(1.0)
          .asBufferedImage();

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
   */
  private byte[] generateWordThumbnail(InputStream docInputStream) throws IOException {
    log.info("Word 썸네일 생성 시작");
    try (XWPFDocument document = new XWPFDocument(docInputStream)) {
      BufferedImage img = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = img.createGraphics();
      graphics.setFont(new Font("Arial", Font.BOLD, 24));
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      String firstPageText = document.getParagraphs().stream()
          .map(paragraph -> paragraph.getText())
          .findFirst()
          .orElse("내용 없음");
      log.info("썸네일에 포함된 첫 페이지 텍스트: {}", firstPageText);

      graphics.drawString(firstPageText, 50, 100); // 텍스트 위치 조정
      graphics.dispose();

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
   */
  private byte[] generateExcelThumbnail(InputStream xlsInputStream) throws IOException {
    log.info("Excel 썸네일 생성 시작");
    try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(xlsInputStream)) {
      String sheetName = workbook.getSheetAt(0).getSheetName();
      log.info("Excel 시트 이름: {}", sheetName);

      BufferedImage img = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = img.createGraphics();
      graphics.setFont(new Font("Arial", Font.BOLD, 24));
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.drawString("Sheet: " + sheetName, 50, 100);
      graphics.dispose();

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
      log.info("PowerPoint 슬라이드 크기: width={}, height={}", pageSize.width, pageSize.height);

      BufferedImage img = new BufferedImage(pageSize.width, pageSize.height, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = img.createGraphics();
      slide.draw(graphics);
      graphics.dispose();

      int[] scaledDimensions = getScaledDimensions(img);
      log.info("PowerPoint 슬라이드 조정된 크기: width={}, height={}", scaledDimensions[0], scaledDimensions[1]);

      BufferedImage thumbnail = Thumbnails.of(img)
          .size(scaledDimensions[0], scaledDimensions[1])
          .outputFormat(outputThumbnailFormat)
          .outputQuality(1.0)
          .asBufferedImage();

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
   */
  private int[] getScaledDimensions(BufferedImage image) {
    int originalWidth = image.getWidth();
    int originalHeight = image.getHeight();
    double widthRatio = (double) DEFAULT_WIDTH / originalWidth;
    double heightRatio = (double) DEFAULT_HEIGHT / originalHeight;
    double scaleFactor = Math.min(widthRatio, heightRatio);

    int newWidth = (int) (originalWidth * scaleFactor);
    int newHeight = (int) (originalHeight * scaleFactor);

    log.info("이미지 크기 조정: originalWidth={}, originalHeight={}, newWidth={}, newHeight={}", originalWidth, originalHeight, newWidth, newHeight);
    return new int[]{newWidth, newHeight};
  }

  /**
   * MultipartFile을 byte 배열로 감싸는 어댑터 클래스
   * (썸네일 생성 후 업로드를 위해 사용)
   */
  public static class MultipartFileAdapter implements MultipartFile {
    private final String fileName;
    private final byte[] content;

    public MultipartFileAdapter(String fileName, byte[] content) {
      this.fileName = fileName;
      this.content = content;
    }

    @Override
    public String getName() {
      return fileName;
    }

    @Override
    public String getOriginalFilename() {
      return fileName;
    }

    @Override
    public String getContentType() {
      return "image/jpeg"; // 썸네일 형식에 맞게 변경 가능
    }

    @Override
    public boolean isEmpty() {
      return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
      return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
      return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return new java.io.ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
      throw new UnsupportedOperationException("transferTo는 지원되지 않습니다.");
    }
  }
}
