package com.tuituidan.oss.bean;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
@Builder
public class ChunkFileRequest {
    /**
     * 文件名称
     */
    @NotNull(message = "fileName can not be empty")
    private String fileName;
    /**
     * 主文件的md5
     */
    @NotNull(message = "fileMd5 can not be empty")
    private String fileMd5;
    /**
     * 文件id
     */
    @NotNull(message = "fileId can not be empty")
    private String fileId;
    /**
     * 当前分片数
     */
    @NotNull(message = "chunk can not be empty")
    private Integer chunk;
    /**
     * 总分片数
     */
    @NotNull(message = "chunkCount can not be empty")
    private Integer chunkCount;
//    /**
//     * 分片文件
//     */
//    private MultipartFile file;

}
