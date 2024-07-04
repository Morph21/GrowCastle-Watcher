package com.morph.general;

import com.morph.jna.utils.WindowInfo;
import com.morph.models.CheckImageResult;
import com.morph.push.SendPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainService {
    private final static Logger logger = LoggerFactory.getLogger(MainService.class);
    static {
        instance = new MainService();
    }

    private static final MainService instance;
    private int warnAfterFailed;
    private final BigDecimal blackRatioRequired = new BigDecimal("0.70").setScale(2, RoundingMode.HALF_UP);

    private MainService() {
        warnAfterFailed = PropertiesService.instance().getInt("warn.after.failed", 3);
        if (warnAfterFailed <= 0) {
            warnAfterFailed = 3;
            logger.info("warn.after.failed needs to be higher than 0, setting to default value=3");
        } else if (warnAfterFailed > 5) {
            logger.info("warn.after.failed needs to be lower than 6, setting to default value=3");
            warnAfterFailed = 3;
        }

    }

    private Map<String, String> checksum = new HashMap<>();
    private int sameCounter = 0;

    public static MainService instance() {
        return instance;
    }

    public void run(List<String> windowNames) {
        SendPushService.instance().reconnectIfPossible();

        var windows = FindWindowService.instance().fetchRelevantWindows(windowNames);
        for (WindowInfo window : windows) {
            var screenShotResult = takeScreenShot(window);
            if (screenShotResult.isImagesAreTheSame()) {
                continue;
            }

            if (screenShotResult.getBlackRatio().compareTo(blackRatioRequired) < 0) {
                logger.info(window.getTitle() + ": Black mode wasn't detected, checking for failure skipped");
                continue;
            }

            logger.info("Found " + screenShotResult.getFoundInImages().stream().filter(x -> x).count() + " losses");

            long count = screenShotResult.getFoundInImages().stream().filter(x -> x).count();
            if (count >= warnAfterFailed && screenShotResult.getFoundInImages().get(4)) {
                logger.info("Sending notification");
                boolean sendImageOnFail = PropertiesService.instance().getBoolean("send.image.on.fail", false);
                if (sendImageOnFail) {
                    File file = new File(window.getTitle() + "_screen.png");
                    SendPushService.instance().sendPushWithImage(file, window, "Wave failed " + count + " times");
                } else {
                    SendPushService.instance().sendPush("Wave failed " + count + " times", window);
                }
            }
        }
    }

    public CheckImageResult takeScreenShot(WindowInfo w) {
        try {
            var result = new CheckImageResult();
            var red = 50;
            var green = 10;
            var blue = 10;

            BufferedImage createScreenCapture = new Robot().createScreenCapture(new Rectangle(w.getRect().left(), w.getRect().top(), w.getRect().right() - w.getRect().left(), w.getRect().bottom() - w.getRect().top()));
            ImageIO.write(createScreenCapture, "png", new File(w.getTitle() + "_screen.png"));
            var firstCut = sliceImageIntoParts(createScreenCapture, 4, 4);


            ImageIO.write(firstCut[15], "png", new File(w.getTitle() + "_screen_first_cut.png"));

            var secondCut = sliceImageIntoParts(firstCut[15], 7, 2);

            var firstPhoto = secondCut[5];
            var secondPhoto = secondCut[7];
            var thirdPhoto = secondCut[9];
            var fourthPhoto = secondCut[11];
            var fithPhoto = secondCut[13];

            String checksum = md5Hash(fithPhoto) + md5Hash(fourthPhoto) + md5Hash(thirdPhoto);

            result.setBlackRatio(FindRedService.instance().findBlackPixelsRatio(createScreenCapture));

            if (this.checksum.containsKey(w.getTitle()) && this.checksum.get(w.getTitle()).equals(checksum)) {
                result.setImagesAreTheSame(true);
                sameCounter++;
                return result;
            } else {
                this.checksum.put(w.getTitle(), checksum);
                sameCounter = 0;
            }

            result.add(FindRedService.instance().findColorInPicture(firstPhoto, red, green, blue));
            result.add(FindRedService.instance().findColorInPicture(secondPhoto, red, green, blue));
            result.add(FindRedService.instance().findColorInPicture(thirdPhoto, red, green, blue));
            result.add(FindRedService.instance().findColorInPicture(fourthPhoto, red, green, blue));
            result.add(FindRedService.instance().findColorInPicture(fithPhoto, red, green, blue));

            return result;


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.info("Taking screenshot of window named " + w.getTitle() + " failed");
            return null;
        }
    }

    public BufferedImage[] sliceImageIntoParts(BufferedImage image, int rows, int columns) {
        // initializing array to hold subimages
        BufferedImage imgs[] = new BufferedImage[16];

        // Equally dividing original image into subimages
        int subimage_Width = image.getWidth() / columns;
        int subimage_Height = image.getHeight() / rows;

        int current_img = 0;

        // iterating over rows and columns for each sub-image
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                // Creating sub image
                imgs[current_img] = new BufferedImage(subimage_Width, subimage_Height, image.getType());
                Graphics2D img_creator = imgs[current_img].createGraphics();

                // coordinates of source image
                int src_first_x = subimage_Width * j;
                int src_first_y = subimage_Height * i;

                // coordinates of sub-image
                int dst_corner_x = subimage_Width * j + subimage_Width;
                int dst_corner_y = subimage_Height * i + subimage_Height;

                img_creator.drawImage(image, 0, 0, subimage_Width, subimage_Height, src_first_x, src_first_y, dst_corner_x, dst_corner_y, null);
                current_img++;
            }
        }

        return imgs;

    }

    private String md5Hash(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            byte[] data = baos.toByteArray();
            byte[] hash = MessageDigest.getInstance("MD5").digest(data);
            String checksum = new BigInteger(1, hash).toString(16);
            return checksum;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
