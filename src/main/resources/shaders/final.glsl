#section VERTEX_SHADER

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

out vec2 outTextCoord;

void main()
{
    gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
    outTextCoord = aTexCoords;
}

#section FRAGMENT_SHADER

out vec4 FragColor;

in vec2 outTextCoord;

uniform sampler2D positionTexture;
uniform sampler2D diffuseTexture;
uniform sampler2D normalTexture;
uniform sampler2D depthTexture;

struct PointLight {
    vec3 position;
    vec3 color;
    float intensity;
    float constant;
    float linear;
    float exponent;
};

uniform PointLight light;

vec4 calcLightColor(vec4 diffuse, vec4 specular, float reflectance, vec3 lightColor, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal) {
    vec4 diffuseColor = vec4(0, 0, 0, 1);
    vec4 specColor = vec4(0, 0, 0, 1);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColor = diffuse * vec4(lightColor, 1.0) * light_intensity * diffuseFactor;

    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir, normal));
    float specularFactor = max(dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, 2);
    specColor = specular * light_intensity  * specularFactor * reflectance * vec4(lightColor, 1.0);

    return (diffuseColor + specColor);
}

vec4 calcPointLight(vec4 diffuse, vec4 specular, float reflectance, PointLight light, vec3 position, vec3 normal) {
    vec3 light_direction = light.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec4 light_color = calcLightColor(diffuse, specular, reflectance, light.color, light.intensity, position, to_light_dir, normal);

    // Apply Attenuation
    float distance = length(light_direction);
    float attenuationInv = light.constant + light.linear * distance +
    light.exponent * distance * distance;
    return light_color / attenuationInv;
}

void main()
{
    float depth = texture(depthTexture, outTextCoord).x;
    if (depth == 1) {
        discard;
    }

    vec3 position = texture(positionTexture, outTextCoord).xyz;

    vec4 albedoSamplerValue = vec4(0);
    vec3 albedo  = albedoSamplerValue.rgb;
    vec4 diffuse = vec4(albedo, 1);

    vec3 normal = normalize(texture(normalTexture, outTextCoord).rgb);

    float reflectance = 1;
    vec4 specular = vec4(1);

    vec4 diffuseSpecularComp = vec4(0);

//    PointLight light = PointLight(vec3(0, 10, 0), vec3(1, 1, 1), 1, 1, 0, 0);

    diffuseSpecularComp += calcPointLight(diffuse, specular, reflectance, light, position, normal);

    FragColor = vec4(0.1, 0.1, 0.1, 1) + diffuseSpecularComp;
}