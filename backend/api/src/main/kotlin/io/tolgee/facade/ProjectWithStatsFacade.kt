package io.tolgee.facade

import io.sentry.Sentry
import io.tolgee.dtos.query_results.ProjectStatistics
import io.tolgee.hateoas.project.ProjectWithStatsModel
import io.tolgee.hateoas.project.ProjectWithStatsModelAssembler
import io.tolgee.model.enums.TranslationState
import io.tolgee.model.views.ProjectWithLanguagesView
import io.tolgee.model.views.ProjectWithStatsView
import io.tolgee.service.LanguageService
import io.tolgee.service.project.LanguageStatsService
import io.tolgee.service.project.ProjectStatsService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.PagedModel
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class ProjectWithStatsFacade(
  private val projectStatsService: ProjectStatsService,
  private val pagedWithStatsResourcesAssembler: PagedResourcesAssembler<ProjectWithStatsView>,
  private val projectWithStatsModelAssembler: ProjectWithStatsModelAssembler,
  private val languageStatsService: LanguageStatsService,
  private val languageService: LanguageService
) {
  fun getPagedModelWithStats(
    projects: Page<ProjectWithLanguagesView>,
  ): PagedModel<ProjectWithStatsModel> {
    val projectIds = projects.content.map { it.id }
    val totals = projectStatsService.getProjectsTotals(projectIds)
    val languages = languageService.getViewsOfProjects(projectIds)
      .groupBy { it.language.project.id }

    val languageStats = languageStatsService.getLanguageStats(projectIds)

    val projectsWithStatsContent = projects.content.map { projectWithLanguagesView ->
      val projectTotals = totals[projectWithLanguagesView.id]
      val baseLanguage = projectWithLanguagesView.baseLanguage
      val projectLanguageStats = languageStats[projectWithLanguagesView.id]

      var stateTotals: ProjectStatsService.ProjectStateTotals? = null
      if (baseLanguage != null && projectLanguageStats != null) {
        stateTotals = projectStatsService.computeProjectTotals(
          baseLanguage,
          projectLanguageStats
            .sortedBy { it.language.name }
            .sortedBy { it.language.id != baseLanguage.id }
        )
      }

      val translatedPercent = stateTotals?.translatedPercent.toPercentValue()
      val reviewedPercent = stateTotals?.reviewedPercent.toPercentValue()
      val untranslatedPercent = (BigDecimal(100) - translatedPercent - reviewedPercent).setScale(
        2,
        RoundingMode.HALF_UP
      )

      val projectStatistics = ProjectStatistics(
        projectId = projectWithLanguagesView.id,
        keyCount = projectTotals?.keyCount ?: 0,
        languageCount = projectTotals?.languageCount ?: 0,
        translationStatePercentages = mapOf(
          TranslationState.TRANSLATED to translatedPercent,
          TranslationState.REVIEWED to reviewedPercent,
          TranslationState.UNTRANSLATED to untranslatedPercent
        )
      )
      ProjectWithStatsView(
        view = projectWithLanguagesView,
        stats = projectStatistics,
        languages = languages[projectWithLanguagesView.id] ?: listOf()
      )
    }
    val page = PageImpl(projectsWithStatsContent, projects.pageable, projects.totalElements)
    return pagedWithStatsResourcesAssembler.toModel(page, projectWithStatsModelAssembler)
  }

  fun Double?.toPercentValue(): BigDecimal {
    if (this == null || this.isNaN()) {
      return BigDecimal(0)
    }
    return try {
      this.toBigDecimal().setScale(3, RoundingMode.HALF_UP)
    } catch (e: NumberFormatException) {
      Sentry.captureException(e, "Failed to convert $this to BigDecimal")
      BigDecimal(0)
    }
  }
}
