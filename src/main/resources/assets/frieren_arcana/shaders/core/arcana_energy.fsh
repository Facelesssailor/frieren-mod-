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
        vec3 col = vec3(.38, 1.0, .62) * (.82 + .28 * drift) + vec3(.40, .22, .30) * fr * fr;
        float a = vertexColor.a * (.86 + .22 * drift + .08 * soft);
        if (k > 0.01) {
            float film = drift * 2.4 + fr * 1.7 + p.y * .025 + ArcanaTime * .03;
            vec3 oil = .55 + .45 * cos(6.2831 * (vec3(0.0, .33, .67) + film));
            col = mix(col, oil, k * (.22 + .45 * fr));
            float y = p.y;
            float wob = noise(vec3(p.x * .05, y * .08, p.z * .05) + ArcanaTime * .03) * 1.6;
            float fine = pow(sin(y * 3.3 + wob * 2.0) * .5 + .5, 7.0);
            float mid = pow(sin(y * 1.15 - wob) * .5 + .5, 3.0);
            float hue = sin(y * .42 + wob * .8 + ArcanaTime * .05) * .5 + .5;
            vec3 green = vec3(.42, 1.0, .58), violet = vec3(.74, .40, 1.0), gold = vec3(1.0, .98, .70);
            vec3 band = mix(mix(green, violet, smoothstep(.25, .75, hue)), gold, mid * .4);
            float bk = k * k;
            col = mix(col, band, bk * (.45 + .45 * fine));
            a = mix(a, a * 1.4 + .30 * fine + .18 * mid, bk);
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
