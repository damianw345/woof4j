package com.damianw345.woof4j

import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.CompressionType
import picocli.CommandLine
import picocli.CommandLine.*
import spark.Spark
import spark.Spark.after
import spark.Spark.port
import java.net.DatagramSocket
import java.net.InetAddress
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

    @Option(names = ["-s", "--shareWoof"], description = ["Used to distribute woof itself"])
    private var shareWoof = false

//    @Option(names = ["-U", "--upload"], description = ["woof provides an upload form and allows uploading files"])
//    private var upload = ""

    @Parameters(
        description = ["""Optional only if -s or --shareWoof option is specified. When a directory is specified, a tar archive gets served. By default it is gzip compressed"""],
        arity = "0..1"
    )
    private var filePath = ""

    override fun run() {

        val compressionTypes = mapOf(gzip to CompressionType.GZIP, bzip2 to CompressionType.BZIP2)
        val archiveTypes = mapOf(zip to ArchiveFormat.ZIP , tarball to ArchiveFormat.TAR)

        val packMethod = PackMethod(compressionTypes.getOrDefault(true, CompressionType.GZIP), archiveTypes.getOrDefault(true, ArchiveFormat.TAR))

        val utils = Woof4jUtils(packMethod)

        port(port)
        Spark.ipAddress(ipAddress)

        utils.serve(shareWoof, filePath)

        println("Serving at: http://$ipAddress:$port/")

        after("/"){ _, _ ->
            if(--count <= 0){
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