package io.tolgee.api.v2.controllers

import io.tolgee.ProjectAuthControllerTest
import io.tolgee.constants.Message
import io.tolgee.fixtures.andHasErrorMessage
import io.tolgee.fixtures.andIsBadRequest
import io.tolgee.fixtures.andIsOk
import io.tolgee.model.enums.ProjectPermissionType
import io.tolgee.model.enums.Scope
import io.tolgee.testing.InvitationTestUtil
import io.tolgee.testing.annotations.ProjectJWTAuthTestMethod
import io.tolgee.testing.assert
import org.junit.jupiter.api.Test

class V2ProjectsInvitationControllerEeTest : ProjectAuthControllerTest("/v2/projects/") {

  val invitationTestUtil: InvitationTestUtil by lazy {
    InvitationTestUtil(this, applicationContext)
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `invites user to project with scopes`() {
    val result = invitationTestUtil.perform {
      scopes = setOf("translations.edit")
    }.andIsOk

    val invitation = invitationTestUtil.getInvitation(result)
    invitation.permission!!.scopes.assert.containsExactlyInAnyOrder(Scope.TRANSLATIONS_EDIT)
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `adds the languages to view`() {
    val result = invitationTestUtil.perform { getLang ->
      scopes = setOf("translations.edit")
      translateLanguages = setOf(getLang("en"))
    }.andIsOk

    val invitation = invitationTestUtil.getInvitation(result)
    invitation.permission!!.translateLanguages.map { it.tag }.assert.containsExactlyInAnyOrder("en")
  }

  @Test
  fun `validates permissions (type vs scopes)`() {
    invitationTestUtil.perform {
      scopes = setOf("translations.edit")
      type = ProjectPermissionType.MANAGE
    }
      .andIsBadRequest.andHasErrorMessage(Message.SET_EXACTLY_ONE_OF_SCOPES_OR_TYPE)
  }

  @Test
  fun `validates language permissions`() {
    invitationTestUtil.perform { getLang ->
      scopes = setOf("translations.view")
      this.translateLanguages = setOf(getLang("en"))
    }.andIsBadRequest.andHasErrorMessage(Message.CANNOT_SET_TRANSLATE_LANGUAGES_WITHOUT_TRANSLATIONS_EDIT_SCOPE)
  }
}
