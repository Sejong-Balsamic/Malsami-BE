package com.balsamic.sejongmalsami.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

/**
 * MultipartFile을 byte 배열로 감싸는 어댑터 클래스
 * (썸네일 생성 후 byte 배열을 MultipartFile로 사용하기 위함)
 */
@Slf4j
public class MultipartFileAdapter implements MultipartFile {

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
    return "image/jpeg"; // 썸네일의 경우 주로 JPEG 형식 사용
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
    return new ByteArrayInputStream(content);
  }

  @Override
  public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
    throw new UnsupportedOperationException("transferTo는 지원되지 않습니다.");
  }
}
