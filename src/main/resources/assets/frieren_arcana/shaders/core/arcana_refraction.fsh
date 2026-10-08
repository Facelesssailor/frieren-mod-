#version 150
uniform sampler2D SceneSampler;
uniform vec2 ScreenSize;
uniform float ArcanaTime;
uniform vec4 ColorModulator;
in vec4 vertexColor;
out vec4 fragColor;
// Falling barrier shards: clear crystal glass. Each shard carries its own seed in red/green and a facet brightness in
// blue. The scene behind is bent and split into a soft spectrum; the surface carries the pale thin-film sheen of the
// shattered dome in ep. 21: lavender, ice blue, pink and gold, flaring white when a facet turns toward the camera.
void main() {
    vec2 uv = gl_FragCoord.xy / ScreenSize;
    vec2 seed = vertexColor.rg;
    vec2 bend = (seed * 2.0 - 1.0) * (.010 + .004 * sin(ArcanaTime * 1.7 + seed.x * 9.0));
    vec3 spectrum = vec3(texture(SceneSampler, clamp(uv + bend * 1.5, .001, .999)).r,
                         texture(SceneSampler, clamp(uv + bend, .001, .999)).g,
                         texture(SceneSampler, clamp(uv + bend * .6, .001, .999)).b);
    float phase = seed.x * 2.3 + seed.y * 1.1 + uv.x * 1.4 + uv.y * .9 + ArcanaTime * .08;
    vec3 holo = .64 + .36 * cos(6.2831 * (vec3(0.0, .33, .67) + phase));
    holo = mix(holo, vec3(.74, .70, 1.0), .40);
    float facet = vertexColor.b;
    vec3 glass = spectrum * (.42 + .20 * facet) + holo * (.58 + .30 * facet) + vec3(.55) * facet * facet;
    fragColor = vec4(glass, clamp(vertexColor.a, 0.0, 1.0)) * ColorModulator;
}
