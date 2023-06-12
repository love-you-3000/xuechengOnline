package com.xuecheng.media;

import com.xuecheng.media.entity.MediaProcess;
import com.xuecheng.media.mapper.MediaProcessMapper;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @className: BigFileTest
 * @author: 朱江
 * @description:
 * @date: 2023/6/8
 **/

@SpringBootTest
public class BigFileTest {


    @Autowired
    MediaProcessMapper processMapper;
    // 分开测试
    public static final MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();
    @Test
    public void testChunk() throws IOException {
        File file = new File("D:\\Study\\JAVA\\简历项目\\xuecheng\\upload\\chunk.mp4");
        String chunkFilePath = "D:\\Study\\JAVA\\简历项目\\xuecheng\\upload\\chunk\\";
        int chunkSize = 1;
        int chunkNum = (int) ((file.length() + chunkSize - 1)/chunkSize);
        byte[] buffer = new byte[1];
        RandomAccessFile raf_r = new RandomAccessFile(file, "r");
        for (int i=0;i<chunkNum;i++){
            File chunkFile = new File(chunkFilePath + i);
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while (((len=raf_r.read(buffer))!=-1)){
                raf_rw.write(buffer,0,len);
                if(chunkFile.length()>=chunkSize){
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();
    }

    // 合并测试
    @Test
    public void testMerge() throws IOException {
        File chunkFilePath = new File("D:\\Study\\JAVA\\简历项目\\xuecheng\\upload\\chunk\\");
        File sourceFile = new File("D:\\Study\\JAVA\\简历项目\\xuecheng\\upload\\chunk.mp4");
        File mergeFile = new File("D:\\Study\\JAVA\\简历项目\\xuecheng\\upload\\merge.mp4");
        byte[] buffer = new byte[1];
        int len = 0;
        FileOutputStream outputStream = new FileOutputStream(mergeFile);
        File[] files = chunkFilePath.listFiles();
        List<File> list = Arrays.stream(files).sorted(Comparator.comparingInt(f -> Integer.parseInt(f.getName()))).collect(Collectors.toList());
        for (File file : list) {
            System.out.println(file.getName());
        }
        for (File file : list) {
            FileInputStream inputStream = new FileInputStream(file);
            while ((len = inputStream.read(buffer)) !=-1){
                outputStream.write(buffer,0,len);
            }
            inputStream.close();
        }
        outputStream.close();
        String sourceMD5 = DigestUtils.md5Hex(Files.newInputStream(sourceFile.toPath()));
        String mergeMD5= DigestUtils.md5Hex(Files.newInputStream(mergeFile.toPath()));
        if(sourceMD5.equals(mergeMD5))
            System.out.println("合并成功！");
        else System.out.println("合并失败！");
    }


    @Test
    public void testMinioUploadChunk(){
        String chunkFilePath = "D:\\Study\\JAVA\\简历项目\\xuecheng\\upload\\chunk\\";
        File chunkFolder = new File(chunkFilePath);
        File[] files = chunkFolder.listFiles();
        List<File> list = Arrays.stream(files).sorted(Comparator.comparingInt(f -> Integer.parseInt(f.getName()))).collect(Collectors.toList());
        //将分块文件上传至minio
        for (int i = 0; i < files.length; i++) {
            try {
                UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder().bucket("testbucket").object("chunk/" + i).filename(list.get(i).getAbsolutePath()).build();
                minioClient.uploadObject(uploadObjectArgs);
                System.out.println("上传分块成功"+i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testMinioMerge() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<ComposeSource> sourceList = Stream.iterate(0, i -> i + 1).limit(26).map(i -> ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build()).collect(Collectors.toList());
        minioClient.composeObject(ComposeObjectArgs.builder().bucket("testbucket").sources(sourceList).object("merge01.mp4").build());
    }

    @Test
    public void testMapper()
    {
        List<MediaProcess> mediaProcessList = processMapper.selectByShardIndex(1, 0, 2);
        System.out.println(mediaProcessList);
    }
}
