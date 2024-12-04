package com.balsamic.sejongmalsami.util.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String[]> {
  @Override
  public String[] convertToDatabaseColumn(List<String> list) {
    return list != null ? list.toArray(new String[0]) : new String[0];
  }

  @Override
  public List<String> convertToEntityAttribute(String[] array) {
    return array != null ? Arrays.asList(array) : new ArrayList<>();
  }
}
