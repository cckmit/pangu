package com.pangu.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * author weihongwei
 * date 2018/1/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName("details")
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class InstanceDetails {

    //描述信息
    private String description;

    // 给客户端提供的地址
    private String addressForClient;

    // 当前承载的数量
    private int times;
}
