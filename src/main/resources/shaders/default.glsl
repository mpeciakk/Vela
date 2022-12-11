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

struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    int hasTexture;
    float reflectance;
};

struct DirectionalLight {
    vec3 color;
    vec3 direction;
    float intensity;
};

struct PointLight {
    vec3 color;
    vec3 position;
    float intensity;
    float constant;
    float linear;
    float exponent;
};

uniform sampler2D sampler;

uniform vec3 ambientLight;
uniform Material material;
uniform float specularPower;
uniform DirectionalLight directionalLight;
uniform PointLight pointLight;

vec4 ambientC;
vec4 diffuseC;
vec4 specularC;

void setupColors(Material material, vec2 uv) {
    if (material.hasTexture == 1) {
        ambientC = texture(sampler, uv);
        diffuseC = ambientC;
        specularC = ambientC;
    } else {
        ambientC = vec4(material.ambient, 1);
        diffuseC = vec4(material.diffuse, 1);
        specularC = vec4(material.specular, 1);
    }
}

vec4 calcLightColor(vec3 color, float intensity, vec3 position, vec3 toLightDirection, vec3 normal) {
    vec4 diffuseColor = vec4(0, 0, 0, 0);
    vec4 specularColor = vec4(0, 0, 0, 0);

    float diffuseFactor = max(dot(normal, toLightDirection), 0.0);
    diffuseColor = diffuseC * vec4(color, 1.0) * intensity * diffuseFactor;

    vec3 cameraDirection = normalize(-position);
    vec3 fromLightDirection = -toLightDirection;
    vec3 reflectedLight = normalize(reflect(fromLightDirection, normal));
    float specularFactor = max(dot(cameraDirection, reflectedLight), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specularColor = specularC * intensity * specularFactor * material.reflectance * vec4(color, 1.0);

    return diffuseColor + specularColor;
}

vec4 calcDirectionalLight(DirectionalLight light, vec3 position, vec3 normal) {
    return calcLightColor(light.color, light.intensity, position, normalize(light.direction), normal);
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal) {
    vec3 lightDirection = light.position - position;
    vec3 toLightDirection = normalize(lightDirection);
    vec4 lightColor = calcLightColor(light.color, light.intensity, position, toLightDirection, normal);

    float distance = length(lightDirection);
    float attenuationInv = light.constant + light.linear * distance + light.exponent * distance * distance;
    return lightColor / attenuationInv;
}

in vec2 out_uv;
in vec3 out_vertex;
in vec3 out_normal;

out vec4 out_Color;

void main() {
    setupColors(material, out_uv);

    vec4 diffuseSpecularComp = calcPointLight(pointLight, out_vertex, out_normal);
//    diffuseSpecularComp += calcPointLight(pointLight, out_vertex, out_normal);

    out_Color = ambientC * vec4(ambientLight, 1) + diffuseSpecularComp;
}