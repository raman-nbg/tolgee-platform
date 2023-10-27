package io.tolgee.dtos.request.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpDto(
  @field:NotBlank
  var name: String = "",

  @field:Email @field:NotBlank
  var email: String = "",

  @field:Size(min = 3, max = 50)
  var organizationName: String? = null,

  @field:Size(min = 8, max = 50)
  @field:NotBlank
  var password: String? = null,

  var invitationCode: String? = null,

  var callbackUrl: String? = null,
) {
  var recaptchaToken: String? = null
}
