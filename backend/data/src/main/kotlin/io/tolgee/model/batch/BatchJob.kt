package io.tolgee.model.batch

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import io.tolgee.batch.JobCharacter
import io.tolgee.batch.data.BatchJobDto
import io.tolgee.batch.data.BatchJobType
import io.tolgee.model.Project
import io.tolgee.model.StandardAuditModel
import io.tolgee.model.UserAccount
import io.tolgee.model.activity.ActivityRevision
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "tolgee_batch_job")
class BatchJob : StandardAuditModel(), IBatchJob {
  @ManyToOne(fetch = FetchType.LAZY)
  lateinit var project: Project

  @ManyToOne(fetch = FetchType.LAZY)
  var author: UserAccount? = null

  @Type(JsonBinaryType::class)
  var target: List<Any> = listOf()

  var totalItems: Int = 0

  var totalChunks: Int = 0

  var chunkSize: Int = 0

  @Enumerated(STRING)
  override var status: BatchJobStatus = BatchJobStatus.PENDING

  @Enumerated(STRING)
  var type: BatchJobType = BatchJobType.PRE_TRANSLATE_BT_TM

  @OneToOne(mappedBy = "batchJob", fetch = FetchType.LAZY)
  var activityRevision: ActivityRevision? = null

  @Type(JsonBinaryType::class)
  var params: Any? = null

  val chunkedTarget get() = chunkTarget(chunkSize, target)

  var maxPerJobConcurrency: Int = -1

  @Enumerated(STRING)
  var jobCharacter: JobCharacter = JobCharacter.FAST

  var hidden: Boolean = false

  val dto get() = BatchJobDto.fromEntity(this)

  companion object {
    fun <T> chunkTarget(chunkSize: Int, target: List<T>): List<List<T>> =
      if (chunkSize == 0) listOf(target) else target.chunked(chunkSize)
  }
}
