package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.object.constants.SystemType;
import java.awt.Dimension;
import java.awt.Graphics2D;
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

  // 썸네일 기본 크기 설정
  public static final int DEFAULT_WIDTH = 300;
  public static final int DEFAULT_HEIGHT = 300;

  private final String outputFormat;

  public ImageThumbnailGenerator() {
    this.outputFormat = isWebPSupported() ? "webp" : "jpg";
    log.info("Selected image format for thumbnails: {}", outputFormat);
  }

  private boolean isWebPSupported() {
    SystemType os = FileUtil.getCurrentSystem();
    switch (os) {
      case WINDOWS:
      case LINUX:
        return true;
      case MAC:
        return false;
      default:
        log.warn("Unknown operating system. Defaulting output format to 'jpg'.");
        return false;
    }
  }

  // 이미지 썸네일 생성
  public byte[] generateImageThumbnail(MultipartFile multipartFile) throws IOException {
    log.info("이미지 썸네일 생성 시작: {}", multipartFile.getOriginalFilename());

    if (Objects.requireNonNull(multipartFile.getContentType()).equals(MimeType.WEBP.getMimeType())) {
      log.info("WebP 형식의 이미지입니다. 썸네일 생성을 건너뜁니다: {}", multipartFile.getOriginalFilename());
      // WebP 이미지를 그대로 반환 (썸네일 생성 없음)
      return multipartFile.getBytes();
    }

    ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
    try {
      Thumbnails.of(multipartFile.getInputStream())
          .size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
          .keepAspectRatio(true)
          .outputFormat(outputFormat)
          .toOutputStream(thumbnailOutputStream);
      log.info("이미지 썸네일 생성 완료: {}", multipartFile.getOriginalFilename());
    } catch (Exception e) {
      log.error("이미지 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }
    return thumbnailOutputStream.toByteArray();
  }

  // PDF 썸네일 생성
  public byte[] generatePdfThumbnail(InputStream pdfInputStream) throws IOException {
    log.info("PDF 썸네일 생성 시작");
    try (PDDocument document = PDDocument.load(pdfInputStream)) {
      log.info("PDF 문서 로드 완료, 페이지 수: {}", document.getNumberOfPages());
      PDFRenderer pdfRenderer = new PDFRenderer(document);
      BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300);
      int[] scaledDimensions = getScaledDimensions(bim);
      log.info("PDF 렌더링 완료, 조정된 크기: width={}, height={}", scaledDimensions[0], scaledDimensions[1]);

      BufferedImage thumbnail = Thumbnails.of(bim)
          .size(scaledDimensions[0], scaledDimensions[1])
          .asBufferedImage();

      ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
      ImageIO.write(thumbnail, outputFormat, thumbnailOutputStream);
      log.info("PDF 썸네일 생성 완료");
      return thumbnailOutputStream.toByteArray();
    } catch (Exception e) {
      log.error("PDF 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }
  }

  // Word 썸네일 생성
  public byte[] generateWordThumbnail(InputStream docInputStream) throws IOException {
    log.info("Word 썸네일 생성 시작");
    try (XWPFDocument document = new XWPFDocument(docInputStream)) {
      BufferedImage img = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = img.createGraphics();
      String firstPageText = document.getParagraphs().stream()
          .map(paragraph -> paragraph.getText())
          .findFirst()
          .orElse("내용 없음");
      log.info("썸네일에 포함된 첫 페이지 텍스트: {}", firstPageText);

      graphics.drawString(firstPageText, 10, 20);

      ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
      ImageIO.write(img, outputFormat, thumbnailOutputStream);
      log.info("Word 썸네일 생성 완료");
      return thumbnailOutputStream.toByteArray();
    } catch (Exception e) {
      log.error("Word 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }
  }

  // Excel 썸네일 생성
  public byte[] generateExcelThumbnail(InputStream xlsInputStream) throws IOException {
    log.info("Excel 썸네일 생성 시작");
    try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(xlsInputStream)) {
      String sheetName = workbook.getSheetAt(0).getSheetName();
      log.info("Excel 시트 이름: {}", sheetName);

      BufferedImage img = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = img.createGraphics();
      graphics.drawString("Sheet: " + sheetName, 10, 20);

      ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
      ImageIO.write(img, outputFormat, thumbnailOutputStream);
      log.info("Excel 썸네일 생성 완료");
      return thumbnailOutputStream.toByteArray();
    } catch (Exception e) {
      log.error("Excel 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }
  }

  // PowerPoint 썸네일 생성
  public byte[] generatePowerPointThumbnail(InputStream pptInputStream) throws IOException {
    log.info("PowerPoint 썸네일 생성 시작");
    try (XMLSlideShow ppt = new XMLSlideShow(pptInputStream)) {
      XSLFSlide slide = ppt.getSlides().get(0);
      Dimension pageSize = ppt.getPageSize();
      log.info("PowerPoint 슬라이드 크기: width={}, height={}", pageSize.width, pageSize.height);

      BufferedImage img = new BufferedImage(pageSize.width, pageSize.height, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = img.createGraphics();
      slide.draw(graphics);
      int[] scaledDimensions = getScaledDimensions(img);
      log.info("PowerPoint 슬라이드 조정된 크기: width={}, height={}", scaledDimensions[0], scaledDimensions[1]);

      BufferedImage thumbnail = Thumbnails.of(img)
          .size(scaledDimensions[0], scaledDimensions[1])
          .asBufferedImage();

      ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
      ImageIO.write(thumbnail, outputFormat, thumbnailOutputStream);
      log.info("PowerPoint 썸네일 생성 완료");
      return thumbnailOutputStream.toByteArray();
    } catch (Exception e) {
      log.error("PowerPoint 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }
  }

  // 자동 크기 조정 로직
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
}