#version 150

uniform vec4 ColorModulator;

uniform int FadeinTime;

uniform int Started;

uniform float GameTime;

in vec2 texCoord0;

out vec4 fragColor;

float linearstep(float mini, float maxi, float x){
    float u = (x-mini) / (maxi-mini);
    u = clamp(u,0,1);
    return u;
}

void main() {
    vec4 color = vec4(87.0 / 255.0, 202.0 / 255.0, 247.0 / 255.0, 1.0);

    float time = GameTime * 24000;

    float distsqr = texCoord0.x * texCoord0.x + texCoord0.y * texCoord0.y;

    //    float left = 1.0 - smoothstep(0.0, 0.1, texCoord0.x);
    //    float right = smoothstep(0.9, 1.0, texCoord0.x);
    //    float total = left + right;
    float al = smoothstep(1, 2, distsqr);

    color.a *= al;
    color.a *= 0.5;
//    color.rgb *= color.a;

    color *= Started;
    if (color.a == 0.0) {
        discard;
    }

    fragColor = color * ColorModulator;
}
