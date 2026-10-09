#version 150
// Frieren Arcana cutscene pass: glow around bright light, and a soft background for the close-ups
// (ep. 21, 15:12 - 15:16, 15:20 - 15:26), done on the finished world image.
uniform sampler2D SceneSampler;
uniform sampler2D DepthSampler;
uniform vec2 ScreenSize;
uniform float Bloom;
uniform float Blur;
uniform float Focus;
uniform vec2 Planes;
in vec2 texCoord;
out vec4 fragColor;

float linearDepth(vec2 uv) {
    float z = texture(DepthSampler, uv).r * 2.0 - 1.0;
    return 2.0 * Planes.x * Planes.y / (Planes.y + Planes.x - z * (Planes.y - Planes.x));
}

void main() {
    vec2 px = 1.0 / ScreenSize;
    vec3 base = texture(SceneSampler, texCoord).rgb;
    vec3 col = base;
    // depth-aware background blur: only what lies well behind the subject goes soft, and the sharp subject is
    // never smeared into it
    if (Blur > 0.5) {
        float d = linearDepth(texCoord);
        float coc = clamp((d - Focus * 1.6) / (Focus * 2.5), 0.0, 1.0);
        if (coc > 0.01) {
            vec3 acc = vec3(0.0);
            float wsum = 0.0;
            for (int i = 0; i < 44; i++) {
                float a = float(i) * 2.39996 + 0.3;
                float r = sqrt((float(i) + 0.5) / 44.0);
                vec2 uv = texCoord + vec2(cos(a), sin(a)) * r * Blur * coc * px;
                float wgt = linearDepth(uv) > Focus * 1.3 ? 1.0 : 0.04;
                acc += texture(SceneSampler, uv).rgb * wgt;
                wsum += wgt;
            }
            col = mix(base, acc / max(wsum, 1e-4), coc);
        }
    }
    // glow: bright pixels bleed light into their surroundings
    if (Bloom > 0.0) {
        vec3 glow = vec3(0.0);
        float reach = ScreenSize.y * 0.055;
        for (int i = 0; i < 32; i++) {
            float a = float(i) * 2.39996 + 0.9;
            float r = (float(i) + 0.5) / 32.0;
            vec2 uv = texCoord + vec2(cos(a), sin(a)) * r * r * reach * px;
            vec3 s = texture(SceneSampler, uv).rgb;
            float l = dot(s, vec3(0.2126, 0.7152, 0.0722));
            glow += s * smoothstep(0.62, 1.0, l) * (1.0 - 0.75 * r);
        }
        col += glow / 32.0 * Bloom * 1.9;
    }
    fragColor = vec4(min(col, vec3(1.0)), 1.0);
}
