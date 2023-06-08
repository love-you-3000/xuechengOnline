package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;

/**
 * @className: MinioTest
 * @author: 朱江
 * @description:
 * @date: 2023/6/8
 **/

@SpringBootTest
public class MinioTest {
    public static final MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void uploadFiles() {
        try {
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket("testbucket").build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("testbucket").build());
            } else {
                System.out.println("Bucket 'testBucket' already exists.");
            }

            //根据扩展名取出mimeType
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
            String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
            if (extensionMatch != null) {
                mimeType = extensionMatch.getMimeType();
            }

            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket") // 确定桶
                    .object("test/01/1-1导学.mp4")//添加子目录
                    .filename("D:\\Study\\JAVA\\资料\\华夏代驾资料\\课程视频\\第1章课程介绍（磨刀不费砍柴工）\\1-1导学.mp4")
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }

    @Test
    public void DeleteFiles() {
        try {

            RemoveObjectArgs removeBucketArgs = RemoveObjectArgs.builder().bucket("testbucket")
                    .object("1-1导学.mp4").build();

            minioClient.removeObject(removeBucketArgs);
            System.out.println("删除成功！");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");

        }
    }

    @Test
    public void GetFiles() {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("test/01/1-1导学.mp4").build();
        try (
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream("D:\\Study\\JAVA\\简历项目\\xuecheng\\upload\\1-1导学.mp4");
        ) {
            IOUtils.copy(inputStream, outputStream);
            //校验文件的完整性对文件的内容进行md5
            // TODO md5值不一样
            String source_md5 = DigestUtils.md5Hex(inputStream);
            FileInputStream fileInputStream = new FileInputStream("D:\\Study\\JAVA\\简历项目\\xuecheng\\upload\\1-1导学.mp4");
            String local_md5 = DigestUtils.md5Hex(fileInputStream);
            if(source_md5.equals(local_md5)){
                System.out.println("下载成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
