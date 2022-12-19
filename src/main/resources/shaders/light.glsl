#section VERTEX_SHADER

layout (location=0) in vec3 inPos;
layout (location=1) in vec2 inCoord;

out vec2 outTextCoord;

void main() {
    outTextCoord = inCoord;
    gl_Position = vec4(inPos, 1.0f);
}

#section FRAGMENT_SHADER

in vec2 outTextCoord;

out vec4 fragColor;

uniform sampler2D positionsText;
uniform sampler2D diffuseText;
uniform sampler2D normalsText;

void main()
{
//    vec2 textCoord = getTextCoord();
//    float depth = texture(depthText, textCoord).r;
//    vec3 worldPos = texture(positionsText, textCoord).xyz;
//    vec4 diffuseC = texture(diffuseText, textCoord);
//    vec4 speculrC = texture(specularText, textCoord);
//    vec3 normal  = texture(normalsText, textCoord).xyz;
//    float shadowFactor = texture(shadowText, textCoord).r;
//    float reflectance = texture(shadowText, textCoord).g;

//    fragColor = vec4(outTextCoord.x, outTextCoord.y, 1, 1);
    fragColor = texture(diffuseText, outTextCoord);
}

