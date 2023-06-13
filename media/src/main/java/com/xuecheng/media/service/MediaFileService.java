package com.xuecheng.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.dto.QueryMediaParamsDto;
import com.xuecheng.media.dto.UploadFileResultDto;
import com.xuecheng.media.entity.MediaFiles;

import java.io.File;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService extends IService<MediaFiles> {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    UploadFileResultDto uploadFile(Long companyId, MediaFiles mediaFiles, String localFilePath);

    void addMediaFilesToDb(Long companyId, MediaFiles mediaFiles, String bucket, String fileMd5, String uploadPath);

    RestResponse<Boolean> checkFile(String fileMd5);

    RestResponse<Boolean> checkChunk(String fileMd5, int chunk);

    RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, String localChunkFilePath);

    RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, MediaFiles mediaFiles);

    File downloadFileFromMinIO(String bucket, String objectName);

    boolean uploadFileToMinio(String bucket, String extension, String uploadName, String localName);
}
