package org.tommap.springai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tommap.springai.model.request.SpeechModelRequest;
import org.tommap.springai.model.response.ApiResponse;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3;
import static org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.Voice.NOVA;
import static org.springframework.ai.openai.api.OpenAiAudioApi.TranscriptResponseFormat.VTT;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/*
    - transcription -> feed LLM an audio file & get back a text response -> use OpenAiAudioTranscriptionModel
        + https://platform.openai.com/docs/guides/audio

    - text to speech -> give LLM a message & get back a talking byte array -> use SpeechModel
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AudioController {
    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
    private final SpeechModel speechModel; //OpenAiAudioSpeechModel implementation

    @PostMapping(value = "/transcribe", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> transcribe(
        @RequestPart("file") MultipartFile file
    ) {
        Resource resource = file.getResource();
        String response = openAiAudioTranscriptionModel.call(resource);

        return ResponseEntity.ok(ApiResponse.ok("audio-to-text response generated successfully", response));
    }

    @PostMapping(value = "/transcribe-options", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> transcribeOptions(
        @RequestPart("file") MultipartFile file
    ) {
        Resource resource = file.getResource();
        AudioTranscriptionOptions audioTranscriptionOptions = OpenAiAudioTranscriptionOptions.builder() //https://docs.spring.io/spring-ai/reference/api/audio/transcriptions/openai-transcriptions.html
                .language("en") //language of the input audio
                .prompt("Talking about spring AI") //give a clue to the genAI model on what's the topic is being discussed in the audio
                .temperature(0.7f) //between 0 and 1 -> higher values make the output more random | lower values make it more focused and deterministic
                .responseFormat(VTT) //format transcription output
                .build();

        AudioTranscriptionPrompt audioTranscriptionPrompt = new AudioTranscriptionPrompt(resource, audioTranscriptionOptions);
        String response = openAiAudioTranscriptionModel.call(audioTranscriptionPrompt)
                .getResult()
                .getOutput();

        //can use transcription as context for ChatClient to generate more accurate response

        return ResponseEntity.ok(ApiResponse.ok("audio-to-text options response generated successfully", response));
    }

    @PostMapping("/speech")
    public ResponseEntity<ApiResponse<String>> speech(
        @RequestBody @Valid SpeechModelRequest request
    ) throws IOException {
        byte[] audioBytes = speechModel.call(request.getMessage());
        Path path = Paths.get("speech-output.mp3");
        Files.write(path, audioBytes);

        var response = String.format("MP3 saved successfully to %s", path.toAbsolutePath());
        return ResponseEntity.ok(ApiResponse.ok("text-to-audio response generated successfully", response));
    }

    @PostMapping("/speech-options")
    public ResponseEntity<ApiResponse<String>> speechOptions(
        @RequestBody @Valid SpeechModelRequest request
    ) throws IOException {
        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
//                .model("tts-1")
                .voice(NOVA)
                .responseFormat(MP3)
                .speed(2.0f)
                .build();

        byte[] audioBytes = speechModel.call(new SpeechPrompt(request.getMessage(), options))
                .getResult()
                .getOutput();

        Path path = Paths.get("speech-output-options.mp3");
        Files.write(path, audioBytes);

        var response = String.format("MP3 saved successfully to %s", path.toAbsolutePath());
        return ResponseEntity.ok(ApiResponse.ok("text-to-audio options response generated successfully", response));
    }
}
