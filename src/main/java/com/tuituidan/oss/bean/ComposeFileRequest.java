package com.tuituidan.oss.bean;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author yanlong
 */
@Setter
@Getter
@Builder
public class ComposeFileRequest {
    @NotNull(message = "文件名称不能为空")
    private String fileName;
    @NotNull(message = "文件md5不能为空")
    private String fileMd5;
    @NotNull(message = "文件Id不能为空")
    private String fileId;
    @NotNull(message = "文件分片数不能为空")
    private int chunkCount;
}
