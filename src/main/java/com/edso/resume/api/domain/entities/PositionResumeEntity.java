package com.edso.resume.api.domain.entities;

import lombok.Data;

import java.util.List;

@Data
public class PositionResumeEntity {
    private String idTuyenDung;
    private String tinTuyenDung;
    private List<PositionResumeSourceInfo> sourceInfos;
    private int total;
}
