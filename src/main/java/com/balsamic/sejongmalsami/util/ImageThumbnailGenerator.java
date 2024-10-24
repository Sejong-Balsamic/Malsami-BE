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

  private final String outputThumbnailFormat;

  public ImageThumbnailGenerator() {
    if (isWebPSupported()) {
      this.outputThumbnailFormat = "webp";
    } else {
      this.outputThumbnailFormat = "jpg";
    }
    log.info("선택된 썸네일 이미지 형식: {}", outputThumbnailFormat);
  }


  /**
   * 운영체제에 대한 Webp 변경 라이브러리 호환 여부 확인
   * WINDOW, LINUX : webp 포맷변경 가능
   * MAC, OTHER : 불가능
   */
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
   * @param file
   * @return
   * @throws IOException
   */
  public byte[] generateImageThumbnail(MultipartFile file) throws IOException {
    log.info("이미지 썸네일 생성 시작: {}", file.getOriginalFilename());

    if (Objects.requireNonNull(file.getContentType()).equals(MimeType.WEBP.getMimeType())) {
      log.info("WebP 형식의 이미지입니다. 썸네일 생성을 건너뜁니다: {}", file.getOriginalFilename());
      // WebP 이미지를 그대로 반환 (썸네일 생성 없음)
      return file.getBytes();
    }

    ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
    try {
      Thumbnails.of(file.getInputStream())
          .size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
          .keepAspectRatio(true)
          .outputFormat(outputThumbnailFormat)
          .toOutputStream(thumbnailOutputStream);
      log.info("이미지 썸네일 생성 완료: {}", file.getOriginalFilename());
    } catch (Exception e) {
      log.error("이미지 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }
    return thumbnailOutputStream.toByteArray();
  }

  /**
   * 문서 썸네일 생성
   * @param file
   * @return
   * @throws IOException
   */
  public byte[] generateDocumentThumbnail(MultipartFile file) throws IOException {
    String mimeType = file.getContentType();
    log.info("파일 {} 의 썸네일 생성 시작. MimeType: {}", file.getOriginalFilename(), mimeType);

    if (mimeType == null) {
      log.warn("파일 {} 의 MimeType을 확인할 수 없습니다.", file.getOriginalFilename());
      return new byte[0];
    }

    byte[] thumbnailBytes;

    if (mimeType.equals(MimeType.PDF.getMimeType())) {
      log.info("PDF 파일 감지: {}", file.getOriginalFilename());
      thumbnailBytes = generatePdfThumbnail(file.getInputStream());
    } else if (mimeType.equals(MimeType.DOCX.getMimeType()) || mimeType.equals(MimeType.DOC.getMimeType())) {
      log.info("Word 문서 감지: {}", file.getOriginalFilename());
      thumbnailBytes = generateWordThumbnail(file.getInputStream());
    } else if (mimeType.equals(MimeType.XLSX.getMimeType()) || mimeType.equals(MimeType.XLS.getMimeType())) {
      log.info("Excel 파일 감지: {}", file.getOriginalFilename());
      thumbnailBytes = generateExcelThumbnail(file.getInputStream());
    } else if (mimeType.equals(MimeType.PPTX.getMimeType()) || mimeType.equals(MimeType.PPT.getMimeType())) {
      log.info("PowerPoint 파일 감지: {}", file.getOriginalFilename());
      thumbnailBytes = generatePowerPointThumbnail(file.getInputStream());
    } else {
      log.warn("지원되지 않는 파일 형식: {}", mimeType);
      return new byte[0];
    }

    log.info("파일 {} 의 썸네일 생성 완료. 생성된 썸네일 크기: {} bytes", file.getOriginalFilename(), thumbnailBytes.length);
    return thumbnailBytes;
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
      ImageIO.write(thumbnail, outputThumbnailFormat, thumbnailOutputStream);
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
      ImageIO.write(img, outputThumbnailFormat, thumbnailOutputStream);
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
      ImageIO.write(img, outputThumbnailFormat, thumbnailOutputStream);
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
      ImageIO.write(thumbnail, outputThumbnailFormat, thumbnailOutputStream);
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