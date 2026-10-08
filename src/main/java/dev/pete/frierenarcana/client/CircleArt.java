package dev.pete.frierenarcana.client;

public final class CircleArt {
    private static final double[][] STROKES = new double[][]{
        {0.0, -1.0, 0.0, 1.0},
        {-0.6, -1.0, -0.6, 1.0},
        {0.6, -1.0, 0.6, 1.0},
        {-0.6, 1.0, 0.6, 1.0},
        {-0.6, 0.0, 0.6, 0.0},
        {-0.6, -1.0, 0.6, -1.0},
        {-0.6, -1.0, 0.6, 1.0},
        {-0.6, 1.0, 0.6, -1.0},
        {0.0, 0.2, 0.6, 1.0},
        {0.0, -0.3, -0.6, -1.0},
        {-0.6, 0.5, 0.0, 1.0},
        {0.0, 1.0, 0.6, 0.5},
        {-0.3, -0.5, 0.3, -0.5},
        {0.6, 0.0, 0.6, -1.0}
    };

    private CircleArt() {
    }

    static double hash(int var0, int var1) {
        double var2 = Math.sin((double)var0 * 12.9898 + (double)var1 * 78.233) * 43758.5453;
        return var2 - Math.floor(var2);
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    private static double sstep(double var0, double var2, double var4) {
        double var6 = clamp((var4 - var0) / (var2 - var0));
        return var6 * var6 * (3.0 - 2.0 * var6);
    }

    private static double ease(double var0) {
        var0 = clamp(var0);
        return 1.0 - Math.pow(1.0 - var0, 3.0);
    }

    private static void col(SpellCircleFx.Ctx var0, float var1, float var2, float var3, float var4) {
        if (Math.abs(var2 - 0.9F) < 0.006F) {
            var2 = 0.915F;
        }

        var0.r = var1;
        var0.g = var2;
        var0.b = var3;
        var0.a = var4;
    }

    private static void ink(SpellCircleFx.Ctx var0, CircleArt.Look var1, float var2) {
        col(var0, var1.lr, var1.lg, var1.lb, var2);
    }

    private static void tint(SpellCircleFx.Ctx var0, CircleArt.Look var1, float var2) {
        col(var0, var1.gr, var1.gg, var1.gb, var2);
    }

    private static void arc(SpellCircleFx.Ctx var0, double var1, double var3, double var5, double var7) {
        int var9 = Math.max(6, (int)(Math.abs(var5 - var3) / (Math.PI * 2) * 96.0));
        var0.arc(var1, var3, var5, var7, var9);
    }

    private static void ringOn(SpellCircleFx.Ctx var0, double var1, double var3, double var5, double var7) {
        if (!(var5 <= 0.002)) {
            arc(var0, var1, var7, var7 + (Math.PI * 2) * Math.min(1.0, var5), var3);
        }
    }

    private static void band(SpellCircleFx.Ctx var0, double var1, double var3, int var5) {
        for (int var6 = 0; var6 < var5; var6++) {
            double var7 = (Math.PI * 2) * (double)var6 / (double)var5;
            double var9 = (Math.PI * 2) * (double)(var6 + 1) / (double)var5;
            var0.pt(var1 * Math.cos(var7), var1 * Math.sin(var7));
            var0.pt(var3 * Math.cos(var7), var3 * Math.sin(var7));
            var0.pt(var3 * Math.cos(var9), var3 * Math.sin(var9));
            var0.pt(var1 * Math.cos(var9), var1 * Math.sin(var9));
        }
    }

    private static void tri(SpellCircleFx.Ctx var0, double var1, double var3, double var5, double var7, double var9, double var11) {
        var0.pt(var1, var3);
        var0.pt(var5, var7);
        var0.pt(var9, var11);
        var0.pt(var9, var11);
    }

    private static void circleAt(SpellCircleFx.Ctx var0, double var1, double var3, double var5, double var7) {
        int var9 = Math.max(12, (int)(var5 * 60.0));

        for (int var10 = 0; var10 < var9; var10++) {
            double var11 = (Math.PI * 2) * (double)var10 / (double)var9;
            double var13 = (Math.PI * 2) * (double)(var10 + 1) / (double)var9;
            var0.seg(var1 + var5 * Math.cos(var11), var3 + var5 * Math.sin(var11), var1 + var5 * Math.cos(var13), var3 + var5 * Math.sin(var13), var7);
        }
    }

    private static void polar(SpellCircleFx.Ctx var0, double var1, double var3, double var5, double var7, double var9) {
        var0.seg(var1 * Math.cos(var3), var1 * Math.sin(var3), var5 * Math.cos(var7), var5 * Math.sin(var7), var9);
    }

    private static void script(SpellCircleFx.Ctx var0, double var1, double var3, int var5, double var6, double var8, int var10, double var11) {
        double var13 = (var1 + var3) * 0.5;
        double var15 = (var3 - var1) * 0.42;
        double var17 = (Math.PI * 2) * var13 / (double)var5 * 0.3;
        int var19 = (int)Math.round((double)var5 * clamp(var11));

        for (int var20 = 0; var20 < var19; var20++) {
            double var21 = var6 + (Math.PI * 2) * ((double)var20 + 0.5) / (double)var5;
            double var23 = -Math.sin(var21);
            double var25 = Math.cos(var21);
            double var27 = Math.cos(var21);
            double var29 = Math.sin(var21);
            double var31 = var13 * var27;
            double var33 = var13 * var29;
            int var35 = (int)(hash(var20 + var10 * 101, 3) * 997.0);
            int var36 = 2 + (int)(hash(var35, 1) * 3.0);
            boolean var37 = false;

            for (int var38 = 0; var38 < var36; var38++) {
                int var39 = (int)(hash(var35, var38 + 7) * (double)STROKES.length) % STROKES.length;
                if (var38 == 0) {
                    var39 = hash(var35, 5) < 0.5 ? 0 : (hash(var35, 6) < 0.5 ? 1 : 2);
                }

                if (var39 <= 2) {
                    if (var37 && var38 > 0) {
                        var39 = 3 + (int)(hash(var35, var38 + 9) * 11.0);
                    }

                    var37 = true;
                }

                double[] var40 = STROKES[Math.min(var39, STROKES.length - 1)];
                var0.seg(
                    var31 + var23 * var40[0] * var17 + var27 * var40[1] * var15,
                    var33 + var25 * var40[0] * var17 + var29 * var40[1] * var15,
                    var31 + var23 * var40[2] * var17 + var27 * var40[3] * var15,
                    var33 + var25 * var40[2] * var17 + var29 * var40[3] * var15,
                    var8
                );
            }

            if (hash(var35, 13) < 0.3) {
                circleAt(var0, var31 + var27 * var15 * 1.25, var33 + var29 * var15 * 1.25, var15 * 0.18, var8 * 0.8);
            }
        }
    }

    private static void ticks(SpellCircleFx.Ctx var0, double var1, double var3, int var5, double var6, double var8) {
        for (int var10 = 0; var10 < var5; var10++) {
            double var11 = var6 + (Math.PI * 2) * (double)var10 / (double)var5;
            double var13 = var10 % 4 == 0 ? var3 : var1 + (var3 - var1) * 0.5;
            polar(var0, var1, var11, var13, var11, var8);
        }
    }

    public static void draw(SpellCircleFx.Ctx var0, CircleArt.Look var1, double var2, double var4, double var6, float var8, int var9) {
        if (!(var8 <= 0.004F) && !(var2 <= 0.01)) {
            double var10 = clamp(var6);
            double var13 = 0.012 + 0.016 * var2;
            double var15 = var4 * 0.22;
            double var17 = -var4 * 0.35;
            if (var1.design == CircleArt.Design.GOLEM) {
                golem(var0, var1, var2, var4, var10, var8);
            } else {
                if (var10 < 1.0) {
                    var0.glow = 0.0;
                    ink(var0, var1, (float)((1.0 - var10) * 0.8) * var8);
                    var0.ring(var2 * (1.0 + 0.35 * ease(var10)), var13 * (1.6 - var10));
                }

                var0.glow = 0.0;
                tint(var0, var1, 0.07F * var8 * (float)sstep(0.0, 0.6, var10));
                band(var0, 0.0, var2 * 1.02, 48);
                tint(var0, var1, 0.1F * var8 * (float)sstep(0.2, 0.8, var10));
                band(var0, var2 * 0.86, var2 * 0.955, 72);
                tint(var0, var1, 0.14F * var8 * (float)sstep(0.3, 1.0, var10));
                band(var0, 0.0, var2 * 0.2, 24);
                var0.glow = 3.2;
                double var19 = ease(var10 * 1.25);
                ink(var0, var1, 0.95F * var8);
                ringOn(var0, var2, var13 * 1.7, var19, var15);
                ringOn(var0, var2 * 0.955, var13 * 0.6, var19, -var15 + 1.3);
                ringOn(var0, var2 * 0.86, var13 * 0.75, ease(var10 * 1.1 - 0.1), var15 + 2.1);
                ink(var0, var1, 0.9F * var8 * (float)sstep(0.25, 0.75, var10));
                script(var0, var2 * 0.865, var2 * 0.95, Math.max(14, (int)(var2 * 16.0)), var15, var13 * 0.5, var9, sstep(0.2, 0.9, var10));
                ink(var0, var1, 0.75F * var8 * (float)sstep(0.35, 0.85, var10));
                ticks(var0, var2 * 0.8, var2 * 0.845, Math.max(48, (int)(var2 * 40.0)) / 4 * 4, -var15 * 1.6, var13 * 0.35);
                int var21 = var1.design != CircleArt.Design.HOLY && var1.design != CircleArt.Design.ICE && var1.design != CircleArt.Design.BARRIER
                    ? (var1.design != CircleArt.Design.EARTH && var1.design != CircleArt.Design.GOLD ? (var1.design == CircleArt.Design.PETAL ? 5 : 3) : 4)
                    : 6;
                ink(var0, var1, 0.9F * var8 * (float)sstep(0.4, 0.9, var10));

                for (int var22 = 0; var22 < var21; var22++) {
                    double var23 = var17 * 0.5 + (Math.PI * 2) * (double)var22 / (double)var21 + (Math.PI / 2);
                    double var25 = Math.cos(var23) * var2 * 0.86;
                    double var27 = Math.sin(var23) * var2 * 0.86;
                    circleAt(var0, var25, var27, var2 * 0.055, var13 * 0.6);
                    circleAt(var0, var25, var27, var2 * 0.025, var13 * 0.5);
                }

                double var29 = 0.55 + 0.45 * ease(var10 * 1.2 - 0.1);
                float var24 = 0.92F * var8 * (float)sstep(0.1, 0.65, var10);
                figure(var0, var1, var2 * var29, var4, var17, var13, var24);
                ink(var0, var1, var24);
                circleAt(var0, 0.0, 0.0, var2 * 0.16 * var29, var13 * 0.8);
                circleAt(var0, 0.0, 0.0, var2 * 0.11 * var29, var13 * 0.5);
                var0.star(var2 * 0.1 * var29, 4, 1, -var17 * 2.0, var13 * 0.5);
                var0.glow = 0.0;
            }
        }
    }

    private static void figure(SpellCircleFx.Ctx var0, CircleArt.Look var1, double var2, double var4, double var6, double var8, float var10) {
        ink(var0, var1, var10);
        switch (var1.design) {
            case ATTACK:
            case HEAVY:
                circleAt(var0, 0.0, 0.0, var2 * 0.64, var8 * 0.8);
                var0.star(var2 * 0.64, 6, 2, var6, var8);

                for (int var56 = 0; var56 < 3; var56++) {
                    double var78 = var6 + (double)var56 * 2.0944 + (Math.PI / 2);
                    circleAt(var0, Math.cos(var78) * var2 * 0.2, Math.sin(var78) * var2 * 0.2, var2 * 0.3, var8 * 0.6);
                }

                for (int var57 = 0; var57 < 6; var57++) {
                    double var79 = var6 + (double)var57 * 1.0472;
                    circleAt(var0, Math.cos(var79) * var2 * 0.64, Math.sin(var79) * var2 * 0.64, var2 * 0.04, var8 * 0.6);
                }

                if (var1.design == CircleArt.Design.HEAVY) {
                    script(var0, var2 * 0.42, var2 * 0.5, 18, -var6, var8 * 0.4, 77, 1.0);
                    circleAt(var0, 0.0, 0.0, var2 * 0.42, var8 * 0.5);
                    circleAt(var0, 0.0, 0.0, var2 * 0.5, var8 * 0.5);
                    var0.star(var2 * 0.78, 12, 5, var6 * 0.5, var8 * 0.4);
                }
                break;
            case ICE:
                var0.polygon(var2 * 0.3, 6, var6, var8);
                var0.polygon(var2 * 0.7, 6, var6 + (Math.PI / 6), var8 * 0.6);

                for (int var55 = 0; var55 < 6; var55++) {
                    double var77 = var6 + (double)var55 * 1.0472;
                    polar(var0, var2 * 0.3, var77, var2 * 0.78, var77, var8);

                    for (double var113 : new double[]{0.45, 0.6}) {
                        double var127 = Math.cos(var77) * var2 * var113;
                        double var138 = Math.sin(var77) * var2 * var113;
                        double var145 = var2 * (0.75 - var113) * 0.7;

                        for (byte var151 = -1; var151 <= 1; var151 += 2) {
                            double var152 = var77 + (double)var151 * 0.7;
                            var0.seg(var127, var138, var127 + Math.cos(var152) * var145, var138 + Math.sin(var152) * var145, var8 * 0.7);
                        }
                    }
                }
                break;
            case WATER:
                for (int var52 = 0; var52 < 2; var52++) {
                    byte var75 = 96;
                    double var80 = 0.0;
                    double var93 = 0.0;

                    for (int var112 = 0; var112 <= var75; var112++) {
                        double var115 = (Math.PI * 2) * (double)var112 / (double)var75;
                        double var129 = var2 * (0.55 + 0.05 * Math.sin(var115 * 9.0 + var4 * (var52 == 0 ? 2.2 : -2.2) + (double)var52 * 1.6));
                        double var139 = Math.cos(var115) * var129;
                        double var146 = Math.sin(var115) * var129;
                        if (var112 > 0) {
                            var0.seg(var80, var93, var139, var146, var8 * (var52 == 0 ? 0.9 : 0.6));
                        }

                        var80 = var139;
                        var93 = var146;
                    }
                }

                for (int var53 = 0; var53 < 4; var53++) {
                    double var76 = var6 + (double)var53 * Math.PI / 2.0;
                    double var90 = Math.cos(var76) * var2 * 0.72;
                    double var103 = Math.sin(var76) * var2 * 0.72;

                    for (int var116 = 0; var116 < 12; var116++) {
                        double var126 = var76 + (Math.PI / 2) + Math.PI * (double)var116 / 12.0;
                        double var137 = var76 + (Math.PI / 2) + Math.PI * (double)(var116 + 1) / 12.0;
                        var0.seg(
                            var90 + Math.cos(var126) * var2 * 0.08,
                            var103 + Math.sin(var126) * var2 * 0.08,
                            var90 + Math.cos(var137) * var2 * 0.08,
                            var103 + Math.sin(var137) * var2 * 0.08,
                            var8 * 0.6
                        );
                    }
                }

                double var54 = 0.0;
                double var81 = 0.0;

                for (int var94 = 1; var94 <= 40; var94++) {
                    double var104 = (double)var94 / 40.0;
                    double var117 = var6 * 2.0 + var104 * Math.PI * 3.0;
                    double var130 = var2 * 0.32 * var104;
                    double var140 = Math.cos(var117) * var130;
                    double var147 = Math.sin(var117) * var130;
                    var0.seg(var54, var81, var140, var147, var8 * 0.6);
                    var54 = var140;
                    var81 = var147;
                }
                break;
            case EARTH:
                var0.polygon(var2 * 0.68, 4, var6, var8);
                var0.polygon(var2 * 0.68, 4, var6 + (Math.PI / 4), var8);
                var0.polygon(var2 * 0.36, 4, -var6, var8 * 0.8);
                circleAt(var0, 0.0, 0.0, var2 * 0.5, var8 * 0.5);

                for (int var50 = 0; var50 < 8; var50++) {
                    double var73 = var6 + (double)var50 * Math.PI / 4.0 + (Math.PI / 8);
                    polar(var0, var2 * 0.36, var73, var2 * 0.5, var73, var8 * 0.6);
                }

                for (int var51 = 0; var51 < 4; var51++) {
                    double var74 = -var6 + (double)var51 * Math.PI / 2.0 + (Math.PI / 4);
                    var0.polygon(var2 * 0.07, 4, var74, var8 * 0.5);
                }
                break;
            case FIRE:
                var0.polygon(var2 * 0.3, 3, var6, var8);
                var0.polygon(var2 * 0.3, 3, var6 + Math.PI, var8);
                circleAt(var0, 0.0, 0.0, var2 * 0.34, var8 * 0.6);

                for (int var49 = 0; var49 < 12; var49++) {
                    double var72 = var6 + (double)var49 * Math.PI / 6.0;
                    double var89 = Math.cos(var72) * var2 * 0.36;
                    double var102 = Math.sin(var72) * var2 * 0.36;

                    for (int var114 = 1; var114 <= 10; var114++) {
                        double var125 = (double)var114 / 10.0;
                        double var136 = var72 + 0.35 * Math.sin(var125 * Math.PI) * (double)(var49 % 2 == 0 ? 1 : -1);
                        double var144 = var2 * (0.36 + (var49 % 2 == 0 ? 0.42 : 0.3) * var125);
                        double var150 = Math.cos(var136) * var144;
                        double var153 = Math.sin(var136) * var144;
                        var0.seg(var89, var102, var150, var153, var8 * (1.0 - 0.6 * var125));
                        var89 = var150;
                        var102 = var153;
                    }
                }
                break;
            case LIGHTNING:
                byte var48 = 18;
                double var71 = 0.0;
                double var88 = 0.0;

                for (int var100 = 0; var100 <= var48 * 2; var100++) {
                    double var110 = var6 + (Math.PI * 2) * (double)var100 / (double)(var48 * 2);
                    double var123 = var2 * (var100 % 2 == 0 ? 0.66 : 0.52);
                    double var134 = Math.cos(var110) * var123;
                    double var143 = Math.sin(var110) * var123;
                    if (var100 > 0) {
                        var0.seg(var71, var88, var134, var143, var8);
                    }

                    var71 = var134;
                    var88 = var143;
                }

                var0.polygon(var2 * 0.26, 6, -var6, var8);

                for (int var101 = 0; var101 < 6; var101++) {
                    double var111 = -var6 + (double)var101 * 1.0472;
                    double[] var124 = new double[]{0.26, 0.34, 0.4, 0.48};
                    double[] var128 = new double[]{0.0, 0.12, -0.1, 0.06};

                    for (int var135 = 0; var135 + 1 < var124.length; var135++) {
                        polar(var0, var2 * var124[var135], var111 + var128[var135], var2 * var124[var135 + 1], var111 + var128[var135 + 1], var8 * 0.8);
                    }
                }
                break;
            case WIND:
                for (int var46 = 0; var46 < 6; var46++) {
                    double var69 = 0.0;
                    double var86 = 0.0;

                    for (int var98 = 0; var98 <= 30; var98++) {
                        double var108 = (double)var98 / 30.0;
                        double var121 = var2 * (0.12 + 0.66 * var108);
                        double var132 = var6 * 1.5 + (double)var46 * 1.0472 + var108 * 2.4;
                        double var141 = Math.cos(var132) * var121;
                        double var148 = Math.sin(var132) * var121;
                        if (var98 > 0) {
                            var0.seg(var69, var86, var141, var148, var8 * (0.4 + 0.6 * var108));
                        }

                        var69 = var141;
                        var86 = var148;
                    }
                }

                for (int var47 = 0; var47 < 3; var47++) {
                    double var70 = 0.0;
                    double var87 = 0.0;

                    for (int var99 = 0; var99 <= 12; var99++) {
                        double var109 = (double)var99 / 12.0;
                        double var122 = -var6 * 2.0 + (double)var47 * 2.0944 + var109 * 2.0;
                        double var133 = var2 * 0.3 * var109;
                        double var142 = Math.cos(var122) * var133;
                        double var149 = Math.sin(var122) * var133;
                        if (var99 > 0) {
                            var0.seg(var70, var87, var142, var149, var8 * 0.7);
                        }

                        var70 = var142;
                        var87 = var149;
                    }
                }
                break;
            case ARROWS:
                var0.star(var2 * 0.42, 8, 3, -var6, var8);
                circleAt(var0, 0.0, 0.0, var2 * 0.42, var8 * 0.6);

                for (int var45 = 0; var45 < 24; var45++) {
                    double var68 = var6 + (double)var45 * Math.PI / 12.0;
                    double var85 = var2 * (var45 % 2 == 0 ? 0.78 : 0.66);
                    polar(var0, var2 * 0.45, var68, var85, var68, var8 * 0.7);
                    polar(var0, var85, var68, var85 - var2 * 0.06, var68 + 0.05, var8 * 0.6);
                    polar(var0, var85, var68, var85 - var2 * 0.06, var68 - 0.05, var8 * 0.6);
                }
                break;
            case HOLY:
                circleAt(var0, 0.0, 0.0, var2 * 0.3, var8 * 0.8);

                for (int var42 = 0; var42 < 6; var42++) {
                    double var65 = var6 + (double)var42 * 1.0472;
                    circleAt(var0, Math.cos(var65) * var2 * 0.3, Math.sin(var65) * var2 * 0.3, var2 * 0.3, var8 * 0.8);
                }

                circleAt(var0, 0.0, 0.0, var2 * 0.6, var8);

                for (int var43 = 0; var43 < 4; var43++) {
                    double var66 = -var6 * 0.5 + (double)var43 * Math.PI / 2.0;
                    polar(var0, var2 * 0.6, var66, var2 * 0.8, var66, var8 * 1.2);
                }

                for (int var44 = 0; var44 < 12; var44++) {
                    double var67 = var6 + (double)var44 * Math.PI / 6.0 + (Math.PI / 12);
                    arc(var0, var2 * 0.7, var67 - 0.12, var67 + 0.12, var8 * 0.6);
                }
                break;
            case GOLD:
                circleAt(var0, 0.0, 0.0, var2 * 0.7, var8 * 0.6);
                var0.polygon(var2 * 0.7, 4, var6 + (Math.PI / 4), var8);
                var0.polygon(var2 * 0.495, 3, var6 + (Math.PI / 2), var8);
                circleAt(var0, 0.0, 0.0, var2 * 0.247, var8 * 0.8);

                for (int var41 = 0; var41 < 4; var41++) {
                    double var64 = var6 + (Math.PI / 4) + (double)var41 * Math.PI / 2.0;
                    circleAt(var0, Math.cos(var64) * var2 * 0.7, Math.sin(var64) * var2 * 0.7, var2 * 0.06, var8 * 0.6);
                }
                break;
            case CUT:
                for (int var40 = 0; var40 < 3; var40++) {
                    double var63 = var6 * 2.0 + (double)var40 * 2.0944;
                    arc(var0, var2 * 0.6, var63, var63 + 1.6, var8 * 1.2);
                    arc(var0, var2 * 0.5, var63 + 0.25, var63 + 1.45, var8 * 0.6);
                    polar(var0, var2 * 0.6, var63 + 1.6, var2 * 0.45, var63 + 1.75, var8 * 0.6);
                }

                var0.polygon(var2 * 0.25, 3, -var6, var8 * 0.8);
                break;
            case PETAL:
                for (int var39 = 0; var39 < 5; var39++) {
                    double var62 = var6 + (double)var39 * 1.2566;

                    for (byte var84 = -1; var84 <= 1; var84 += 2) {
                        double var92 = 0.0;
                        double var107 = 0.0;

                        for (int var120 = 0; var120 <= 16; var120++) {
                            double var20 = (double)var120 / 16.0;
                            double var22 = var2 * 0.7 * var20;
                            double var24 = Math.sin(var20 * Math.PI) * 0.35 * (double)var84;
                            double var26 = Math.cos(var62 + var24) * var22;
                            double var28 = Math.sin(var62 + var24) * var22;
                            if (var120 > 0) {
                                var0.seg(var92, var107, var26, var28, var8 * 0.8);
                            }

                            var92 = var26;
                            var107 = var28;
                        }
                    }
                }

                var0.polygon(var2 * 0.2, 5, -var6, var8);
                break;
            case BIND:
                for (byte var36 = -1; var36 <= 1; var36 += 2) {
                    double var59 = 0.0;
                    double var82 = 0.0;

                    for (int var97 = 0; var97 <= 24; var97++) {
                        double var106 = (double)var97 / 24.0;
                        double var119 = (var106 * 2.0 - 1.0) * var2 * 0.72;
                        double var131 = (double)var36 * Math.sin(var106 * Math.PI) * var2 * 0.3;
                        if (var97 > 0) {
                            var0.seg(var59, var82, var119, var131, var8);
                        }

                        var59 = var119;
                        var82 = var131;
                    }
                }

                circleAt(var0, 0.0, 0.0, var2 * 0.22, var8);
                circleAt(var0, 0.0, 0.0, var2 * 0.08, var8 * 1.4);

                for (int var37 = 0; var37 < 9; var37++) {
                    double var60 = Math.PI * (0.15 + 0.7 * (double)var37 / 8.0);
                    polar(var0, var2 * 0.34, var60, var2 * 0.44, var60, var8 * 0.5);
                }

                for (int var38 = 0; var38 < 4; var38++) {
                    double var61 = var6 + (double)var38 * Math.PI / 2.0 + (Math.PI / 4);

                    for (int var83 = 0; var83 < 3; var83++) {
                        circleAt(
                            var0,
                            Math.cos(var61) * var2 * (0.5 + 0.09 * (double)var83),
                            Math.sin(var61) * var2 * (0.5 + 0.09 * (double)var83),
                            var2 * 0.045,
                            var8 * 0.5
                        );
                    }
                }
                break;
            case BARRIER:
                double var35 = var2 * 0.11;
                double var13 = var35 * Math.sqrt(3.0);

                for (int var15 = -6; var15 <= 6; var15++) {
                    for (int var96 = -6; var96 <= 6; var96++) {
                        double var17 = var13 * ((double)var15 + (double)var96 * 0.5);
                        double var118 = var35 * 1.5 * (double)var96;
                        if (!(var17 * var17 + var118 * var118 > var2 * 0.72 * var2 * 0.72)) {
                            double var21 = Math.cos(var6 * 0.3);
                            double var23 = Math.sin(var6 * 0.3);
                            double var25 = var17 * var21 - var118 * var23;
                            double var27 = var17 * var23 + var118 * var21;

                            for (int var29 = 0; var29 < 6; var29++) {
                                double var30 = var6 * 0.3 + (Math.PI / 6) + (double)var29 * Math.PI / 3.0;
                                double var32 = var30 + (Math.PI / 3);
                                var0.seg(
                                    var25 + Math.cos(var30) * var35 * 0.92,
                                    var27 + Math.sin(var30) * var35 * 0.92,
                                    var25 + Math.cos(var32) * var35 * 0.92,
                                    var27 + Math.sin(var32) * var35 * 0.92,
                                    var8 * 0.5
                                );
                            }
                        }
                    }
                }
            case GOLEM:
            default:
                break;
            case FLOWER:
                for (int var34 = 0; var34 < 10; var34++) {
                    double var58 = var6 + (double)var34 * Math.PI / 5.0;
                    double var14 = Math.cos(var58) * var2 * 0.58;
                    double var16 = Math.sin(var58) * var2 * 0.58;

                    for (int var18 = 0; var18 < 5; var18++) {
                        double var19 = var58 + (double)var18 * 1.2566;
                        circleAt(var0, var14 + Math.cos(var19) * var2 * 0.05, var16 + Math.sin(var19) * var2 * 0.05, var2 * 0.045, var8 * 0.5);
                    }
                }

                circleAt(var0, 0.0, 0.0, var2 * 0.45, var8 * 0.7);
                var0.star(var2 * 0.4, 5, 2, -var6, var8 * 0.7);
                break;
            case SPEED:
                for (int var11 = 0; var11 < 8; var11++) {
                    double var12 = var6 * 3.0 + (double)var11 * Math.PI / 4.0;
                    polar(var0, var2 * 0.45, var12 - 0.15, var2 * 0.6, var12, var8);
                    polar(var0, var2 * 0.6, var12, var2 * 0.45, var12 + 0.15, var8);
                }

                circleAt(var0, 0.0, 0.0, var2 * 0.38, var8 * 0.6);
        }
    }

    private static void golem(SpellCircleFx.Ctx var0, CircleArt.Look var1, double var2, double var4, double var6, float var8) {
        double var9 = 0.3 + 0.7 * ease(var6 * 1.2);
        double var11 = var4 * 0.05;
        byte var13 = 16;
        var0.glow = 0.0;
        if (var6 < 1.0) {
            ink(var0, var1, (float)((1.0 - var6) * 0.7) * var8);
            var0.ring(var2 * (0.5 + 0.7 * ease(var6)), 0.06 * var2 * (1.0 - var6) + 0.02);
        }

        for (int var14 = 0; var14 < var13; var14++) {
            double var15 = var11 + (Math.PI * 2) * (double)var14 / (double)var13;
            double var17 = Math.PI / (double)var13 * 0.95;
            double var19 = var2 * (var14 % 2 == 0 ? 1.0 : 0.72) * var9;
            double var21 = var2 * 0.3 * var9;
            tint(var0, var1, 0.42F * var8);
            tri(
                var0,
                Math.cos(var15 - var17) * var21,
                Math.sin(var15 - var17) * var21,
                Math.cos(var15) * var19,
                Math.sin(var15) * var19,
                Math.cos(var15 + var17) * var21,
                Math.sin(var15 + var17) * var21
            );
            col(var0, Math.min(1.0F, var1.gr * 0.7F), Math.min(1.0F, var1.gg * 0.7F), Math.min(1.0F, var1.gb * 0.85F), 0.35F * var8);
            tri(
                var0,
                Math.cos(var15 - var17 * 0.35) * var21,
                Math.sin(var15 - var17 * 0.35) * var21,
                Math.cos(var15) * var19 * 0.92,
                Math.sin(var15) * var19 * 0.92,
                Math.cos(var15) * var21 * 1.02,
                Math.sin(var15) * var21 * 1.02
            );
            var0.glow = 2.0;
            ink(var0, var1, 0.65F * var8);
            var0.seg(Math.cos(var15 - var17) * var21, Math.sin(var15 - var17) * var21, Math.cos(var15) * var19, Math.sin(var15) * var19, 0.012 + 0.006 * var2);
            var0.seg(Math.cos(var15) * var19, Math.sin(var15) * var19, Math.cos(var15 + var17) * var21, Math.sin(var15 + var17) * var21, 0.012 + 0.006 * var2);
            var0.glow = 0.0;
        }

        tint(var0, var1, 0.35F * var8);
        band(var0, 0.0, var2 * 0.3 * var9, 40);
        col(var0, Math.min(1.0F, var1.gr + 0.25F), Math.min(1.0F, var1.gg + 0.25F), Math.min(1.0F, var1.gb + 0.15F), 0.4F * var8);
        band(var0, var2 * 0.2 * var9, var2 * 0.26 * var9, 40);
        var0.glow = 2.5;
        ink(var0, var1, 0.8F * var8);
        var0.ring(var2 * 0.3 * var9, 0.014 + 0.008 * var2);
        var0.ring(var2 * 0.2 * var9, 0.01 + 0.005 * var2);
        var0.glow = 0.0;
    }

    public static enum Design {
        ATTACK,
        HEAVY,
        ICE,
        WATER,
        EARTH,
        FIRE,
        LIGHTNING,
        WIND,
        ARROWS,
        HOLY,
        GOLD,
        CUT,
        PETAL,
        BIND,
        BARRIER,
        GOLEM,
        FLOWER,
        SPEED;
    }

    public static record Look(CircleArt.Design design, float lr, float lg, float lb, float gr, float gg, float gb) {
    }
}
