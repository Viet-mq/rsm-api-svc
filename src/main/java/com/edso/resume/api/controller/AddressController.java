package com.edso.resume.api.controller;

import com.edso.resume.api.domain.entities.AddressEntity;
import com.edso.resume.api.domain.request.CreateAddressRequest;
import com.edso.resume.api.domain.request.DeleteAddressRequest;
import com.edso.resume.api.domain.request.UpdateAddressRequest;
import com.edso.resume.api.service.AddressService;
import com.edso.resume.lib.entities.HeaderInfo;
import com.edso.resume.lib.response.BaseResponse;
import com.edso.resume.lib.response.GetArrayResponse;
import com.edso.resume.lib.utils.ParseHeaderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/address")
public class AddressController extends BaseController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/list")
    public BaseResponse findAllAddress(
            @RequestHeader Map<String, String> headers,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>findAllAddress u: {}, name: {}, page: {}, size: {}", headerInfo, name, page, size);
        GetArrayResponse<AddressEntity> resp = addressService.findAll(headerInfo, name, page, size);
        logger.info("<=findAllAddress u: {}, name: {}, page: {}, size: {}, resp: {}", headerInfo, name, page, size, resp.info());
        return resp;
    }

    @PostMapping("/create")
    public BaseResponse createAddress(@RequestHeader Map<String, String> headers, @RequestBody CreateAddressRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>createAddress u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = addressService.createAddress(request);
            }
        }
        logger.info("<=createAddress u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/update")
    public BaseResponse updateAddress(@RequestHeader Map<String, String> headers, @RequestBody UpdateAddressRequest request) {
        BaseResponse response = new BaseResponse();
        HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
        logger.info("=>updateAddress u: {}, req: {}", headerInfo, request);
        if (request == null) {
            response.setResult(-1, "Vui lòng điền đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                request.setInfo(headerInfo);
                response = addressService.updateAddress(request);
            }
        }
        logger.info("<=updateAddress u: {}, req: {}, resp: {}", headerInfo, request, response);
        return response;
    }

    @PostMapping("/delete")
    public BaseResponse deleteAddress(@RequestHeader Map<String, String> headers, @RequestBody DeleteAddressRequest request) {
        logger.info("=>deleteAddress req: {}", request);
        BaseResponse response = new BaseResponse();
        if (request == null) {
            response.setResult(-1, "Vui lòng nhập đầy đủ thông tin");
        } else {
            response = request.validate();
            if (response == null) {
                HeaderInfo headerInfo = ParseHeaderUtil.build(headers);
                request.setInfo(headerInfo);
                response = addressService.deleteAddress(request);
            }
        }
        logger.info("<=deleteAddress req: {}, resp: {}", request, response);
        return response;
    }
}
