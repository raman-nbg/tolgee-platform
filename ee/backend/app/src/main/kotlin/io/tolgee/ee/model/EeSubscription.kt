package io.tolgee.ee.model

import io.hypersistence.utils.hibernate.type.array.EnumArrayType
import io.tolgee.constants.Feature
import io.tolgee.ee.data.SubscriptionStatus
import io.tolgee.model.AuditModel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Parameter
import org.hibernate.annotations.Type
import java.util.*

@Entity
@Table(schema = "ee")
class EeSubscription : AuditModel() {
  @field:Id
  val id: Int = 1

  @field:NotBlank
  lateinit var licenseKey: String

  @field:ColumnDefault("Plan")
  lateinit var name: String

  @field:NotNull
  var currentPeriodEnd: Date? = null

  var cancelAtPeriodEnd: Boolean = false

  @Type(EnumArrayType::class, parameters = [Parameter(name = EnumArrayType.SQL_ARRAY_TYPE, value = "varchar")])
  @Column(name = "enabled_features", columnDefinition = "varchar[]")
  var enabledFeatures: Array<Feature> = arrayOf()
    get() {
      return if (status != SubscriptionStatus.ERROR && status != SubscriptionStatus.CANCELED) field else arrayOf()
    }

  @Enumerated(EnumType.STRING)
  @ColumnDefault("ACTIVE")
  var status: SubscriptionStatus = SubscriptionStatus.ACTIVE

  var lastValidCheck: Date? = null
}
