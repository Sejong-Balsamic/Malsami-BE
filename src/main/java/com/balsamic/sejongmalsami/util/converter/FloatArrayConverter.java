package com.balsamic.sejongmalsami.util.converter;

import com.balsamic.sejongmalsami.util.CommonUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FloatArrayConverter implements AttributeConverter<float[], String> {

  @Override
  public String convertToDatabaseColumn(float[] attribute) {
    return CommonUtil.floatArrayToString(attribute);
  }

  @Override
  public float[] convertToEntityAttribute(String dbData) {
    return CommonUtil.stringToFloatArray(dbData);
  }
}