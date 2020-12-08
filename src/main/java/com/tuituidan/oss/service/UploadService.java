package com.tuituidan.oss.service;

import com.tuituidan.oss.bean.*;
import com.tuituidan.oss.exception.ImageHostException;
import com.tuituidan.oss.util.CompressUtils;
import com.tuituidan.oss.util.FileTypeUtils;
import com.tuituidan.oss.util.HashMapUtils;
import com.tuituidan.oss.util.StringExtUtils;
import io.minio.ComposeSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UploadService.
 *
 * @author zhujunhan
 * @version 1.0
 * @date 2020/10/14
 */
@Slf4j
@Service
public class UploadService {

    @Resource
    private MinioService minioService;

    @Resource
    private ElasticsearchService elasticsearchService;

    @Resource
    private FileCacheService fileCacheService;

    @Resource
    private ChunkFileCacheService chunkFileCacheService;

    @Value("${chunk.bucket:chunk}")
    private String chunkBucket;

    /**
     * base64数据上传.
     *
     * @param inputStream inputStream
     * @return String
     */
    public String uploadBase64(InputStream inputStream) {
        String base64Str = StringExtUtils.streamToString(inputStream);
        Pair<String, String> base64 = StringExtUtils.getBase64Info(base64Str);
        if (null == base64) {
            throw ImageHostException.builder().tip("base64数据格式错误")
                    .error("base64数据格式错误-{}", base64Str).build();
        }
        return upload(new FileInfo().setName("image." + base64.getLeft()).setBase64(base64.getRight()));
    }

    /**
     * 文件上传.
     *
     * @param fileInfo fileInfo
     * @return String
     */
    public String upload(FileInfo fileInfo) {
        String ext = FilenameUtils.getExtension(fileInfo.getName()).toLowerCase();
        if (FileTypeUtils.isNotSupport(ext)) {
            throw ImageHostException.builder().tip("很抱歉，暂不支持上传该种类型的文件！").build();
        }
        fileInfo.setExt(ext);
        fileInfo.setId(StringExtUtils.getId());
        byte[] sourceData = getDataFromFileInfo(fileInfo);
        // 是否压缩+文件md5来标识是否是同一个文件
        String md5 = StringExtUtils.format("{}-{}", DigestUtils.md5Hex(sourceData), fileInfo.isCompress());
        String objName = fileCacheService.get(md5);
        if (StringUtils.isNotBlank(objName)) {
            // 已存在直接返回，不重复上传
            return minioService.getObjectUrl(objName);
        }
        if (fileInfo.isCompress()) {
            sourceData = CompressUtils.compress(fileInfo.getExt(), sourceData);
        }
        objName = StringExtUtils.getObjectName(fileInfo.getId(), fileInfo.getExt());
        // minio支持给文件打标签（老一些版本的minio不支持，但也不会报错），将这些信息也一并写入
        Map<String, String> tags = HashMapUtils.newFixQuarterSize();
        tags.put("info", fileInfo.getTags());
        tags.put("compress", String.valueOf(fileInfo.isCompress()));
        tags.put("md5", md5);
        try (InputStream inputStream = new ByteArrayInputStream(sourceData)) {
            minioService.putObject(objName, tags, inputStream);
        } catch (Exception ex) {
            throw ImageHostException.builder().tip("文件上传失败")
                    .error("上传文件到 MinIO 失败，文件名-{}", fileInfo.getName(), ex).build();
        }
        elasticsearchService.saveFileDoc(objName, md5, fileInfo);
        fileCacheService.put(md5, objName);
        return minioService.getObjectUrl(objName);
    }

    private byte[] getDataFromFileInfo(FileInfo fileInfo) {
        try {
            // 如果是base64的，转换base64
            if (StringUtils.isNotBlank(fileInfo.getBase64())) {
                return Base64.getDecoder().decode(fileInfo.getBase64());
            }
            return fileInfo.getFile().getBytes();
        } catch (Exception ex) {
            throw ImageHostException.builder().tip("文件上传失败").error("获取文件数据失败！", ex).build();
        }
    }

