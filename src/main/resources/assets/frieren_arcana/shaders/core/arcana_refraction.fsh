#version 150
uniform sampler2D SceneSampler;
uniform vec2 ScreenSize;
uniform float ArcanaTime;
uniform vec4 ColorModulator;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 uv=gl_FragCoord.xy/ScreenSize;
    vec2 displacement=(vertexColor.rg*2.0-1.0)*(.004+.001*sin(ArcanaTime*3.0));
    vec3 spectrum=vec3(texture(SceneSampler,clamp(uv+displacement*1.25,.001,.999)).r,
                       texture(SceneSampler,clamp(uv+displacement,.001,.999)).g,
                       texture(SceneSampler,clamp(uv+displacement*.7,.001,.999)).b);
    fragColor=vec4(mix(spectrum,vec3(.03,.55,.26),.14),vertexColor.a)*ColorModulator;
}
