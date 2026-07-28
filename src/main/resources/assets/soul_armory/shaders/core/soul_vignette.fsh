#version 150

uniform vec4 ColorModulator;

uniform int FadeinTime;

uniform float GameTime;

uniform float AlphaScale;

in vec2 texCoord0;

out vec4 fragColor;

float linearstep(float mini, float maxi, float x) {
    float u = (x - mini) / (maxi - mini);
    u = clamp(u, 0, 1);
    return u;
}

void main() {
    vec4 color = vec4(87.0 / 255.0, 202.0 / 255.0, 247.0 / 255.0, 1.0);

    float edge = 0.85 + 0.012*sin(GameTime*7000);
    // Superellipse Norm
    float n = 25.0;
    float tex = pow(pow(abs(texCoord0.x), n) + pow(abs(texCoord0.y), n), 1.0 / n);

    color.a = clamp((tex - edge) / (1.0 - edge), 0.0, 1.0) * 0.75;
    color.a *= AlphaScale;

    if (color.a == 0.0) {
        discard;
    }

    fragColor = color * ColorModulator;
}
