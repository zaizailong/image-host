import com.tuituidan.oss.ImageHostApplication;
import com.tuituidan.oss.bean.ChunkFileRequest;
import com.tuituidan.oss.bean.ChunkFileResult;
import com.tuituidan.oss.bean.ComposeFileRequest;
import com.tuituidan.oss.bean.UploadResult;
import com.tuituidan.oss.service.UploadService;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ImageHostApplication.class)
public class ChunkFileTest {

    @Autowired
    private UploadService uploadService;
    @Test
    public void chunkFileUpload() {
        ChunkFileResult chunkUploadFileUrls = uploadService.getChunkUploadFileUrls(ChunkFileRequest
                .builder()
                .chunk(1)
                .chunkCount(2)
                .fileId("2348")
                .fileMd5("2348")
                .fileName("chunk.txt")
                .build());

        Map<Integer, String> urls = chunkUploadFileUrls.getFileChunkUrls();

        for (Map.Entry<Integer, String> entry : urls.entrySet()) {
            Integer key = entry.getKey();
            try(FileInputStream fis = getFile("part"+key+".txt")){
                System.out.println("chunk " + entry.getKey() + " uploadurl:" +entry.getValue());
                HttpFileHelper.httpSend(entry.getValue(),fis);
            }
            catch (Exception e){
                System.out.println("upload chunk file error ");
                System.out.println(e);
            }
        }

        UploadResult result = uploadService.composeFile(ComposeFileRequest
                .builder()
                .chunkCount(2)
                .fileId("2348")
                .fileMd5("2348")
                .fileName("chunk.txt")
                .build());

        System.out.println("download url:" + result.getUrl());

    }

    @SneakyThrows
    public FileInputStream getFile(String fileName){
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        return fis;
    }
}
