#version 150
in vec4 vertexColor;
in vec3 magicPosition;
uniform vec4 ColorModulator;
uniform float ArcanaTime;
out vec4 fragColor;
// Original continuous world-space shimmer. No post-processing or external shader dependency.
float hash(vec3 p) { return fract(sin(dot(p,vec3(127.1,311.7,74.7)))*43758.5453); }
float noise(vec3 p) {
    vec3 i=floor(p),f=fract(p); f=f*f*(3.0-2.0*f);
    return mix(mix(mix(hash(i),hash(i+vec3(1,0,0)),f.x),mix(hash(i+vec3(0,1,0)),hash(i+vec3(1,1,0)),f.x),f.y),
               mix(mix(hash(i+vec3(0,0,1)),hash(i+vec3(1,0,1)),f.x),mix(hash(i+vec3(0,1,1)),hash(i+vec3(1,1,1)),f.x),f.y),f.z);
}
void main() {
    vec4 c=vertexColor*ColorModulator;
    float n=noise(magicPosition*.75+vec3(0,-ArcanaTime*.23,ArcanaTime*.09));
    float fine=noise(magicPosition*3.2+ArcanaTime*.13);
    c.rgb *= .89+.11*n;
    c.a *= .78+.22*n;
    c.rgb += vec3(.012,.018,.025)*fine*c.a;
    if(c.a<.002)discard;
    fragColor=c;
}
