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
    c.rgb *= (.89+.11*n)*1.14;
    c.a *= .78+.22*n;
    c.rgb += vec3(.012,.018,.025)*fine*c.a;
    // Barrier shell: tagged by green .90 and a blue channel of .60-.64. Plain translucent green normally; the thin
    // iridescent bands (green / violet / pale gold) fade in only while Frieren's spell is running (blue encodes strength).
    if (abs(vertexColor.g - 0.90) < 0.008 && vertexColor.b > 0.585 && vertexColor.b < 0.655) {
        float k = clamp((vertexColor.b - 0.598) / 0.04, 0.0, 1.0);
        if (k > 0.02) {
            float y = magicPosition.y;
            float wob = noise(vec3(magicPosition.x * .05, y * .08, magicPosition.z * .05) + ArcanaTime * .03) * 1.6;
            float fine = pow(sin(y * 3.3 + wob * 2.0) * .5 + .5, 7.0);
            float mid = pow(sin(y * 1.15 - wob) * .5 + .5, 3.0);
            float hue = sin(y * .42 + wob * .8 + ArcanaTime * .05) * .5 + .5;
            vec3 green = vec3(.42, 1.0, .58), violet = vec3(.72, .42, 1.0), gold = vec3(1.0, .98, .66);
            vec3 band = mix(mix(green, violet, smoothstep(.25, .75, hue)), gold, mid * .35);
            c.rgb = mix(c.rgb, band, k * (.50 + .35 * fine));
            c.a *= mix(1.0, .45 + .75 * fine + .35 * mid, k);
        }
    }
    if(c.a<.002)discard;
    fragColor=c;
}
