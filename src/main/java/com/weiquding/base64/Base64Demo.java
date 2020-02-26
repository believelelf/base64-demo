package com.weiquding.base64;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Locale;

/**
 * description
 * https://stackoverflow.com/questions/5459701/how-can-i-watermark-an-image-in-java
 * http://web.archive.org/web/20080324030029/http://blog.codebeach.com/2008/02/watermarking-images-in-java-servlet.html
 * https://stackoverflow.com/questions/39140494/add-watermark-to-image
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2020/2/25
 */
public class Base64Demo {

    private static final String IMAGE_FILE_NAME = "C:\\cipher\\image\\source_image.jpg";

    private static final String IMAGE_FILE_NAME_OUTPUT = "C:\\cipher\\image\\source_image_base64_output.jpg";

    private static final String IMAGE_FILE_NAME_TEXT_WATERMARK = "C:\\cipher\\image\\source_image_text_watermark.jpg";

    private static final String IMAGE_FILE_NAME_IMAGE_WATERMARK = "C:\\cipher\\image\\source_image_image_watermark.jpg";

    private static final String TEXT_WATERMARK = "仅限于XXXX和XXXX业务办理使用";

    private static final String IMAGE_WATERMARK = "C:\\cipher\\image\\14016661.png";


    public static void main(String[] args) throws IOException {
        // 测试一： 转base64
        String base64 = convertBase64(IMAGE_FILE_NAME);
        System.out.println("图片[" + IMAGE_FILE_NAME + "]的Base64如下\n" + base64);

        // 测试二：  写base64为文件
        writeImage(base64, IMAGE_FILE_NAME_OUTPUT);

        // 测试三： 打印系统字体
        getAvailableFontFamilyNames();

        // 测试四： 增加文字水印
        writeImage(addTextWatermark(TEXT_WATERMARK, new File(IMAGE_FILE_NAME)), IMAGE_FILE_NAME_TEXT_WATERMARK);

        // 测试五： 增加图片水印
        writeImage(addImageWatermark(new File(IMAGE_WATERMARK), new File(IMAGE_FILE_NAME), PlacementPosition.MIDDLECENTER, 80), IMAGE_FILE_NAME_IMAGE_WATERMARK);


    }

    /**
     * 将图片base64字符串编码写入文件
     *
     * @param base64   base64字符串编码图片
     * @param fileName 文件名
     * @throws IOException
     */
    public static void writeImage(String base64, String fileName) throws IOException {
        writeImage(Base64.getDecoder().decode(base64), fileName);
    }

