package cn.simbok.iris.controller;

import cn.simbok.iris.service.IrisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/stream")
public class MjpegStreamController {

    private static final Logger log = LoggerFactory.getLogger(MjpegStreamController.class);

    @Autowired
    private IrisService irisService;

    @GetMapping(value = "/mjpeg", produces = "multipart/x-mixed-replace;boundary=frame")
    public void streamMjpeg(HttpServletResponse response) {
        log.info("MJPEG stream started");
        response.setContentType("multipart/x-mixed-replace;boundary=frame");

        try (OutputStream outputStream = response.getOutputStream()) {
            int frameCount = 0;
            int noFrameCount = 0;

            while (true) {
                byte[] frameData = irisService.getLatestPreviewFrame();
                int width = irisService.getPreviewWidth();
                int height = irisService.getPreviewHeight();

                if (frameData == null || width == 0 || height == 0) {
                    noFrameCount++;
                    Thread.sleep(100);
                    continue;
                }

                frameCount++;
                /*if (frameCount == 1 || frameCount % 30 == 0) {
                    log.info("Streaming frame #{}: {}x{}, size: {}", frameCount, width, height, frameData.length);
                }*/

                if (frameData != null && width > 0 && height > 0) {
                    try {
                        // Convert BGRA to RGB BufferedImage
                        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                        byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

                        // BGRA to BGR conversion
                        for (int i = 0; i < width * height; i++) {
                            imageData[i * 3] = frameData[i * 4];     // B
                            imageData[i * 3 + 1] = frameData[i * 4 + 1]; // G
                            imageData[i * 3 + 2] = frameData[i * 4 + 2]; // R
                        }

                        // Rotate 180 degrees
                        AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
                        tx.translate(-width, -height);
                        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                        image = op.filter(image, null);

                        // Convert to JPEG
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, "jpg", baos);
                        byte[] jpegData = baos.toByteArray();

                        // Send MJPEG frame
                        outputStream.write(("--frame\r\n").getBytes());
                        outputStream.write(("Content-Type: image/jpeg\r\n").getBytes());
                        outputStream.write(("Content-Length: " + jpegData.length + "\r\n\r\n").getBytes());
                        outputStream.write(jpegData);
                        outputStream.write("\r\n".getBytes());
                        outputStream.flush();
                    } catch (Exception e) {
                        log.error("Error converting frame", e);
                    }
                }

                Thread.sleep(33); // ~30 FPS
            }
        } catch (Exception e) {
            log.error("MJPEG stream error", e);
        }
    }
}
