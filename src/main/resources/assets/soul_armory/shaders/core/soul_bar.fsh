#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform vec4 UVRange;

uniform float GameTime;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) {
        discard;
    }

    // GameTime is in [0, 1) over course of the day, turn it back into ticks
    float time = GameTime * 24000;
    // Normalize UV coordinate
    float localX = (texCoord0.x - UVRange.x) / (UVRange.y - UVRange.x);
    float localY = (texCoord0.y - UVRange.z) / (UVRange.w - UVRange.z);


    fragColor = color * ColorModulator;
}
