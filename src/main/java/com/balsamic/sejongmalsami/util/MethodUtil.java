package com.balsamic.sejongmalsami.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class MethodUtil {

  // 변환 메서드
  @NotNull
  public static <T, D> List<D> convertToDtoList(List<T> entities, Function<T, D> converter) {
    return entities.stream()
        .map(converter)
        .collect(Collectors.toList());
  }


}
