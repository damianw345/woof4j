package com.damianw345.woof4j

import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import java.io.File

fun compressFile(file: File, archiveFormat: ArchiveFormat, compressionType: CompressionType): File {
    val archiveName = file.name
    val archiver = ArchiverFactory.createArchiver(archiveFormat, compressionType)
    return archiver.create(archiveName, file, file)
}

fun archiveFile(file: File, archiveFormat: ArchiveFormat): File {
    val archiveName = file.name
    val archiver = ArchiverFactory.createArchiver(archiveFormat)
    return archiver.create(archiveName, file, file)
}

fun getCompressionType(map: Map<CompressionType, Boolean>): CompressionType{

    val iter = map.filterValues { it }.keys.iterator()

    return if(iter.hasNext())
        iter.next()
    else
        CompressionType.GZIP
}

fun getArchiveType(map: Map<ArchiveFormat, Boolean>): ArchiveFormat {

    val iter = map.filterValues { it }.keys.iterator()

    return if (iter.hasNext())
        iter.next()
    else
        ArchiveFormat.TAR
}