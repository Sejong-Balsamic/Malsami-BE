package com.balsamic.sejongmalsami.object.constants;

import lombok.Getter;

@Getter
public enum ImageQuality {
  ORIGINAL(1.0, 1.0),
  HIGH(0.9, 0.9),
  MEDIUM(0.7, 0.7),
  LOW(0.5, 0.5);

  private final double scale;
  private final double outputQuality;

  ImageQuality(double scale, double outputQuality) {
    this.scale = scale;
    this.outputQuality = outputQuality;
  }
}
