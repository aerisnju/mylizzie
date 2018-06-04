package featurecat.lizzie.gui;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.api.list.FixedSizeList;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
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
    private Map<FixedSizeList<String>, BufferedImage> imageAssetsFallbackCache;

    private AssetsManager() {
        imageAssetsCache = new HashMap<>();
        imageAssetsFallbackCache = new HashMap<>();
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

    private BufferedImage getImageAssetNoExcept(String assetPath) {
        try {
            return getImageAsset(assetPath);
        } catch (Exception e) {
            return null;
        }
    }

    public BufferedImage getImageAssetFallThrough(String... assetPaths) throws IOException {
        if (ArrayUtils.isEmpty(assetPaths)) {
            return null;
        }

        FixedSizeList<String> request = ArrayAdapter.adapt(assetPaths);
        BufferedImage resultAsset = imageAssetsFallbackCache.get(request);
        if (resultAsset == null) {
            resultAsset = getImageAssetNoExcept(request.getFirst());
            if (resultAsset == null) {
                resultAsset = getImageAssetFallThrough(ArrayUtils.subarray(assetPaths, 1, assetPaths.length));
                if (resultAsset != null) {
                    imageAssetsFallbackCache.put(request, resultAsset);
                }
                return resultAsset;
            } else {
                imageAssetsFallbackCache.put(request, resultAsset);
                return resultAsset;
            }
        } else {
            return resultAsset;
        }
    }
}
