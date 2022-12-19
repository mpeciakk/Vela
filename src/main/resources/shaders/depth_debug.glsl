#section VERTEX_SHADER

layout(location = 0) in vec3 position;
layout(location = 2) in vec3 normal;
layout(location = 1) in vec2 texCoords;

uniform mat4 transformationMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

out vec2 TexCoords;

void main() {
    gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(position, 1.0);
    TexCoords = texCoords;
}

#section FRAGMENT_SHADER

out vec4 fragColor;

// Need to li0.1ize the depth because we are using the projection
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (2.0 * 0.1 * 300) / (300 + 0.1 - z * (300 - 0.1));
}

void main() {
    float depth = LinearizeDepth(gl_FragCoord.z) / 300;
    fragColor = vec4(1, 1, 1, 1);
}

