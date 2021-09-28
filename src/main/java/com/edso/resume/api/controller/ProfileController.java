package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.CalendarEntity;
import com.edso.resume.api.domain.entities.HistoryEntity;
import com.edso.resume.api.domain.entities.NoteProfileEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.service.ProfileService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayCalendarReponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController extends BaseController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/list")
    public BaseResponse findAllProfile(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "idProfile", required = false) String idProfile,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllProfile u: {}, fullName: {}, idProfile: {}, page: {}, size: {}", headerInfo, fullName, idProfile, page, size);
        GetArrayResponse<ProfileEntity> resp = profileService.findAll(headerInfo, fullName, idProfile, page, size);
        logger.info("<=findAllProfile u: {}, fullName: {}, idProfile: {}, page: {}, size: {}, resp: {}", headerInfo, idProfile, fullName, page, size, resp.info());
        return resp;
    }

    @GetMapping("/calendars")
    public BaseResponse findAllCalendar(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "idProfile", required = false) String idProfile) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllCalendar u: {}, idProfile: {}", headerInfo, idProfile);
        GetArrayCalendarReponse<CalendarEntity> resp = profileService.findAllCalendar(headerInfo, idProfile);
        logger.info("<=findAllCalendar u: {}, idProfile: {}, resp: {}", headerInfo, idProfile, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createProfile(@RequestHeader Map<String, String> headers, @RequestBody CreateProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.createProfile(request);
            }
        }
        logger.info("<=createProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/create-calendar")
    public BaseResponse createCalendarProfile(@RequestHeader Map<String, String> headers, @RequestBody CreateCalendarProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createCalendarProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.createCalendarProfile(request);
            }
        }
        logger.info("<=createCalendarProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateProfile(@RequestHeader Map<String, String> headers, @RequestBody UpdateProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.updateProfile(request);
            }
        }
        logger.info("<=updateProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update-calendar")
    public BaseResponse updateCalendarProfile(@RequestHeader Map<String, String> headers, @RequestBody UpdateCalendarProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateCalendarProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.updateCalendarProfile(request);
            }
        }
        logger.info("<=updateCalendarProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteProfile(@RequestHeader Map<String, String> headers, @RequestBody DeleteProfileRequest request) {
        logger.info("=>deleteProfile req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = profileService.deleteProfile(request);
            }
        }
        logger.info("<=deleteProfile req: {}, resp: {}", request, response);
        return response;
    }

    @PostMapping("/delete-calendar")
    public BaseResponse deleteCalendarProfile(@RequestHeader Map<String, String> headers, @RequestBody DeleteCalendarProfileRequest request) {
        logger.info("=>deleteCalendarProfile req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = profileService.deleteCalendarProfile(request);
            }
        }
        logger.info("<=deleteCalendarProfile req: {}, resp: {}", request, response);
        return response;
    }

    @GetMapping("/export")
    public byte[] exportExcel(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "fullName", required = false) String fullName) throws  IOException {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>exportExcel u: {}, name: {}", headerInfo, fullName);
        byte[] resp = profileService. exportExcel(headerInfo, fullName);
        logger.info("<=exportExcel u: {}, name: {}, resp: {}", headerInfo, fullName, resp);
        return resp;
    }

    @PostMapping("/update-status")
    public BaseResponse updateStatusProfile(@RequestHeader Map<String, String> headers, @RequestBody UpdateStatusProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateStatusProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.updateStatusProfile(request);
            }
        }
        logger.info("<=updateStatusProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @GetMapping("/history")
    public BaseResponse findAllHistoryProfile(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "idProfile", required = false) String idProfile,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllHistoryProfile u: {}, idProfile: {}, page: {}, size: {}", headerInfo, idProfile, page, size);
        GetArrayResponse<HistoryEntity> resp = profileService.findAllHistory(headerInfo, idProfile, page, size);
        logger.info("<=findAllHistoryProfile u: {}, idProfile: {}, page: {}, size: {}, resp: {}", headerInfo, idProfile, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create-note")
    public BaseResponse createNoteProfile(@RequestHeader Map<String, String> headers, @RequestBody CreateNoteProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createNoteProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.createNoteProfile(request);
            }
        }
        logger.info("<=createNoteProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @GetMapping("/notes")
    public BaseResponse findAllNoteProfile(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "idProfile", required = false) String idProfile,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllNoteProfile u: {}, idProfile: {}, page: {}, size: {}", headerInfo, idProfile, page, size);
        GetArrayResponse<NoteProfileEntity> resp = profileService.findAllNote(headerInfo, idProfile, page, size);
        logger.info("<=findAllNoteProfile u: {}, idProfile: {}, page: {}, size: {}, resp: {}", headerInfo, idProfile, page, size, resp.info());
        return resp;
    }

}
