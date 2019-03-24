package com.damianw345.woof4j

import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import picocli.CommandLine
import spark.Response
import spark.Spark
import java.io.File
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

