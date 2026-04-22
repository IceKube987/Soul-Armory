#version 150

uniform vec4 ColorModulator;
uniform vec4 UVRange;
uniform float GameTime;

in vec2 texCoord0;

out vec4 fragColor;

void main() {

    float localX = (texCoord0.x - UVRange.x) / (UVRange.y - UVRange.x);
    float localY = (texCoord0.y - UVRange.z) / (UVRange.w - UVRange.z);

    vec4 color = vec4(localX, localY, 0.0, 1.0);

    fragColor = color * ColorModulator;
}
