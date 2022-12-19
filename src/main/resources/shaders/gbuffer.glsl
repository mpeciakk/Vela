uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;

#section VERTEX_SHADER

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 uv;
layout (location = 2) in vec3 normal;

out vec3 out_vertex;
out vec2 out_uv;
out vec3 out_normal;

void main() {
    vec4 worldPos = transformationMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * viewMatrix * worldPos;

    out_uv = uv;
    out_normal = normal;
    out_vertex = worldPos.xyz;
}

#section FRAGMENT_SHADER

uniform sampler2D sampler;

in vec2 out_uv;
in vec3 out_vertex;
in vec3 out_normal;

layout (location = 0) out vec3 fs_worldpos;
layout (location = 1) out vec3 fs_diffuse;
layout (location = 2) out vec3 fs_normal;
//layout (location = 3) out vec3 fs_normal;
//layout (location = 4) out vec2 fs_shadow;

//out vec4 out_Color;

void main() {
    fs_worldpos   = out_vertex.xyz;
    fs_diffuse    = vec3(1, 1, 1);
//    fs_specular   = vec3(1, 1, 1);
    fs_normal     = normalize(out_normal);

//    out_Color = vec4(1,1,1,1);
}