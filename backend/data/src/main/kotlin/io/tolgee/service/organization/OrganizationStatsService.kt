package io.tolgee.service.organization

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class OrganizationStatsService(
  private val entityManager: EntityManager
) {
  fun getProjectLanguageCount(projectId: Long): Long {
    return entityManager
      .createQuery(
        """
          select count(l) from Language l 
          where l.project.id = :projectId
        """.trimIndent()
      )
      .setParameter("projectId", projectId)
      .singleResult as Long
  }

  fun getProjectKeyCount(projectId: Long): Long {
    return entityManager
      .createQuery(
        """
          select count(k) from Key k
          where k.project.id = :projectId
        """.trimIndent()
      )
      .setParameter("projectId", projectId)
      .singleResult as Long
  }

  fun getCurrentTranslationSlotCount(organizationId: Long): Long {
    val result = entityManager.createNativeQuery(
      """
      select 
         (select sum(keyCount * languageCount) as translationCount
          from (select p.id as projectId, count(l.id) as languageCount
                from project as p
                         join language as l on l.project_id = p.id
                where p.organization_owner_id = :organizationId
                group by p.id) as languageCounts
                   join (select p.id as projectId, count(k.id) as keyCount
                         from project as p
                                  join key as k on k.project_id = p.id
                         where p.organization_owner_id = :organizationId
                         group by p.id) as keyCounts on keyCounts.projectId = languageCounts.projectId)
      """.trimIndent()
    ).setParameter("organizationId", organizationId).singleResult as BigDecimal? ?: 0
    return result.toLong()
  }

  fun getCurrentTranslationCount(organizationId: Long): Long {
    return entityManager.createQuery(
      """
      select count(t) from Translation t where 
        t.key.project.organizationOwner.id = :organizationId and 
        t.state <> io.tolgee.model.enums.TranslationState.UNTRANSLATED
      """.trimIndent()
    ).setParameter("organizationId", organizationId).singleResult as Long? ?: 0
  }
}
