package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.ProfileDetailEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.api.service.ProfileService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.response.GetResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(value = "reject", required = false) String reject,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "follow", required = false) String follow,
            @RequestParam(value = "blackList", required = false) String blackList,
            @RequestParam(value = "talentPool", required = false) String talentPool,
            @RequestParam(value = "job", required = false) String job,
            @RequestParam(value = "levelJob", required = false) String levelJob,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "recruitment", required = false) String recruitment,
            @RequestParam(value = "calendar", required = false) String calendar,
            @RequestParam(value = "statusCV", required = false) String statusCV,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "pic", required = false) String pic,
            @RequestParam(value = "hrRef", required = false) String hrRef,
            @RequestParam(value = "fromCreateAt", required = false) Long fromCreateAt,
            @RequestParam(value = "from", required = false) Long from,
            @RequestParam(value = "toCreateAt", required = false) Long toCreateAt,
            @RequestParam(value = "to", required = false) Long to,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllProfile u: {}, reject: {}, fullName: {}, follow: {}, blackList: {}, talentPool: {}, job: {}, levelJob: {}, department: {}, recruitment: {}, calendar: {}, statusCV: {}, key: {}, tag: {}, pic: {}, hrRef: {}, from:{}, to:{}, fromCreateAt:{}, toCreateAt:{}, page: {}, size: {}", headerInfo, reject, fullName, follow, blackList, talentPool, job, levelJob, department, recruitment, calendar, statusCV, key, tag, pic, hrRef, from, to, fromCreateAt, toCreateAt, page, size);
        GetArrayResponse<ProfileEntity> resp = profileService.findAll(headerInfo, reject, fullName, follow, blackList, talentPool, job, levelJob, department, recruitment, calendar, statusCV, key, tag, pic, hrRef, from, to, fromCreateAt, toCreateAt, page, size);
        logger.info("<=findAllProfile u: {}, reject: {}, fullName: {}, follow: {}, blackList: {}, talentPool: {}, job: {}, levelJob: {}, department: {}, recruitment: {}, calendar: {}, statusCV: {}, key: {}, tag: {}, pic: {}, hrRef: {}, from:{}, to:{}, fromCreateAt:{}, toCreateAt:{}, page: {}, size: {}, resp: {}", headerInfo, reject, fullName, follow, blackList, talentPool, job, levelJob, department, recruitment, calendar, statusCV, key, tag, pic, hrRef, from, to, fromCreateAt, toCreateAt, page, size, resp.info());
        return resp;
    }

    @GetMapping("/detail")
    public BaseResponse findOneProfile(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "idProfile") String idProfile) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findOneProfile u: {}, idProfile: {}", headerInfo, idProfile);
        GetResponse<ProfileDetailEntity> resp = profileService.findOne(headerInfo, idProfile);
        logger.info("<=findOneProfile u: {}, idProfile: {}, resp: {}", headerInfo, idProfile, resp.info());
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

    @PostMapping("/update-detail")
    public BaseResponse updateDetailProfile(@RequestHeader Map<String, String> headers, @RequestBody UpdateDetailProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateDetailProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.updateDetailProfile(request);
            }
        }
        logger.info("<=updateDetailProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/reject")
    public BaseResponse updateRejectProfile(@RequestHeader Map<String, String> headers,
                                            @ModelAttribute UpdateRejectProfileRequest request,
                                            @ModelAttribute CandidateRequest candidate,
                                            @ModelAttribute PresenterRequest presenter) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateRejectProfile u: {}, req: {}, candidate: {}, presenter: {}", headerInfo, request, candidate, presenter);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.updateRejectProfile(request, candidate, presenter);
            }
        }
        logger.info("<=updateRejectProfile u: {}, req: {}, resp: {}, candidate: {}, presenter: {}", headerInfo, request, response, candidate, presenter);
        return response;
    }

    @PostMapping("/talentpool")
    public BaseResponse updateTalentPoolProfile(@RequestHeader Map<String, String> headers, @RequestBody UpdateTalentPoolProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateTalentPoolProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.updateTalentPoolProfile(request);
            }
        }
        logger.info("<=updateTalentPoolProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete-talentpool")
    public BaseResponse deleteTalentPoolProfile(@RequestHeader Map<String, String> headers, @RequestBody DeleteTalentPoolProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>deleteTalentPoolProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.deleteTalentPoolProfile(request);
            }
        }
        logger.info("<=deleteTalentPoolProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/blacklist")
    public BaseResponse updateBlackListProfile(@RequestHeader Map<String, String> headers, @RequestBody UpdateBlackListProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateBlackListProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.updateBlackListProfile(request);
            }
        }
        logger.info("<=updateBlackListProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete-blacklist")
    public BaseResponse deleteBlackListProfile(@RequestHeader Map<String, String> headers, @RequestBody DeleteBlackListProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>deleteBlackListProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.deleteBlackListProfile(request);
            }
        }
        logger.info("<=deleteBlackListProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/isold")
    public void isOld(@RequestParam String id) {
        logger.info("=>isOld id: {}", id);
        profileService.isOld(id);
        logger.info("<=isOld id: {}", id);
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


    @PostMapping("/merge")
    public BaseResponse mergeProfile(@RequestHeader Map<String, String> headers, @RequestBody MergeProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>mergeProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.mergeDuplicateProfile(request);
            }
        }
        logger.info("<=mergeProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/add-follower")
    public BaseResponse addFollower(@RequestHeader Map<String, String> headers, @RequestBody AddFollowerRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>addFollower u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.addFollower(request);
            }
        }
        logger.info("<=addFollower u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete-follower")
    public BaseResponse deleteFollower(@RequestHeader Map<String, String> headers, @RequestBody DeleteFollowerRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>deleteFollower u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.deleteFollower(request);
            }
        }
        logger.info("<=deleteFollower u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/add-tags")
    public BaseResponse addTags(@RequestHeader Map<String, String> headers, @RequestBody AddTagsProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>addTags u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.addTags(request);
            }
        }
        logger.info("<=addTags u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete-tag")
    public BaseResponse deleteTagProfile(@RequestHeader Map<String, String> headers, @RequestBody DeleteTagProfileRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>deleteTagProfile u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = profileService.deleteTag(request);
            }
        }
        logger.info("<=deleteTagProfile u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

}