    /**
     * 将图片字节数组写入文件
     *
     * @param imageBytes 图片字节数组
     * @param fileName   文件名
     * @throws IOException
     */
    public static void writeImage(byte[] imageBytes, String fileName) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(new File(fileName))) {
            IOUtils.write(imageBytes, outputStream);
        }
    }

    /**
     * 读入文件流，转换为base64字符串
     *
     * @param fileName 文件名
     * @return base64字符串
     */
    private static String convertBase64(String fileName) {
        try (
                FileInputStream inputStream = new FileInputStream(new File(fileName));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ) {
            IOUtils.copy(inputStream, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert base64 images", e);
        }
    }

    /**
     * 获取当前系统中安装的所有字体名
     */
    public static String[] getAvailableFontFamilyNames() {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        //返回包含在此所有字体系列名称的数组， GraphicsEnvironment本地化为默认的语言环境，如返回 Locale.getDefault() 。
        String[] fontFamilyNames = graphicsEnvironment.getAvailableFontFamilyNames();
        // Locale.getDefault()
        for (String fontFamilyName : fontFamilyNames) {
            System.out.println(fontFamilyName);
        }
        // Locale.ENGLISH
        fontFamilyNames = graphicsEnvironment.getAvailableFontFamilyNames(Locale.ENGLISH);
        for (String fontFamilyName : fontFamilyNames) {
            System.out.println(fontFamilyName);
        }
        return fontFamilyNames;
    }

    /**
     * 对原图增加文字水印，并返回图片数据字节
     *
     * @param text            文字水印
     * @param sourceImageFile 原图文件
     * @return 图片数据字节
     */
    public static byte[] addTextWatermark(String text, File sourceImageFile) {
        try {
            BufferedImage sourceImage = ImageIO.read(sourceImageFile);
            Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();

            // initializes necessary graphic properties
            // Create an alpha composite of 20%
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
            g2d.setComposite(alphaChannel);
            g2d.setColor(Color.BLUE);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            //  使用华文仿宋
            g2d.setFont(new Font("STFangsong", Font.BOLD, 50));
            FontMetrics fontMetrics = g2d.getFontMetrics();
            Rectangle2D rect = fontMetrics.getStringBounds(text, g2d);

            // calculates the coordinate where the String is painted
            int centerX = (sourceImage.getWidth() - (int) rect.getWidth()) / 2;
            int centerY = (sourceImage.getHeight() - (int) rect.getHeight()) / 2;

            // paints the textual watermark
            g2d.drawString(text, centerX, centerY);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String ext = sourceImageFile.getName().substring(sourceImageFile.getName().lastIndexOf('.') + 1);
            ImageIO.write(sourceImage, ext, baos);

            g2d.dispose();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to add text watermark to image", e);
        }
    }


    /**
     * 对原图增加图片水印，并返回图片数据字节
     *
     * @param watermarkImageFile         水印图
     * @param sourceImageFile            原图
     * @param position                   起始点
     * @param watermarkSizeMaxPercentage 比例
     * @return 图片数据字节
     */
    public static byte[] addImageWatermark(File watermarkImageFile, File sourceImageFile, PlacementPosition position,
                                           double watermarkSizeMaxPercentage) {
        try {
            BufferedImage sourceImage = ImageIO.read(sourceImageFile);
            BufferedImage watermarkImage = ImageIO.read(watermarkImageFile);

            // initializes necessary graphic properties
            Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
            g2d.setComposite(alphaChannel);

            // 取水印图大小
            Pair<Double, Double> watermarkSizePair = calculateWatermarkDimensions(sourceImage, watermarkImage, watermarkSizeMaxPercentage);

            // 取起始点
            Pair<Integer, Integer> topPoint = getTopPoint(position, sourceImage.getWidth(), sourceImage.getHeight(), watermarkSizePair.getLeft().intValue(), watermarkSizePair.getRight().intValue());

            // paints the image watermark
            g2d.drawImage(resizeImage(watermarkImage,
                    watermarkSizePair.getLeft().intValue(), watermarkSizePair.getRight().intValue()), topPoint.getRight(), topPoint.getLeft(), null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String ext = sourceImageFile.getName().substring(sourceImageFile.getName().lastIndexOf('.') + 1);
            ImageIO.write(sourceImage, ext, baos);
            g2d.dispose();

            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to add image watermark to image", e);
        }
    }


    /**
     * Resizes an image to a absolute width and height (the image may not be
     * proportional)
     *
     * @param sourceImage  the original image
     * @param scaledWidth  absolute width in pixels
     * @param scaledHeight absolute height in pixels
     * @return the output image
     */
    public static BufferedImage resizeImage(BufferedImage sourceImage, int scaledWidth, int scaledHeight) {
        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, sourceImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(sourceImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return outputImage;
    }

    public static Pair<Integer, Integer> getTopPoint(PlacementPosition position, int imageWidth, int imageHeight, int watermarkWidth, int watermarkHeight) {
        int x = 0;
        int y = 0;
        if (position != null) {
            switch (position) {
                case TOPLEFT:
                    break;
                case TOPCENTER:
                    x = (imageWidth / 2) - (watermarkWidth / 2);
                    y = 0;
                    break;
                case TOPRIGHT:
                    x = imageWidth - watermarkWidth;
                    y = 0;
                    break;

                case MIDDLELEFT:
                    x = 0;
                    y = (imageHeight / 2) - (watermarkHeight / 2);
                    break;
                case MIDDLECENTER:
                    x = (imageWidth / 2) - (watermarkWidth / 2);
                    y = (imageHeight / 2) - (watermarkHeight / 2);
                    break;
                case MIDDLERIGHT:
                    x = imageWidth - watermarkWidth;
                    y = (imageHeight / 2) - (watermarkHeight / 2);
                    break;

                case BOTTOMLEFT:
                    x = 0;
                    y = imageHeight - watermarkHeight;
                    break;
                case BOTTOMCENTER:
                    x = (imageWidth / 2) - (watermarkWidth / 2);
                    y = imageHeight - watermarkHeight;
                    break;
                case BOTTOMRIGHT:
                    x = imageWidth - watermarkWidth;
                    y = imageHeight - watermarkHeight;
                    break;

                default:
                    break;
            }
        }
        return Pair.of(x, y);
    }

    /**
     * 计算水印的大小
     *
     * @param originalImage  原图
     * @param watermarkImage 水印图
     * @param maxPercentage  比例
     * @return 水印的大小
     */
    private static Pair<Double, Double> calculateWatermarkDimensions(
            BufferedImage originalImage, BufferedImage watermarkImage,
            double maxPercentage) {

        double imageWidth = originalImage.getWidth();
        double imageHeight = originalImage.getHeight();

        double maxWatermarkWidth = imageWidth / 100.0 * maxPercentage;
        double maxWatermarkHeight = imageHeight / 100.0 * maxPercentage;

        double watermarkWidth = watermarkImage.getWidth();
        double watermarkHeight = watermarkImage.getHeight();

        if (watermarkWidth > maxWatermarkWidth) {
            double aspectRatio = watermarkWidth / watermarkHeight;
            watermarkWidth = maxWatermarkWidth;
            watermarkHeight = watermarkWidth / aspectRatio;
        }

        if (watermarkHeight > maxWatermarkHeight) {
            double aspectRatio = watermarkWidth / watermarkHeight;
            watermarkHeight = maxWatermarkHeight;
            watermarkWidth = watermarkHeight / aspectRatio;
        }
        return Pair.of(watermarkHeight, watermarkWidth);
    }


    /**
     * 坐标点对
     *
     * @param <R>
     * @param <L>
     */
    public static class Pair<R, L> {
        private R right;
        private L left;

        private Pair(R right, L left) {
            this.right = right;
            this.left = left;
        }

        public static <R, L> Pair of(R right, L left) {
            return new Pair(right, left);
        }

        public R getRight() {
            return right;
        }

        public L getLeft() {
            return left;
        }
    }

    /**
     * 起点位置
     */
    public enum PlacementPosition {
        /**
         * 起点位置
         */
        TOPLEFT, TOPCENTER, TOPRIGHT, MIDDLELEFT, MIDDLECENTER, MIDDLERIGHT, BOTTOMLEFT, BOTTOMCENTER, BOTTOMRIGHT
    }
}
