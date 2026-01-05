package cn.simbok.iris.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/stream")
public class MjpegStreamController {
    
    @GetMapping(value = "/mjpeg", produces = "multipart/x-mixed-replace;boundary=frame")
    public void streamMjpeg(HttpServletResponse response) throws Exception {
        response.setContentType("multipart/x-mixed-replace;boundary=frame");
        OutputStream outputStream = response.getOutputStream();
        
        try {
            while (true) {
                byte[] frameData = new byte[0];
                
                if (frameData.length > 0) {
                    outputStream.write(("--frame\r\n").getBytes());
                    outputStream.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
                    outputStream.write(frameData);
                    outputStream.write("\r\n".getBytes());
                    outputStream.flush();
                }
                
                Thread.sleep(33);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputStream.close();
        }
    }
}
