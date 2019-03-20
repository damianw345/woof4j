package com.damianw345.woof4j

import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import java.io.File


fun isDirectory(path: String): Boolean{
    val file = File(path)
    return file.isDirectory
}

fun compressFile(source: String, dest: String): File {
    val archiveName = "tmp.tar.gz"
    val archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
    return archiver.create(archiveName, File(dest), File(source))
}