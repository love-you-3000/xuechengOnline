package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.dto.QueryMediaParamsDto;
import com.xuecheng.media.dto.UploadFileResultDto;
import com.xuecheng.media.entity.MediaFiles;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto  queryMediaParamsDto);

 UploadFileResultDto uploadFile(Long companyId, MediaFiles mediaFiles, String localFilePath);

 void addMediaFilesToDb(Long companyId, MediaFiles mediaFiles, String fileMd5, String uploadPath);
}
