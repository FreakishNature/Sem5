package com.controllers;

import com.model.UploadFileResponse;
import com.response.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@RestController
public class FileController {
    private String UPLOADED_FOLDER = "./src/main/resources/images/";

    @Value("${server.url}")
    private String SERVER_URL;

    @GetMapping("/file/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) throws IOException {
        File file = new File(UPLOADED_FOLDER + fileName);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }


    // 3.1.1 Single file upload
    @PostMapping("/file")
    // If not @RestController, uncomment this
    //@ResponseBody
    public ResponseEntity<?> uploadFile(
            @RequestParam MultipartFile file) {

        if (file.isEmpty()) {
            return new ResponseEntity<>("please select a file!", HttpStatus.BAD_REQUEST);
        }
        String fileName;
        try {
           fileName = saveUploadedFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse("Bad file format"),HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new UploadFileResponse(fileName,file.getContentType(),file.getSize()), new HttpHeaders(), HttpStatus.OK);

    }
/*
    // 3.1.2 Multiple file upload
    @PostMapping("/api/upload/multi")
    public ResponseEntity<?> uploadFileMulti(
            @RequestParam("extraField") String extraField,
            @RequestParam("files") MultipartFile[] uploadfiles) {

        logger.debug("Multiple file upload!");

        // Get file name
        String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename())
                .filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));

        if (StringUtils.isEmpty(uploadedFileName)) {
            return new ResponseEntity("please select a file!", HttpStatus.OK);
        }

        try {

            saveUploadedFiles(Arrays.asList(uploadfiles));

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity("Successfully uploaded - "
                + uploadedFileName, HttpStatus.OK);

    }
*/
    //save file
    private String saveUploadedFile(MultipartFile file) throws IOException {
            if (file.isEmpty()) {
                return "";
            }

            byte[] bytes = file.getBytes();

            String creatingPath = new Date().getTime() + ".jpg";
            Path path = Paths.get(UPLOADED_FOLDER + creatingPath);
            Files.write(path, bytes);

        return SERVER_URL + "/file/" + creatingPath;
    }
//    @PostMapping("/uploadMultipleFiles")
//    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
//        return Arrays.asList(files)
//                .stream()
//                .map(file -> uploadFile(file))
//                .collect(Collectors.toList());
//    }
}
