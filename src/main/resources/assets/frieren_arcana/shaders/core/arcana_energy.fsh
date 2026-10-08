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
    // Tagged by green .90 and blue .60-.64. Red carries the view-angle (fresnel) term, blue carries how far
    // Frieren's spell has progressed. At rest: a smooth, faint, slightly translucent green with a soft brighter rim.
    // While the spell runs: an oily thin-film rainbow sheen and the thin green / violet / pale-gold horizontal bands.
    if (abs(vertexColor.g - 0.90) < 0.008 && vertexColor.b > 0.585 && vertexColor.b < 0.655) {
        float k = clamp((vertexColor.b - 0.598) / 0.04, 0.0, 1.0);
        float fr = clamp(vertexColor.r, 0.0, 1.0);
        vec3 p = magicPosition;
        float drift = fbm(p * .035 + vec3(0.0, ArcanaTime * .035, ArcanaTime * .02));
        float soft = noise(p * .11 + vec3(ArcanaTime * .07, 0.0, -ArcanaTime * .05));
        vec3 col = vec3(.26, 1.0, .46) * (.82 + .28 * drift) + vec3(.36, .18, .26) * fr * fr;
        float a = vertexColor.a * (.86 + .22 * drift + .08 * soft);
        if (k > 0.01) {
            float film = drift * 2.4 + fr * 1.7 + p.y * .025 + ArcanaTime * .03;
            vec3 oil = .55 + .45 * cos(6.2831 * (vec3(0.0, .33, .67) + film));
            col = mix(col, oil, k * (.22 + .45 * fr));
            // Bold horizontal stripes like the struck dome in ep. 21: violet, lavender, green, lime, white and a little
            // deep teal, uneven widths, edges softly wobbling; thin white filaments ride on top.
            float y = p.y;
            float wob = noise(vec3(p.x * .04, y * .06, p.z * .04) + ArcanaTime * .03) * 1.2;
            float s = y * .40 + .9 * sin(y * .11) + wob;
            float id = floor(s), fs = fract(s);
            float h = fract(sin(id * 91.7 + 3.1) * 43758.5453);
            vec3 stripe = h < .30 ? vec3(.62, .42, .94) : h < .47 ? vec3(.82, .72, 1.0) : h < .63 ? vec3(.40, .88, .48)
                        : h < .76 ? vec3(.86, .98, .52) : h < .89 ? vec3(.97, 1.0, .94) : vec3(.18, .44, .42);
            float edge = smoothstep(0.0, .14, fs) * smoothstep(1.0, .86, fs);
            float fine = pow(sin(y * 3.3 + wob * 2.0) * .5 + .5, 9.0);
            vec3 band = mix(col, stripe, .35 + .65 * edge);
            band = mix(band, vec3(1.0), fine * .35);
            float bk = k * k;
            col = mix(col, band, bk * .9);
            a = mix(a, max(a * 1.2, .38 + .24 * edge) + .12 * fine, bk);
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
