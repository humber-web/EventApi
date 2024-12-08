package com.example.event_ticketing.utils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class QRCodeUtil {

    public static byte[] generateQRCodeImage(String qrCodeText) throws WriterException, IOException {
        // Set up QR code parameters
        Map<EncodeHintType, Object> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.MARGIN, 1); // Margin around the QR code

        // Create BitMatrix
        BitMatrix matrix = new MultiFormatWriter().encode(qrCodeText, BarcodeFormat.QR_CODE, 200, 200, hintMap);

        // Convert BitMatrix to BufferedImage
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE); // Set the background color
        graphics.fillRect(0, 0, 200, 200);
        graphics.setColor(Color.BLACK); // Set the color for the QR code

        // Fill the image with the QR code bits
        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 200; x++) {
                if (matrix.get(x, y)) {
                    image.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }

        // Convert BufferedImage to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "PNG", baos);
        baos.flush();
        return baos.toByteArray(); // Return as byte array
    }
}
