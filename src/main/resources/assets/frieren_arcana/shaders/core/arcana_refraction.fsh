#version 150
uniform sampler2D SceneSampler;
uniform vec2 ScreenSize;
uniform float ArcanaTime;
uniform vec4 ColorModulator;
in vec4 vertexColor;
out vec4 fragColor;
float rnd(vec2 p) { return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453); }
void main() {
    vec2 uv = gl_FragCoord.xy / ScreenSize;
    float tick = floor(ArcanaTime * 14.0);
    // horizontal tearing bands, like a broken signal behind cracked glass
    float band = rnd(vec2(floor(uv.y * 90.0), tick));
    float tear = (band > 0.86 ? (band - 0.93) * 0.05 : 0.0) * vertexColor.a;
    vec2 displacement = (vertexColor.rg * 2.0 - 1.0) * (.006 + .0015 * sin(ArcanaTime * 3.0));
    vec2 base = uv + vec2(tear, 0.0);
    vec3 spectrum = vec3(texture(SceneSampler, clamp(base + displacement * 1.6, .001, .999)).r,
                         texture(SceneSampler, clamp(base + displacement, .001, .999)).g,
                         texture(SceneSampler, clamp(base + displacement * .55, .001, .999)).b);
    // sparse green static blocks
    vec2 cell = floor(gl_FragCoord.xy / vec2(5.0, 2.0));
    float n = rnd(cell + tick * 1.37);
    float stat = step(0.86, n) * (0.35 + 0.65 * rnd(cell.yx + tick));
    float scan = 0.94 + 0.06 * sin(gl_FragCoord.y * 1.7 + ArcanaTime * 40.0);
    vec3 glass = mix(spectrum * scan, vec3(.03, .55, .26), .16);
    glass += vec3(.06, .95, .42) * stat * 0.6;
    fragColor = vec4(glass, clamp(vertexColor.a + stat * 0.25, 0.0, 1.0)) * ColorModulator;
}
