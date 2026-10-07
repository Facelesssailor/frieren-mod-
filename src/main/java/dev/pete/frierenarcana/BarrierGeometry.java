package dev.pete.frierenarcana;

public final class BarrierGeometry {
    private BarrierGeometry() {
    }

    public static boolean inside(double x, double y, double z, double radius) {
        return x * x + y * y + z * z < radius * radius;
    }

    public static double firstHit(double x, double y, double z, double dx, double dy, double dz, double radius) {
        double a = dx * dx + dy * dy + dz * dz;
        if (a < 1.0E-12) {
            return Double.POSITIVE_INFINITY;
        } else {
            double b = 2.0 * (x * dx + y * dy + z * dz);
            double c = x * x + y * y + z * z - radius * radius;
            double discriminant = b * b - 4.0 * a * c;
            if (discriminant < 0.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                double root = Math.sqrt(discriminant);
                double t1 = (-b - root) / (2.0 * a);
                double t2 = (-b + root) / (2.0 * a);
                if (t1 >= 1.0E-7 && t1 <= 1.0) {
                    return t1;
                } else {
                    return t2 >= 1.0E-7 && t2 <= 1.0 ? t2 : Double.POSITIVE_INFINITY;
                }
            }
        }
    }
}
