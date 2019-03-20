package com.damianw345.woof4j

import picocli.CommandLine
import picocli.CommandLine.*
import spark.Spark
import spark.Spark.*
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Paths


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
    private var count = ""

    @Option(names = ["-z", "--gzip"], description = ["Used on a directory, it creates a tarball with gzip compression"])
    private var gzip = ""

    @Option(
        names = ["-j", "--bzip2"],
        description = ["Used on a directory, it creates a tarball with bzip2 compression"]
    )
    private var bzip2 = ""

    @Option(names = ["-Z", "--zip"], description = ["Used on a directory, it creates a tarball with ZIP compression"])
    private var zip = ""

    @Option(
        names = ["-u", "--tarball"],
        description = ["Used on a directory, it creates a tarball with no compression"]
    )
    private var tarball = ""

    @Option(names = ["-s", "--shareWoof"], description = ["Used to distribute woof itself"])
    private var shareWoof = ""

//    @Option(names = ["-U", "--upload"], description = ["woof provides an upload form and allows uploading files"])
//    private var upload = ""

    @Parameters(
        description = ["""When no filename is specified, or set to '-', then stdin will be read. When a directory is specified, an tar archive gets served. By default it is gzip compressed"""]
    )
    private var filePath = ""

    override fun run() {

        port(port)
        Spark.ipAddress(ipAddress)
//        externalStaticFileLocation(filePath)

        init()

        get("/") { request, response ->
            response.header("Content-Type", "application/octet-stream")

            val bytes = Files.readAllBytes(Paths.get(filePath))
            val raw = response.raw()

            raw.outputStream.use { it.write(bytes) }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine.run(Woof4jCli(), System.err, *args)
        }
    }


}