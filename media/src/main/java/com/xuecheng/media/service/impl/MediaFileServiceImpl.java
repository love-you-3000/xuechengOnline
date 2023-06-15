package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XuechengException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.utils.StringUtil;
import com.xuecheng.media.dto.QueryMediaParamsDto;
import com.xuecheng.media.dto.UploadFileResultDto;
import com.xuecheng.media.entity.MediaFiles;
import com.xuecheng.media.entity.MediaProcess;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

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
        queryWrapper.like(MediaFiles::getFilename, queryMediaParamsDto.getFilename());
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
    public UploadFileResultDto uploadFile(Long companyId, MediaFiles mediaFiles, String localFilePath, String objectName) {
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
        String uploadPath;
        if (StringUtil.isEmpty(objectName)) {
            uploadPath = getDefaultFoldPath() + fileMd5 + extension;
        } else uploadPath = objectName;
        boolean result = uploadFileToMinio(fileBucket, extension, uploadPath, localFilePath);
        if (!result)
            XuechengException.cast("上传文件失败！");
        mediaFiles.setFileSize(file.length());
        selfProxy.addMediaFilesToDb(companyId, mediaFiles, fileBucket, fileMd5, uploadPath);
        BeanUtils.copyProperties(mediaFiles, dto);
        return dto;
    }

    @Transactional
    @Override
    public void addMediaFilesToDb(Long companyId, MediaFiles mediaFiles, String bucket, String fileMd5, String uploadPath) {
        mediaFiles.setId(fileMd5);
        mediaFiles.setFileId(fileMd5);
        mediaFiles.setCompanyId(companyId);
        mediaFiles.setBucket(bucket);
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
        addWaitingTask(mediaFiles);
        // 视频转码需要和文件上传成功同步，也就是要保持事务
        // 判断该文件是否需要处理
        // 如果需要处理，向MediaProcess中插入数据

    }

    // 将文件加入待处理数据表
    private void addWaitingTask(MediaFiles mediaFiles) {
        // 判断该文件是否需要处理
        // 如果需要处理，向MediaProcess中插入数据
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        if (mimeType.equals("video/x-msvideo")) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setStatus("1"); // 1为待处理
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }
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


    /**
     * @Author: 朱江
     * @Description: 检查分块是否存在，false不存在，true存在
     * @Date: 11:39 2023/6/13
     **/
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        RestResponse<Boolean> booleanRestResponse = checkFile(fileMd5);
        if (booleanRestResponse.getResult()) return booleanRestResponse;
        //得到分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        InputStream fileInputStream;
        //文件流
        try {
            fileInputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(videoBucket)
                            .object(chunkFilePath)
                            .build());
            if (fileInputStream != null) {
                //分块已存在
                return RestResponse.success(true);
            }
        } catch (Exception ignored) {
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        String uploadPath = getChunkFileFolderPath(fileMd5);
        String uploadName = uploadPath + chunk;
        boolean b = uploadFileToMinio(videoBucket, null, uploadName, localChunkFilePath);
        if (!b) return RestResponse.validfail(false, "上传文件失败！");
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, MediaFiles mediaFiles) {
        // 避免重复上传
        RestResponse<Boolean> booleanRestResponse = checkFile(fileMd5);
        if (booleanRestResponse.getResult()) return booleanRestResponse;
        // 找到分块文件，用minio API合并
        String chunkPath = getChunkFileFolderPath(fileMd5);
        List<ComposeSource> sourceList = Stream.iterate(0, i -> i + 1)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(videoBucket)
                        .object(chunkPath + i)
                        .build()).collect(Collectors.toList());
        String fileName = mediaFiles.getFilename();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String uploadPath = fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extension;
        try {
            minioClient.composeObject(ComposeObjectArgs.builder().bucket(videoBucket).sources(sourceList).object(uploadPath).build());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错,bucket:{}, objectName:{},错误信息:{}", videoBucket, uploadPath, e.getMessage());
            return RestResponse.validfail(false, "合并文件出错");
        }
        File downloadedFile = downloadFileFromMinIO(videoBucket, uploadPath);
        if (downloadedFile == null) {
            log.debug("下载合并后文件失败,mergeFilePath:{}", uploadPath);
            return RestResponse.validfail(false, "下载合并后文件失败。");
        }
        try (InputStream newFileInputStream = Files.newInputStream(downloadedFile.toPath())) {
            //minio上文件的md5值
            String md5Hex = DigestUtils.md5Hex(newFileInputStream);
            //比较md5值，不一致则说明文件不完整
            if (!fileMd5.equals(md5Hex)) {
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取文件md5值出错,bucket:{}, objectName:{},错误信息:{}", videoBucket, uploadPath, e.getMessage());
            return RestResponse.validfail(false, "获取文件md5值出错");
        }

        mediaFiles.setFileSize(downloadedFile.length());
        mediaFiles.setTags("视频文件");
        mediaFiles.setFileType("001002");
        // 文件入库
        selfProxy.addMediaFilesToDb(companyId, mediaFiles, videoBucket, fileMd5, uploadPath);
        //todo 删除分块
        clearChunkFiles(chunkPath, chunkTotal);
        return RestResponse.success(true);
    }

    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(videoBucket).objects(deleteObjects).build());
            // 执行到这里还没有删除，需要遍历一遍！！！！！
            results.forEach(r -> {
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清楚分块文件失败,objectname:{}", deleteError.objectName(), e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清楚分块文件失败,chunkFileFolderPath:{}", chunkFileFolderPath, e);
        }
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
    @Override
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

    /**
     * 从minio下载文件
     *
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    @Override
    public File downloadFileFromMinIO(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
