package com.damianw345.woof4j

import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import spark.Spark
import spark.Spark.*
import java.io.File
import java.net.InetAddress
import java.net.DatagramSocket


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

    override fun run() {


        port(port)
        Spark.ipAddress(ipAddress)
        externalStaticFileLocation("/home/dw/Desktop")
        init()
        //    compressFile("/home/dw/bin", "/home/dw/Desktop")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine.run(Woof4jCli(), System.err, *args)
        }
    }

    private fun compressFile(source: String, dest: String): File {
        val archiveName = "tmp.tar.gz"
        val archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
        return archiver.create(archiveName, File(dest), File(source))
    }
}