package com.edso.resume.api.domain.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateCrawlerRequest extends BaseAuthRequest {

    private String id;
    private String name;

//    public BaseResponse validateCreate(){
//
//    }
//
//    public BaseResponse validateUpdate(){
//
//    }

}
