#version 150
uniform sampler2D SceneSampler;
uniform vec2 ScreenSize;
uniform float ArcanaTime;
uniform vec4 ColorModulator;
in vec4 vertexColor;
out vec4 fragColor;
// Falling barrier shards: clear holographic glass. Each shard carries its own seed in red/green and a facet
// brightness in blue. The scene behind is bent and split into a soft spectrum; the surface carries an oily
// purple / cyan / pink / green sheen like the shattered dome in the anime. No static, no scanlines.
void main() {
    vec2 uv = gl_FragCoord.xy / ScreenSize;
    vec2 seed = vertexColor.rg;
    vec2 bend = (seed * 2.0 - 1.0) * (.010 + .004 * sin(ArcanaTime * 1.7 + seed.x * 9.0));
    vec3 spectrum = vec3(texture(SceneSampler, clamp(uv + bend * 1.5, .001, .999)).r,
                         texture(SceneSampler, clamp(uv + bend, .001, .999)).g,
                         texture(SceneSampler, clamp(uv + bend * .6, .001, .999)).b);
    float phase = seed.x * 2.3 + seed.y * 1.1 + uv.x * 1.4 + uv.y * .9 + ArcanaTime * .08;
    vec3 holo = .55 + .45 * cos(6.2831 * (vec3(0.0, .33, .67) + phase));
    float facet = vertexColor.b;
    vec3 glass = spectrum * (.70 + .25 * facet) + holo * (.32 + .40 * facet);
    glass += vec3(.10, .35, .20) * .25;
    fragColor = vec4(glass, clamp(vertexColor.a, 0.0, 1.0)) * ColorModulator;
}
