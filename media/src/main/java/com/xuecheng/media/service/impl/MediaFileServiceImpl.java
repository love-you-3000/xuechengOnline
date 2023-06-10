package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XuechengException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.dto.QueryMediaParamsDto;
import com.xuecheng.media.dto.UploadFileResultDto;
import com.xuecheng.media.entity.MediaFiles;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    // 存储普通文件
    @Value("${minio.bucket.files}")
    String fileBucket;

    // 存储视频文件
    @Value("${minio.bucket.videoFiles}")
    String videoBucket;

    @Autowired
    MediaFileService selfProxy;


    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, MediaFiles mediaFiles, String localFilePath) {
        UploadFileResultDto dto = new UploadFileResultDto();
        File file = new File(localFilePath); // 用户上传文件的本地备份
        if (!file.exists()) XuechengException.cast("文件不存在！");
        String fileMd5 = getFileMd5(file);
        MediaFiles hasExist = mediaFilesMapper.selectById(fileMd5);
        if (hasExist != null) {

            BeanUtils.copyProperties(hasExist, dto);
            return dto;
        }
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String uploadPath = getDefaultFoldPath() + fileMd5 + extension;
        boolean result = uploadFileToMinio(fileBucket, extension, uploadPath, localFilePath);
        if (!result)
            XuechengException.cast("上传文件失败！");
        mediaFiles.setFileSize(file.length());
        selfProxy.addMediaFilesToDb(companyId, mediaFiles, fileMd5, uploadPath);
        BeanUtils.copyProperties(mediaFiles, dto);
        return dto;
    }

    @Transactional
    @Override
    public void addMediaFilesToDb(Long companyId, MediaFiles mediaFiles, String fileMd5, String uploadPath) {
        mediaFiles.setId(fileMd5);
        mediaFiles.setFileId(fileMd5);
        mediaFiles.setCompanyId(companyId);
        mediaFiles.setBucket(fileBucket);
        mediaFiles.setUrl("/" + mediaFiles.getBucket() + "/" + uploadPath);
        mediaFiles.setFilePath(uploadPath);
        mediaFiles.setCreateDate(LocalDateTime.now());
        mediaFiles.setAuditStatus("002003");
        mediaFiles.setStatus("1");
        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert < 0) {
            log.error("保存文件信息到数据库失败,{}", mediaFiles);
            XuechengException.cast("保存文件信息失败");
        }
        log.debug("保存文件信息到数据库成功,{}", mediaFiles);
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) return RestResponse.success(false);
        String bucket = mediaFiles.getBucket();
        String filePath = mediaFiles.getFilePath();
        try {
            GetObjectResponse stream = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(filePath).build());
            if (stream == null) return RestResponse.success(false);
        } catch (Exception e) {
            e.printStackTrace();
            return RestResponse.success(false);
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //得到分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        //文件流
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(videoBucket)
                            .object(chunkFilePath)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return RestResponse.success(false);
        }
        //分块未存在
        return RestResponse.success(true);

    }

    @Override
    public RestResponse<Boolean> uploadchunk(String fileMd5, int chunk, String localChunkFilePath) {
        String uploadPath = getChunkFileFolderPath(fileMd5);
        String uploadName = uploadPath + chunk;
        boolean b = uploadFileToMinio(videoBucket, null, uploadName, localChunkFilePath);
        if (!b) return RestResponse.validfail(false, "上传文件失败！");
        return RestResponse.success(true);
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getDefaultFoldPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date()).replace("-", "/") + "/";
    }

    // 上传文件到MINIO
    public boolean uploadFileToMinio(String bucket, String extension, String uploadName, String localName) {
        try {
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            } else {
                System.out.println("Bucket '" + bucket + "' already exists.");
            }
            String mimeType = getMimeType(extension);
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucket) // 确定桶
                    .object(uploadName)
                    .filename(localName)
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                    .build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错:bucket:{}, uploadNane:{}, localName:{}, 错误信息:{}", bucket, uploadName, localName, e.getMessage());
        }
        return false;
    }

    private String getMimeType(String extension) {
        if (extension == null)
            extension = "";
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/chunk/";
    }
}
