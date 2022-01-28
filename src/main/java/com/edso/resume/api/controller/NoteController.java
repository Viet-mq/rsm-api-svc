package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.NoteProfileEntity;
import com.edso.resume.api.domain.request.CreateNoteProfileRequest;
import com.edso.resume.api.domain.request.DeleteNoteProfileRequest;
import com.edso.resume.api.domain.request.UpdateNoteProfileRequest;
import com.edso.resume.api.service.NoteService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/note")
public class NoteController extends BaseController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/list")
    public BaseResponse findAllNoteProfile(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "idProfile") String idProfile,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllNoteProfile u: {}, idProfile: {}, page: {}, size: {}", headerInfo, idProfile, page, size);
        GetArrayResponse<NoteProfileEntity> resp = noteService.findAllNote(headerInfo, idProfile, page, size);
        logger.info("<=findAllNoteProfile u: {}, idProfile: {}, page: {}, size: {}, resp: {}", headerInfo, idProfile, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createNoteProfile(@RequestHeader Map<String, String> headers, @ModelAttribute CreateNoteProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createNoteProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = noteService.createNoteProfile(request);
            }
        }
        logger.info("<=createNoteProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateNoteProfile(@RequestHeader Map<String, String> headers, @ModelAttribute UpdateNoteProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateNoteProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = noteService.updateNoteProfile(request);
            }
        }
        logger.info("<=updateNoteProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteNoteProfile(@RequestHeader Map<String, String> headers, @RequestBody DeleteNoteProfileRequest request) {
        logger.info("=>deleteNoteProfile req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = noteService.deleteNoteProfile(request);
            }
        }
        logger.info("<=deleteNoteProfile req: {}, resp: {}", request, response);
        return response;
    }

    @Value("${note.serverpath}")
    private String serverPath;

    @GetMapping("/export/file/{file-name}")
    public ResponseEntity<Resource> exportFile(@PathVariable("file-name") String fileName) {
        logger.info("export file");
        File file = new File(serverPath + fileName);
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (Throwable e) {
            logger.error("Exception: ", e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + fileName);

        return ResponseEntity.ok()
                .contentLength(file.length())
                .headers(headers)
                .body(resource);
    }

}
