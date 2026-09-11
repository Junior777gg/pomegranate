package org.unstabledev.pomegranate

object MediaSelector {
    fun File(onResult: (KMPFile) -> Unit) = fileChoose(onResult)
    fun MultipleFiles(onResult: (List<KMPFile>) -> Unit) = multipleFilesChoose(onResult)
    fun Image(onResult: (KMPFile) -> Unit) = imageChoose(onResult)
    fun MultipleImages(onResult: (List<KMPFile>) -> Unit) = multipleImagesChoose(onResult)

    lateinit var fileChoose: (onResult: (KMPFile) -> Unit) -> Unit
    lateinit var multipleFilesChoose: (onResult: (List<KMPFile>) -> Unit) -> Unit
    lateinit var imageChoose: (onResult: (KMPFile) -> Unit) -> Unit
    lateinit var multipleImagesChoose: (onResult: (List<KMPFile>) -> Unit) -> Unit
}