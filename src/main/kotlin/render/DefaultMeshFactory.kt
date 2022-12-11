package render

import ain.mesh.IndicesVBO
import ain.mesh.Mesh
import ain.mesh.MeshBuilder
import ain.mesh.MeshFactory

class DefaultMeshFactory : MeshFactory() {
    override fun processMesh(meshBuilder: MeshBuilder, mesh: Mesh): Mesh {
        mesh.bind()

        val vertices = mesh.getVbo(0, 3)
        val uvs = mesh.getVbo(1, 2)
        val normals = mesh.getVbo(2, 3)
        val indices = mesh.addVbo(IndicesVBO())

        val meshVertices = meshBuilder.vertices
        val meshIndices = meshBuilder.indices

        val verticesData = FloatArray(meshVertices.size * 3)
        val uvsData = FloatArray(meshVertices.size * 2)
        val normalsData = FloatArray(meshVertices.size * 3)
        val indicesData = meshIndices.toTypedArray()

        var verticesIndex = 0
        var uvsIndex = 0
        var normalsIndex = 0

        for (vertex in meshVertices) {
            verticesData[verticesIndex++] = vertex.position.x
            verticesData[verticesIndex++] = vertex.position.y
            verticesData[verticesIndex++] = vertex.position.z
            uvsData[uvsIndex++] = vertex.uvs.x
            uvsData[uvsIndex++] = vertex.uvs.y
            normalsData[normalsIndex++] = vertex.normal.y
            normalsData[normalsIndex++] = vertex.normal.y
            normalsData[normalsIndex++] = vertex.normal.y
        }

        vertices.flush(getFloatBuffer(verticesData))
        uvs.flush(getFloatBuffer(uvsData))
        normals.flush(getFloatBuffer(normalsData))
        indices.flush(getIntBuffer(indicesData.toIntArray()))

        mesh.elementsCount = indicesData.size

        mesh.unbind()

        return mesh
    }
}