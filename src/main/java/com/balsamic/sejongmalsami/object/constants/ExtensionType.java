package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ExtensionType {
    JPEG(".jpeg"),
    JPG(".jpg"),
    PNG(".png"),
    MP4(".mp4"),
    ZIP(".zip"),
    AVI(".avi"),
    MOV(".mov"),
    MP3(".mp3"),
    WAV(".wav"),
    AAC(".aac");

    private final String description;

    // 유효한 확장자인지 확인하는 메서드
    public static boolean isValidExtension(String extension) {
        return Arrays.stream(ExtensionType.values())
                .map(ExtensionType::getDescription)
                .collect(Collectors.toList())
                .contains(extension.toLowerCase());
    }
}