    /**
     * 获取分片上传的 url
     * 1. 从缓存中查询文件是否已经上传
     * 2. 从minio中查询已上传分片
     * 3. 获取待上传分片的上传url
     * 4.
     * @param request
     * @return
     */
    public ChunkFileResult getChunkUploadFileUrls(ChunkFileRequest request){

        String fileMd5 = request.getFileMd5();

        ChunkFileResult chunkFile = chunkFileCacheService.get(fileMd5);
        // 已上传成功，直接返回，秒传
        if(chunkFile != null && chunkFile.getStatus() == ChunkFileResult.COMPLETE_UPLOAD){
            return chunkFile;
        }

        chunkFile = new ChunkFileResult();

        // 从minio中查询已上传的分片
        List<String> objectNames = minioService.listObject(chunkBucket, fileMd5);
        // 已上传分片大于0
        if(objectNames.size() > 0){
            // 全部上传
            if(objectNames.size() == request.getChunkCount()){
                chunkFile.setStatus(ChunkFileResult.COMPLETE_UPLOAD);
                return chunkFile;
            }
            // 获取未上传分片的上传url
            Set<Integer> chunkSet = new HashSet<>();
            for (String objectUrl : objectNames) {
                int chunk = Integer.parseInt(objectUrl.substring(objectUrl.lastIndexOf("/") + 1, objectUrl.lastIndexOf(".part")));
                chunkSet.add(chunk);
            }
            Map<Integer,String> chunkFileUrls = new HashMap<>();
            for (int i = 0; i < request.getChunkCount(); i++) {
                if(!chunkSet.contains(i)){
                    String presignUrl = minioService.presignUrl(chunkBucket, fileMd5 + "/" + i + ".part");
                    chunkFileUrls.put(i,presignUrl);
                }
            }
            chunkFile.setStatus(ChunkFileResult.PART_UPLOAD);
            chunkFile.setFileChunkUrls(chunkFileUrls);
            return chunkFile;
        }

        // 当前文件未上传
        Integer chunkCount = request.getChunkCount();
        Map<Integer,String> chunkFileUrls = new HashMap<>(chunkCount);
        for (int i = 0; i < chunkCount; i++) {
            String presignUrl = minioService.presignUrl(chunkBucket, fileMd5 + "/" + i + ".part");
            chunkFileUrls.put(i,presignUrl);
        }
        chunkFile.setChunkCount(chunkCount);
        chunkFile.setFileMd5(fileMd5);
        chunkFile.setFileChunkUrls(chunkFileUrls);
        chunkFile.setStatus(ChunkFileResult.NO_UPLOAD);
        return chunkFile;
    }

    /**
     * 合并文件
     * 1. 根据md5从minio中获取已上传的分片names
     * 2. 合并文件
     * 3. 获取下载url
     * 4. 存入缓存
     * 5. 返回文件下载路径
     * @param request
     * @return
     */
    public UploadResult composeFile(ComposeFileRequest request){
        // 根据md5从minio中获取已上传的分片names
        List<String> objectNames = minioService.listObject(chunkBucket, request.getFileMd5());

        List<ComposeSource> sourceList = objectNames.stream().map(objectName -> ComposeSource
                .builder()
                .bucket(chunkBucket)
                .object(objectName)
                .build()).collect(Collectors.toList());
        // 生成文件名称
        String fileType = request.getFileName().substring(request.getFileName().lastIndexOf(".") + 1);
        String objName = StringExtUtils.getObjectName(request.getFileId(), fileType);
        // 合并文件
        minioService.composeObject(objName,sourceList);
        // 获取文件下载url
        String downLoadUrl = minioService.getObjectUrl(objName);
        // 存入缓存
        ChunkFileResult chunkFileResult = new ChunkFileResult();
        chunkFileResult.setFileMd5(request.getFileMd5());
        chunkFileResult.setStatus(ChunkFileResult.COMPLETE_UPLOAD);
        chunkFileResult.setDownloadUrl(downLoadUrl);
        chunkFileResult.setChunkCount(request.getChunkCount());
        chunkFileCacheService.put(request.getFileMd5(),chunkFileResult);
        // 返回下载地址
        return UploadResult.builder()
                .id(request.getFileId())
                .url(downLoadUrl)
                .build();
    }
}
