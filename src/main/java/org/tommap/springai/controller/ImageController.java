package org.tommap.springai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tommap.springai.model.request.ImageModelRequest;
import org.tommap.springai.model.response.ApiResponse;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImageController {
    private final ImageModel imageModel;

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<String>> image(
        @RequestBody @Valid ImageModelRequest request
    ) {
        var imagePrompt = new ImagePrompt(request.getMessage());
        var imageResponse = imageModel.call(imagePrompt);
        var response  = imageResponse.getResult().getOutput().getUrl();

        return ResponseEntity.ok(ApiResponse.ok("image response generated successfully", response));
    }

    @PostMapping("/image-options")
    public ResponseEntity<ApiResponse<String>> imageOptions(
        @RequestBody @Valid ImageModelRequest request
    ) {
        var openAiImageOptions = OpenAiImageOptions.builder()
                .N(1) //number of images to generate
                .quality("hd")
                .style("natural")
                .height(1024)
                .width(1024)
                .responseFormat("url")
                .build();

        var imagePrompt = new ImagePrompt(request.getMessage(), openAiImageOptions);
        var imageResponse = imageModel.call(imagePrompt);
        var response = imageResponse.getResult().getOutput().getUrl();

        return ResponseEntity.ok(ApiResponse.ok("image options response generated successfully", response));
    }
}
