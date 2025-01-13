package club.thom.tem.util;

public class ColourConversion {

    public static double[] rgbToXyz(int rgbInt) {
        double[] rgb = new double[]{
                ((rgbInt >> 16) & 0xFF) / 255.0,
                ((rgbInt >> 8) & 0xFF) / 255.0,
                (rgbInt & 0xFF) / 255.0
        };

        for (int i = 0; i < rgb.length; i++) {
            double value = rgb[i];
            rgb[i] = value <= 0.04045 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
        }

        double[] xyzFactors = new double[]{0.4124, 0.3576, 0.1805, 0.2126, 0.7152, 0.0722, 0.0193, 0.1192, 0.9505};
        return new double[]{
                dotProduct(xyzFactors, 0, rgb),
                dotProduct(xyzFactors, 3, rgb),
                dotProduct(xyzFactors, 6, rgb)
        };
    }

    public static double[] xyzToLab(double x, double y, double z) {
        double refX = 0.95047, refY = 1.0, refZ = 1.08883;
        x /= refX;
        y /= refY;
        z /= refZ;

        return new double[]{
                116 * f(y) - 16,
                500 * (f(x) - f(y)),
                200 * (f(y) - f(z))
        };
    }

    public static double[] rgbIntToCielab(int rgbInt) {
        double[] xyz = rgbToXyz(rgbInt);
        return xyzToLab(xyz[0], xyz[1], xyz[2]);
    }

    private static double f(double t) {
        return t > 0.008856 ? Math.pow(t, 1.0 / 3.0) : 7.787 * t + 16.0 / 116.0;
    }

    private static double dotProduct(double[] factors, int offset, double[] rgb) {
        double sum = 0.0;
        for (int i = 0; i < 3; i++) {
            sum += factors[offset + i] * rgb[i];
        }
        return sum;
    }
}

