#section VERTEX_SHADER

layout (location = 0) in vec3 aPos;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;

void main()
{
    gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(aPos, 1.0);
}

#section FRAGMENT_SHADER

void main()
{
     gl_FragDepth = gl_FragCoord.z;
}
