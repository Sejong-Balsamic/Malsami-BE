package com.balsamic.sejongmalsami.util.config;

import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/* swagger에서 정적 태그 선택시 한글로 들어오는 값 Enum으로 변환*/
@Component
public class StringToQuestionPresetTagConverter implements Converter<String, QuestionPresetTag> {

  @Override
  public QuestionPresetTag convert(String source) {
    for (QuestionPresetTag tag : QuestionPresetTag.values()) {
      if (tag.getDescription().equals(source)) {
        return tag;
      }
    }
    throw new IllegalArgumentException("알 수 없는 enum 형식입니다. source: " + source);
  }
}

