package com.damianw345.woof4j

import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.CompressionType
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import spark.Spark
import spark.Spark.*
import java.io.File
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


@Command(
    name = "woof4j", mixinStandardHelpOptions = true, version = ["woof4j 1.0"],
    description = ["Serves a single file <count> times via http on port <port> on IP address <ip_addr>"]
)
class Woof4jCli : Runnable {

    //  https://stackoverflow.com/a/38342964
    @Option(names = ["-i", "--ip"], description = ["IP address to share the file"])
    private var ipAddress = DatagramSocket().use { socket ->
        socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
        socket.localAddress.hostAddress
    }

    @Option(names = ["-p", "--port"], description = ["Port to be used to share the file"])
    private var port = 8080

    @Option(names = ["-c", "--count"], description = ["Number of times to share the file"])
    private var count = 1

    @Option(names = ["-z", "--gzip"], description = ["Used on a directory, it creates a tarball with gzip compression"])
    private var gzip = false

    @Option(
        names = ["-j", "--bzip2"],
        description = ["Used on a directory, it creates a tarball with bzip2 compression"]
    )
    private var bzip2 = false

    @Option(names = ["-Z", "--zip"], description = ["Used on a directory, it creates a ZIP archive"])
    private var zip = false

    @Option(
        names = ["-u", "--tarball"],
        description = ["Used on a directory, it creates a tarball with no compression"]
    )
    private var tarball = false

//    @Option(names = ["-s", "--shareWoof"], description = ["Used to distribute woof itself"])
//    private var shareWoof = false

//    @Option(names = ["-U", "--upload"], description = ["woof provides an upload form and allows uploading files"])
//    private var upload = ""

    @Parameters(
        description = ["""When a directory is specified, an tar archive gets served. By default it is gzip compressed"""]
    )
    private var filePath = ""

    private var packedFile: File? = null

    override fun run() {

        val compressionTypes = mapOf(CompressionType.GZIP to gzip, CompressionType.BZIP2 to bzip2)
        val archiveTypes = mapOf(ArchiveFormat.ZIP to zip, ArchiveFormat.TAR to tarball)

        port(port)
        Spark.ipAddress(ipAddress)

        println("Serving at: ${ipAddress}:${port}/")

        val compression = getCompressionType(compressionTypes)
        val archive = getArchiveType(archiveTypes)

        get("/") { _, response ->

            var path = Paths.get(filePath)
            val file = path.toFile()

            if(file.isDirectory){

                packedFile = if(tarball or zip)
                    archiveFile(file, archive)
                else compressFile(file, archive, compression)

                path = packedFile?.toPath()
            }

            response.header("Content-Type", "application/octet-stream")
            response.header("Content-Disposition", "attachment; filename=${path.fileName}")

            val bytes = Files.readAllBytes(path)
            val raw = response.raw()

            raw.outputStream.use { it.write(bytes) }
        }

        after("/"){ _, _ ->
            if(--count <= 0){
                packedFile?.delete()
                exitProcess(0)
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine.run(Woof4jCli(), System.err, *args)
        }
    }
}