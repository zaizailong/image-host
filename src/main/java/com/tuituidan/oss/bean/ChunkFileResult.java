package com.tuituidan.oss.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ChunkFileResult {


    /**
     * 分片上传状态
     *  0-未上传
     *  1-部分上传
     */
    private Integer status;

    public static final int NO_UPLOAD = 0;
    public static final int PART_UPLOAD = 1;
    public static final int COMPLETE_UPLOAD = 2;
    /**
     * 文件md5
     */
    private String fileMd5;
    /**
     * 总分片数
     */
    private Integer chunkCount;
    /**
     * 下载url
     */
    private String downloadUrl;
    /**
     * 分片上传url
     */
    private Map<Integer,String> fileChunkUrls;
}
