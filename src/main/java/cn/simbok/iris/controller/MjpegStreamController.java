package cn.simbok.iris.controller;

import cn.simbok.iris.service.IrisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/stream")
@CrossOrigin(origins = "*")
public class MjpegStreamController {
    
    private static final Logger log = LoggerFactory.getLogger(MjpegStreamController.class);
    
    @Autowired
    private IrisService irisService;
    
    /**
     * MJPEG流预览接口
     */
    @GetMapping(value = "/preview", produces = "multipart/x-mixed-replace; boundary=frame")
    public void streamPreview(HttpServletResponse response) {
        response.setContentType("multipart/x-mixed-replace; boundary=frame");
        
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            while (irisService.isInitialized()) {
                byte[] frameData = irisService.getLatestPreviewFrame();
                int width = irisService.getPreviewWidth();
                int height = irisService.getPreviewHeight();
                
                if (frameData != null && width > 0 && height > 0) {
                    // 将BGRA字节数组转换为JPEG
                    byte[] jpegData = convertBgraToJpeg(frameData, width, height);
                    
                    if (jpegData != null) {
                        // 写入MJPEG帧
                        outputStream.write(("--frame\r\n").getBytes());
                        outputStream.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
                        outputStream.write(jpegData);
                        outputStream.write("\r\n".getBytes());
                        outputStream.flush();
                    }
                }
                
                // 控制帧率，约30fps
                Thread.sleep(33);
            }
        } catch (Exception e) {
            log.error("Stream error", e);
        }
    }
    
    /**
     * 将BGRA格式转换为JPEG
     */
    private byte[] convertBgraToJpeg(byte[] bgraData, int width, int height) {
        try {
            // 创建BufferedImage
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            System.arraycopy(bgraData, 0, imageData, 0, Math.min(bgraData.length, imageData.length));
            
            // 转换为JPEG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "JPEG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Convert image error", e);
            return null;
        }
    }
}

