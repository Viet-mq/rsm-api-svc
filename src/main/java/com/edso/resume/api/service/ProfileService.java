package com.edso.resume.api.service;

import com.edso.resume.api.domain.entities.ProfileDetailEntity;
import com.edso.resume.api.domain.entities.ProfileEntity;
import com.edso.resume.api.domain.request.*;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.response.GetResponse;

public interface ProfileService {

    GetArrayResponse<ProfileEntity> findAll(HeaderInfo info, String reject, String fullName, String follow, String blackList, String talentPool, String job, String levelJob, String department, String recruitment, String calendar, String statusCV, String key, String tag, String pic, String hrRef, Long from, Long to, Long fromCreateAt, Long toCreateAt, Integer page, Integer size);

    GetResponse<ProfileDetailEntity> findOne(HeaderInfo info, String idProfile);

    BaseResponse createProfile(CreateProfileRequest request);

    BaseResponse updateProfile(UpdateProfileRequest request);

    BaseResponse updateDetailProfile(UpdateDetailProfileRequest request);

    BaseResponse deleteProfile(DeleteProfileRequest request);

    BaseResponse updateStatusProfile(UpdateStatusProfileRequest request);

    BaseResponse updateRejectProfile(UpdateRejectProfileRequest request, CandidateRequest candidate, PresenterRequest presenter);

    BaseResponse updateTalentPoolProfile(UpdateTalentPoolProfileRequest request);

    BaseResponse deleteTalentPoolProfile(DeleteTalentPoolProfileRequest request);

    BaseResponse updateBlackListProfile(UpdateBlackListProfileRequest request);

    BaseResponse deleteBlackListProfile(DeleteBlackListProfileRequest request);

    void isOld(String id);

    BaseResponse mergeDuplicateProfile(MergeProfileRequest request);

    BaseResponse addFollower(AddFollowerRequest request);

    BaseResponse deleteFollower(DeleteFollowerRequest request);

    BaseResponse addTags(AddTagsProfileRequest request);

    BaseResponse deleteTag(DeleteTagProfileRequest request);

}
