#version 150
in vec4 vertexColor;
in vec3 magicPosition;
uniform vec4 ColorModulator;
uniform float ArcanaTime;
out vec4 fragColor;

float hash(vec3 p) { return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453); }
float noise(vec3 p) {
    vec3 i = floor(p), f = fract(p); f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash(i), hash(i + vec3(1, 0, 0)), f.x), mix(hash(i + vec3(0, 1, 0)), hash(i + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(hash(i + vec3(0, 0, 1)), hash(i + vec3(1, 0, 1)), f.x), mix(hash(i + vec3(0, 1, 1)), hash(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}
float fbm(vec3 p) { return noise(p) * .55 + noise(p * 2.03 + 7.1) * .3 + noise(p * 4.1 + 13.7) * .15; }

void main() {
    // ---- Barrier dome --------------------------------------------------------------------------------------------
    // Tagged by green .90 and blue .60-.655. Red carries the view-angle (fresnel) term.
    // blue exactly .60: a barrier at rest - a smooth, faint, slightly translucent green with a soft brighter rim.
    // blue above: struck by Frieren's Barrier Breaker (ep. 21, 15:08 - 15:18). The green is gone: dark clear glass with
    // an oily rainbow film; (blue - .60) / .04 is how far the bands have come; blue bytes 164 / 165 are the lime-yellow
    // pulse and the cyan-white flash just before it breaks. Alpha then carries half the opacity.
    if (abs(vertexColor.g - 0.90) < 0.008 && vertexColor.b > 0.585 && vertexColor.b < 0.656) {
        float kk = (vertexColor.b - 0.598) / 0.04;
        float fr = clamp(vertexColor.r, 0.0, 1.0);
        vec3 p = magicPosition;
        float drift = fbm(p * .035 + vec3(0.0, ArcanaTime * .035, ArcanaTime * .02));
        if (kk < 0.1) {
            float soft = noise(p * .11 + vec3(ArcanaTime * .07, 0.0, -ArcanaTime * .05));
            vec3 col = vec3(.26, 1.0, .46) * (.82 + .28 * drift) + vec3(.36, .18, .26) * fr * fr;
            float a = vertexColor.a * (.86 + .22 * drift + .08 * soft);
            fragColor = vec4(col, clamp(a, 0.0, 1.0)) * ColorModulator;
            if (fragColor.a < .002) discard;
            return;
        }
        float k = clamp(kk, 0.0, 1.0);
        float op = clamp(vertexColor.a * 2.0, 0.0, 1.0);
        // oil on dark glass: marbled rainbow, strongest toward the silhouette
        float sw = fbm(p * .05 + vec3(0.0, ArcanaTime * .02, ArcanaTime * .015));
        float sw2 = fbm(p * .13 + vec3(ArcanaTime * .03, sw * 2.0, 0.0));
        float film = sw * 3.2 + sw2 * 1.3 + fr * 1.5 + ArcanaTime * .02;
        vec3 oil = .5 + .5 * cos(6.2831 * (vec3(0.0, .33, .67) + film));
        float blot = smoothstep(.30, .70, sw2);
        vec3 col = mix(vec3(.07, .09, .12), oil, .35 + .30 * blot + .30 * fr);
        float a = (.07 + .24 * fr + .09 * blot) * op;
        // the bands: bold horizontal stripes over the struck dome
        float bk = smoothstep(.15, 1.0, k);
        if (bk > 0.0) {
            float y = p.y;
            float wob = noise(vec3(p.x * .04, y * .06, p.z * .04) + ArcanaTime * .03) * 1.2;
            float aa = fwidth(y);                                    // fade the finest stripes out where they would alias
            float s1 = y * .50 + .7 * sin(y * .07) + wob;            // broad stripes
            float s2 = y * 1.6 + wob * 1.7;                          // fine stripes riding on them
            float h1 = fract(sin(floor(s1) * 91.7 + 3.1) * 43758.5453), h2 = fract(sin(floor(s2) * 47.3 + 1.7) * 43758.5453);
            // a fixed run of colours so the sky is mostly violet and lavender, as at 15:16
            int ci = int(mod(floor(s1) * 7.0 + floor(h1 * 2.0), 12.0));
            vec3 st = ci == 0 || ci == 3 || ci == 6 || ci == 9 ? vec3(.68, .50, .92) : ci == 1 || ci == 5 || ci == 10 ? vec3(.82, .70, 1.0)
                    : ci == 2 || ci == 8 ? vec3(.40, .78, .48) : ci == 4 ? vec3(.86, .96, .55) : ci == 7 ? vec3(.96, 1.0, .95) : vec3(.22, .40, .52);
            vec3 fine = h2 < .4 ? vec3(.62, .44, .90) : h2 < .6 ? vec3(.97, 1.0, .96) : h2 < .8 ? vec3(.46, .86, .50) : vec3(.30, .30, .70);
            float f1 = fract(s1), f2 = fract(s2);
            float e1 = mix(smoothstep(0.0, .10, f1) * smoothstep(1.0, .90, f1), .8, smoothstep(.6, 1.6, aa));
            float e2 = smoothstep(.15, .35, f2) * smoothstep(.85, .65, f2) * (1.0 - smoothstep(.35, .9, aa));
            vec3 band = mix(col, st, .45 + .55 * e1);
            band = mix(band, fine, .55 * e2 * step(.5, h2 + h1 * .3));
            // a saturated rainbow edge where the bands are still spreading (15:10.5 - 15:11.5)
            float edge = smoothstep(.20, .45, k) * (1.0 - smoothstep(.55, .85, k));
            vec3 rim = .5 + .5 * cos(6.2831 * (vec3(0.0, .33, .67) + y * .5 + ArcanaTime * .2));
            band = mix(band, rim, edge * .6);
            col = mix(col, band, bk);
            a = mix(a, max(a, (.62 + .25 * e1) * op), bk);
            // the pulses before the break
            if (kk > 1.08) {
                bool cyan = kk > 1.18;
                vec3 pal = cyan ? (h1 < .5 ? vec3(.85, 1.0, 1.0) : h1 < .8 ? vec3(.55, .85, 1.0) : vec3(1.0)) 
                                : (h1 < .4 ? vec3(.88, .98, .30) : h1 < .7 ? vec3(.62, .86, .22) : vec3(.98, .92, .55));
                col = mix(pal, vec3(1.0), cyan ? .25 * e2 : .15 * e2);
                a = max(a, .85 * op);
            }
        }
        fragColor = vec4(col, clamp(a, 0.0, 1.0)) * ColorModulator;
        if (fragColor.a < .002) discard;
        return;
    }
    // ---- everything else: soft continuous shimmer -----------------------------------------------------------------
    vec4 c = vertexColor * ColorModulator;
    float n = noise(magicPosition * .75 + vec3(0, -ArcanaTime * .23, ArcanaTime * .09));
    float fine = noise(magicPosition * 3.2 + ArcanaTime * .13);
    c.rgb *= (.92 + .08 * n) * 1.12;
    c.a *= .84 + .16 * n;
    c.rgb += vec3(.012, .018, .025) * fine * c.a;
    if (c.a < .002) discard;
    fragColor = c;
}
