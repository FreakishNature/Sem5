package com.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UploadFileResponse {
    private String imageUri;
    private String fileType;
    private long size;
}