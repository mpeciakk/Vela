#section VERTEX_SHADER

layout (location = 0) in vec3 position;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(position, 1.0);
}

#section FRAGMENT_SHADER

out vec4 FragColor;

void main() {
    FragColor = vec4(1, 1, 1, 1);
}