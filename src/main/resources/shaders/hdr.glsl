#section VERTEX_SHADER

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoordinates;

out vec2 TextureCoordinates;

void main() {
    gl_Position = vec4(position, 1.0);
    TextureCoordinates = textureCoordinates;
}

#section FRAGMENT_SHADER

in vec2 TextureCoordinates;

uniform sampler2D colorBuffer;

out vec4 fragColor;


void main() {
    vec3 color = texture(colorBuffer, TextureCoordinates).rgb;
    fragColor = vec4(color, 1.0);
}