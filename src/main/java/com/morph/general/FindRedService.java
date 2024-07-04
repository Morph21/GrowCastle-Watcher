package com.morph.general;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FindRedService {
    static {
        instance = new FindRedService();
    }

    private static FindRedService instance;

    private FindRedService() {
    }

    public static FindRedService instance() {
        return instance;
    }

    public boolean findColorInPicture(BufferedImage img, int r, int g, int b) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {

                int c = img.getRGB(x, y);

                int red = (c & 0x00ff0000) >> 16;
                int green = (c & 0x0000ff00) >> 8;
                int blue = c & 0x000000ff;

                if (red >= r && green <= g && blue <= b) {
                    return true;
                }


            }
        }
        return false;
    }

    public BigDecimal findBlackPixelsRatio(BufferedImage img) {
        int countOfPixels = 0;
        int countOfBlackPixels = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {

                int c = img.getRGB(x, y);

                int red = (c & 0x00ff0000) >> 16;
                int green = (c & 0x0000ff00) >> 8;
                int blue = c & 0x000000ff;

                countOfPixels++;

                if (red == 0 && green == 0 && blue == 0) {
                    countOfBlackPixels++;
                }
            }
        }
        BigDecimal allPixels = new BigDecimal(countOfPixels);
        BigDecimal blackPixels = new BigDecimal(countOfBlackPixels);

        return blackPixels.divide(allPixels, 2, RoundingMode.HALF_UP);
    }

    /**
     * For processing image before using ocr on it
     * Currently not used
     * @param img
     * @return
     */
    public BufferedImage turnToBlackAndWhite(BufferedImage img) {
        int increase = 4;
        BufferedImage newImage = new BufferedImage(img.getWidth() * increase, img.getHeight() * increase, BufferedImage.TYPE_INT_BGR);
        Graphics2D g2d = newImage.createGraphics();
        Color black = new Color(0, 0, 0);
        Color white = new Color(255, 255, 255);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {

                int c = img.getRGB(x, y);

                int red = (c & 0x00ff0000) >> 16;
                int green = (c & 0x0000ff00) >> 8;
                int blue = c & 0x000000ff;


//                try { // This section will save single pixel as image so you can debug what was found few lines below
//                    BufferedImage testPixel = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
//                    int rgbPixel = red << 16 | green << 8 | blue;
//                    testPixel.setRGB(0, 0, rgbPixel);
//                    ImageIO.write(testPixel, "png", new File("testPixel.png"));
//                } catch (Exception e) {
//                    logger.error(e.getMessage(), e);
//                }

                if (red == 0 && green == 0 && blue == 0) {
                    g2d.setColor(white);
                    fillRect(g2d, x, y, increase);
                } else if (red == 0 && blue == 0 && green > 20) {
                    g2d.setColor(black);
                    fillRect(g2d, x, y, increase);
                } else {
                    g2d.setColor(white);
                    fillRect(g2d, x, y, increase);
                }


            }
        }
        g2d.drawImage(newImage, 0, 0, null);
//        try {
//            ImageIO.write(newImage, "png", new File(UUID.randomUUID().toString() + ".png"));
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
        return newImage;
    }

    private void fillRect(Graphics2D g2d, int x, int y, int increase) {
        g2d.fillRect(x * increase, y * increase, increase, increase);
    }
}
;