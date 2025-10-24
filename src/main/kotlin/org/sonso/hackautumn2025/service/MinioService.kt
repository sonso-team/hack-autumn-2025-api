package org.sonso.hackautumn2025.service

import io.minio.BucketExistsArgs
import io.minio.ListObjectsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.SetBucketPolicyArgs
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.sonso.hackautumn2025.properties.MinioProperties
import org.sonso.hackautumn2025.repository.UserRepository
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.text.Normalizer
import java.util.*

@Service
class MinioService(
    private val minioClient: MinioClient,
    private val minioProperties: MinioProperties,
    private val userRepository: UserRepository,
) {

    private val log = LoggerFactory.getLogger(MinioService::class.java)

    @PostConstruct
    fun init() {
        log.info("Initializing MinIO bucket settings...")
        ensureBucketExists()
        setPublicAccess()
    }

    private fun ensureBucketExists() {
        try {
            val bucketExists = minioClient
                .bucketExists(BucketExistsArgs.builder().bucket(minioProperties.userAvatarsBucket).build())

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.userAvatarsBucket).build())
                log.info("MinIO Bucket '${minioProperties.userAvatarsBucket}' created")
            } else {
                log.info("MinIO Bucket '${minioProperties.userAvatarsBucket}' already exists")
            }
        } catch (e: Exception) {
            log.error("Error checking/creating MinIO bucket: '${minioProperties.userAvatarsBucket}'", e)
        }
    }

    private fun setPublicAccess() {
        val policyJson = loadPolicyJson(minioProperties.userAvatarsBucket)

        minioClient.setBucketPolicy(
            SetBucketPolicyArgs.builder()
                .bucket(minioProperties.userAvatarsBucket)
                .config(policyJson)
                .build()
        )

        log.info("MinIO Bucket '${minioProperties.userAvatarsBucket}' is now public (Read-Only).")
    }

    private fun loadPolicyJson(bucketName: String): String {
        val resource = ClassPathResource("minio-public-policy.json")
        val json = resource.inputStream.bufferedReader().use { it.readText() }
        return json.replace("{{bucket_name}}", bucketName)
    }

    @Transactional
    fun uploadFile(avatarId: UUID, file: MultipartFile): String {
        val sanitizeFile = if (file.originalFilename.isNullOrEmpty()) "file" else file.originalFilename!!
        val sanitizedFilename = sanitizeFilename(sanitizeFile)

        val fileName = "$avatarId-$sanitizedFilename"

        log.info("Uploading file '$fileName' to MinIO bucket '${minioProperties.userAvatarsBucket}'...")

        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(minioProperties.userAvatarsBucket)
                .`object`(fileName)
                .stream(file.inputStream, file.size, -1)
                .contentType(file.contentType)
                .build()
        )

        val path = minioProperties.endpointDownload +
                "/${minioProperties.userAvatarsBucket}" +
                "/$fileName"

        log.info("File uploaded in MinIO successfully: $fileName")
        return path
    }

    private fun sanitizeFilename(filename: String, maxLen: Int = 200): String {
        var s = Normalizer.normalize(filename, Normalizer.Form.NFKC).trim()

        s = s.replace("\\s+".toRegex(), "_")
        s = s.replace("[^\\p{L}0-9._-]".toRegex(), "")
        s = s.replace("_+".toRegex(), "_")
        s = s.trim('.', '_')
        if (s.isEmpty()) s = "file"
        if (s.length > maxLen) s = s.take(maxLen)

        return s
    }

    /**
     * Задача будет выполняться каждый день в 03:00 ночи.
     * Удаляется файл в 2 случаях
     * 1) из MinIO если файла с таким path нет в таблицу avatars
     * 2) из MunIO и таблицы avatars, если user_id и chat_id == NULL
     *
     * (ПРОТЕСТИРОВАНО, РАБОТАЕТ)
     */
    @Scheduled(cron = "\${app.scheduled.clean-unused-files}")
    @Transactional
    fun cleanUnusedFiles() {
        log.info("Starting cleanup of unused files...")

        runBlocking {
            try {
                val objects = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(minioProperties.userAvatarsBucket).build()
                )
                coroutineScope {
                    for (result in objects) {
                        launch(Dispatchers.IO) {
                            val path = result.get().objectName()
                            val avatarEntity = userRepository
                                .findByAvatarPath("${minioProperties.userAvatarsBucket}/$path")

                            if (avatarEntity == null) {
                                log.info("Delete file with MinIO: $path")
                                deleteFile(path)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("Error retrieving file list from MinIO", e)
            }
        }
        log.info("Cleanup process completed!")
    }

    private fun deleteFile(path: String) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioProperties.userAvatarsBucket)
                    .`object`(path)
                    .build()
            )
            log.info("Deleted unused file: $path")
        } catch (e: Exception) {
            log.error("Error deleting file: $path", e)
        }
    }
}
