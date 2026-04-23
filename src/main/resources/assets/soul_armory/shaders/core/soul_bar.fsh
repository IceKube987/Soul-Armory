#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform vec4 UVRange;
uniform float GameTime;
uniform float SoulOverflowPercentage;

in vec2 texCoord0;

out vec4 fragColor;

vec2 n22(vec2 p)
{
    vec3 a = fract(p.xyx * vec3(123.34, 234.34, 345.65));
    a += dot(a, a + 34.45);
    return fract(vec2(a.x * a.y, a.y * a.z));
}

vec2 get_gradient(vec2 pos)
{
    float twoPi = 6.283185;
    float angle = n22(pos).x * twoPi;
    return vec2(cos(angle), sin(angle));
}

float perlin_noise(vec2 uv, float cells_count)
{
    vec2 pos_in_grid = uv * cells_count;
    vec2 cell_pos_in_grid = floor(pos_in_grid);
    vec2 local_pos_in_cell = (pos_in_grid - cell_pos_in_grid);
    vec2 blend = local_pos_in_cell * local_pos_in_cell * (3.0f - 2.0f * local_pos_in_cell);

    vec2 left_top = cell_pos_in_grid + vec2(0, 1);
    vec2 right_top = cell_pos_in_grid + vec2(1, 1);
    vec2 left_bottom = cell_pos_in_grid + vec2(0, 0);
    vec2 right_bottom = cell_pos_in_grid + vec2(1, 0);

    float left_top_dot = dot(pos_in_grid - left_top, get_gradient(left_top));
    float right_top_dot = dot(pos_in_grid - right_top, get_gradient(right_top));
    float left_bottom_dot = dot(pos_in_grid - left_bottom, get_gradient(left_bottom));
    float right_bottom_dot = dot(pos_in_grid - right_bottom, get_gradient(right_bottom));

    float noise_value = mix(
        mix(left_bottom_dot, right_bottom_dot, blend.x),
        mix(left_top_dot, right_top_dot, blend.x),
        blend.y);


    return (0.5 + 0.5 * (noise_value / 0.7));
}

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

    vec4 s = vec4(vec3(0.2), 1);

    float p = perlin_noise(vec2(texCoord0.x - time * 0.002,texCoord0.y), 15.0f);

    float stripe = smoothstep(0.4, 0.5, p) * (1.0 - smoothstep(0.5, 0.6, p));

    float intensity = stripe * 0.15;

    s += vec4(vec3(intensity), 0);

    // 0.706 = 180/256
    if (texCoord0.x < 0.706 * SoulOverflowPercentage)
    s = vec4(0.0);

    fragColor = (color + s) * ColorModulator;
}
