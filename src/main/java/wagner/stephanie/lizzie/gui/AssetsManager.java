package wagner.stephanie.lizzie.gui;

import org.jetbrains.annotations.Contract;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AssetsManager {
    private static final AssetsManager assetsManagerSingleton = new AssetsManager();

    private Map<String, BufferedImage> imageAssetsCache;

    private AssetsManager() {
        imageAssetsCache = new HashMap<>();
    }

    @Contract(pure = true)
    public static AssetsManager getAssetsManager() {
        return assetsManagerSingleton;
    }

    public BufferedImage getImageAsset(String assetPath) throws IOException {
        IOException exception = null;
        BufferedImage resultAsset = imageAssetsCache.get(assetPath);
        if (resultAsset != null) {
            return resultAsset;
        }

        File assetFile = new File(assetPath);
        if (assetFile.exists()) {
            try {
                resultAsset = ImageIO.read(assetFile);
                imageAssetsCache.put(assetPath, resultAsset);
                return resultAsset;
            } catch (IOException e) {
                exception = e;
            }
        }

        if (!assetFile.isAbsolute() && !assetFile.toString().contains("..")) {
            String resoucePath;
            if (assetPath.startsWith("/")) {
                resoucePath = assetPath;
            } else {
                resoucePath = "/" + assetPath;
            }
            try (InputStream inputStream = AssetsManager.class.getResourceAsStream(resoucePath)) {
                resultAsset = ImageIO.read(inputStream);
                imageAssetsCache.put(assetPath, resultAsset);
                return resultAsset;
            } catch (IOException e) {
                exception = e;
            }
        }


        if (exception != null) {
            throw exception;
        }

        return null;
    }

    public synchronized BufferedImage getImageAssetSync(String assetPath) throws IOException {
        return getImageAsset(assetPath);
    }
}
