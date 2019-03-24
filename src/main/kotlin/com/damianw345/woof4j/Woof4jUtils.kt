package com.damianw345.woof4j

import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import spark.Response
import spark.Spark
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread.sleep
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Woof4jUtils(private val packMethod: PackMethod) {

    fun serveFile(filePath: String){
        Spark.get("/") { _, response ->

            var path = Paths.get(filePath)
            val file = path.toFile()

            if (file.isDirectory)
                path = packFile(file, packMethod.archiveFormat, packMethod.compressionType).toPath()

            serializeFile(path, response)
        }
    }

    fun serveWoofJar(){
        Spark.get("/") { _, response ->

            val classLoader = ClassLoader.getSystemClassLoader()
            val inputStream = classLoader.getResourceAsStream("woof4j.jar")

            response.header("Content-Type", "application/octet-stream")
            response.header("Content-Disposition", "attachment; filename=woof4j.jar")

            var bytesRead = 0
            val buffer = ByteArray(1024)

            val raw = response.raw()
            raw.outputStream.use {
                while(inputStream.read(buffer).also { bytesRead = it } >=0) {
                    it.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    private fun packFile(file: File, archiveFormat: ArchiveFormat, compressionType: CompressionType): File {

        val archiveName = file.name

        val archiver = if(archiveFormat == ArchiveFormat.ZIP)
            ArchiverFactory.createArchiver(archiveFormat)
        else
            ArchiverFactory.createArchiver(archiveFormat, compressionType)

        val packedFile = archiver.create(archiveName, file, file)
        packedFile.deleteOnExit()

        return packedFile
    }

    private fun serializeFile(path: Path, response: Response){

        response.header("Content-Type", "application/octet-stream")
        response.header("Content-Disposition", "attachment; filename=${path.fileName}")

        val bytes = Files.readAllBytes(path)
        val raw = response.raw()

        raw.outputStream.use { it.write(bytes) }
    }

//    companion object{
//        fun serveFile() {
//            serveFile()
//        }
//    }
}

